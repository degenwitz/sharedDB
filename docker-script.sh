#!/bin/bash
# call this script with one integer argument, namely the number of subordinate nodes should be created
./gradlew build                                                                        # build jar file
docker build --build-arg JAR_FILE="build/libs/*.jar" -t db-image .                     # build docker image

echo -ne "\033[2m"

docker stop "coordinator" && docker rm "coordinator" # cleanup host if already created before
ROOT_COORDINATOR=$(docker container create --name "coordinator" -p 8080:8080 db-image) # name for root node
SUBORDINATE_LIST=()     # list for pure subordinates

internaldockeradress=${2:-http://docker.host.internal:localhost}

# init test result file
TESTOUT="test-out.csv"
echo "#Subordinates,StoppedNode,HandledCorrectly,UncommittedCorrectly" > $TESTOUT

SUB_PORTS="" # port list for host setup
for ((i = 1; i <= $1; i++)); do
  docker stop "sub$i" && docker rm "sub$i" # clean up previous instance
  SUBORDINATE_LIST+=($(docker container create --name "sub$i" -p "808$i":8080 db-image))
  SUB_PORTS+="\"808$i\","
done

docker start $ROOT_COORDINATOR # run root node
# start subordinates
for ((i = 1; i <= $1; i++)); do
  docker start ${SUBORDINATE_LIST[$((i - 1))]} #&
done

echo -e "\033[0m\033[1mWaiting for containers to start up... (30s)\033[0m\033[2m"
sleep 30 # wait for all of the containers to start up

# post setup for root node
curl --location --request POST 'http://localhost:8080/client/setup' \
  --header 'Content-Type: application/json' \
  --data-raw "{
	\"hostPort\": \"8080\",
	\"address\": \"$internaldockeradress\",
	\"myPort\": \"8080\",
	\"isCoordinator\": \"true\",
	\"subPorts\": [${SUB_PORTS%?}],
	\"sleepTimer\": 10000
}"

# setup subordinates
for ((i = 1; i <= $1; i++)); do
    curl --location --request POST "http://localhost:808$i/client/setup" \
    --header 'Content-Type: application/json' \
    --data-raw "{
	\"hostPort\": \"8080\",
	\"address\": \"$internaldockeradress\",
	\"myPort\": \"808$i\",
	\"isCoordinator\": \"false\",
	\"subPorts\": [],
	\"sleepTimer\": 10000
}"
done

CRASH_TIME=$((RANDOM % 20 + 40))
PORT_TO_CRASH=$((RANDOM % ($1 + 1)))

echo -e "\033[0m\033[1mWaiting for setups to finish... (10s)\033[0m\033[2m"
sleep 10 # wait for setups to finish

# start transaction
curl --location --request POST "http://localhost:8080/commit/$i"

echo -e "\033[0m\033[1mWaiting for crash...(${CRASH_TIME}s)\033[0m\033[2m"
sleep $CRASH_TIME

if [ $PORT_TO_CRASH -eq 0 ]; then
  #  curl --location --request GET 'http://localhost:8080/memory/nonVol' \
  #    --header 'Content-Type: application/json'
  docker stop $ROOT_COORDINATOR
  echo -e "\033[0m\033[1mStopped root node after $CRASH_TIME seconds.\033[0m\033[2m"
  docker start $ROOT_COORDINATOR
  sleep 15
  curl --location --request GET 'http://localhost:8080/memory/nonVol' \
    --header 'Content-Type: application/json'
else
  #  curl --location --request GET "http://localhost:808$PORT_TO_CRASH/memory/nonVol" \
  #    --header 'Content-Type: application/json'
  docker stop ${SUBORDINATE_LIST[$((PORT_TO_CRASH - 1))]}
  echo -e "\033[0m\033[1mStopped subordinate node on port 808$PORT_TO_CRASH in $CRASH_TIME seconds.\033[0m\033[2m"
  docker start ${SUBORDINATE_LIST[$((PORT_TO_CRASH - 1))]}
  sleep 15
  curl --location --request GET "http://localhost:808$PORT_TO_CRASH/memory/nonVol" \
    --header 'Content-Type: application/json'
fi

# check for inconsistencies
HOST_UNCOMMITED=$(curl --silent --location --request GET 'http://localhost:8080/processes/uncommited')
HOST_HANDLED=$(curl --silent --location --request GET 'http://localhost:8080/processes/handled')

HANDLED_CORRECTLY=true
UNCOMMITTED_CORRECTLY=true
# get uncommitted processes
for ((i = 1; i <= $1; i++)); do
  RESULT=$(curl --silent --location --request GET "http://localhost:808$i/processes/uncommited")
  UNCOMMITTED_CORRECTLY=$( [[ "\"$RESULT\"" == "\"$HOST_UNCOMMITED\"" ]] && echo $UNCOMMITTED_CORRECTLY || echo false )
done
echo
echo -e "\033[0m\033[1mWaiting for reception of uncommitted processes... (10s)\033[0m\033[2m"
sleep 10

# get handled processes
for ((i = 1; i <= $1; i++)); do
  RESULT=$(curl --silent --location --request GET "http://localhost:808$i/processes/handled")
  HANDLED_CORRECTLY=$( [[ "\"$RESULT\"" == "\"$HOST_HANDLED\"" ]] && echo $HANDLED_CORRECTLY || echo false )
done

echo -e "\033[0m\033[1mWaiting for reception of handled processes... (10s)\033[0m\033[2m"
sleep 10

echo "$1,$PORT_TO_CRASH,$HANDLED_CORRECTLY,$UNCOMMITTED_CORRECTLY" >> $TESTOUT

# stop subordinates
for port in "${SUBORDINATE_LIST[@]}"; do
  docker stop $port
done
docker stop $ROOT_COORDINATOR # stop root node

docker stop "coordinator" && docker rm "coordinator"
for ((i = 1; i <= $1; i++)); do
  docker stop "sub$i" && docker rm "sub$i"
done

echo -e "\033[0m\033[1mCheck $TESTOUT for the results!\033[0m\a"
