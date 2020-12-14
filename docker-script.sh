#!/bin/bash
# call this script with one integer argument, namely the number of test rounds that should be run
./gradlew build                                                    # build jar file
docker build --build-arg JAR_FILE="build/libs/*.jar" -t db-image . # build docker image

internaldockeradress=${2:-http://docker.host.internal:localhost}

# init test result file
TESTOUT="test-out.csv"
echo "Loop,#Subordinates,StoppedNode,HandledCorrectly,UncommittedCorrectly" >$TESTOUT

echo
echo -e "\033[1mCleanup of any dangling containers\033[0m\033[2m"
# cleanup containers if already created before
docker stop "coordinator" && docker rm "coordinator"
for ((i = 1; i <= 9; i++)); do
  docker stop "sub$i" && docker rm "sub$i"
done

for ((j = 1; j <= $1; j++)); do
  echo
  echo -e "\033[0m\033[1mTest Number ${j}\033[0m\033[2m"

  ROOT_COORDINATOR=$(docker container create --name "coordinator" -p 8080:8080 db-image) # name for root node
  SUBORDINATE_LIST=()                                                                    # list for pure subordinates

  NUMBER_OF_SUBS=$((RANDOM % 9 + 1))

  SUB_PORTS="" # port list for host setup
  for ((i = 1; i <= $NUMBER_OF_SUBS; i++)); do
    SUBORDINATE_LIST+=($(docker container create --name "sub$i" -p "808$i":8080 db-image))
    SUB_PORTS+="\"808$i\","
  done

  echo -e "\033[0mWaiting for containers to start up...\033[2m"

  docker start $ROOT_COORDINATOR &# run root node
  sleep 5
  # start subordinates
  for ((i = 1; i <= $NUMBER_OF_SUBS; i++)); do
    docker start ${SUBORDINATE_LIST[$((i - 1))]}
    sleep 8
  done

  sleep 10 # wait for all of the containers to start up

  echo -e "\033[0mWaiting for setups...\033[2m"

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
  sleep 10

  # setup subordinates
  for ((i = 1; i <= $NUMBER_OF_SUBS; i++)); do
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
    echo "http://localhost:808$i/process/$j"
    curl --location --request POST "http://localhost:808$i/process/$j" \
    --data-raw "Lorem ipsumm $i$j"
  done
  curl --location --request POST "http://localhost:8080/process/$j" \
    --data-raw "Lorem ipsumCoord$j"
    

  CRASH_TIME=$((RANDOM % 400 + 100))
  PORT_TO_CRASH=$((RANDOM % ($NUMBER_OF_SUBS + 1)))

  sleep 10 # wait for setups to finish

  # start transaction
  curl --location --request POST "http://localhost:8080/commit/$j"

  echo -e "\033[0mWaiting for crash...($((CRASH_TIME/100))s)\033[2m"
  sleep $((CRASH_TIME/100))

  if [ $PORT_TO_CRASH -eq 0 ]; then
    docker stop $ROOT_COORDINATOR
    echo -e "\033[0mStopped root node after $((CRASH_TIME/100)) seconds.\033[2m"
    docker start $ROOT_COORDINATOR
  else
    docker stop ${SUBORDINATE_LIST[$((PORT_TO_CRASH - 1))]}
    echo -e "\033[0mStopped subordinate node on port 808$PORT_TO_CRASH in $((CRASH_TIME/100)) seconds.\033[2m"
    docker start ${SUBORDINATE_LIST[$((PORT_TO_CRASH - 1))]}
    sleep 5
  fi

  # check for inconsistencies
  UNCOMMITTED_CORRECTLY=true
  # get uncommitted processes
  echo -e "\033[0mWaiting for reception of uncommitted processes...\033[2m"
  SUB1_UNCOMMITED=$(curl --silent --location --request GET "http://localhost:8081/processes/uncommited")
  sleep 5
  echo $SUB1_UNCOMMITED
  for ((i = 2; i <= $NUMBER_OF_SUBS; i++)); do
    RESULT=$(curl --silent --location --request GET "http://localhost:808$i/processes/uncommited")
    echo $RESULT
    UNCOMMITTED_CORRECTLY=$([[ "\"$RESULT\"" == "\"$SUB1_UNCOMMITED\"" ]] && echo $UNCOMMITTED_CORRECTLY || echo false)
    sleep 5
  done

  sleep 5

  HANDLED_CORRECTLY=true
  # get handled processes
  echo -e "\033[0mWaiting for reception of handled processes...\033[2m"
  SUB1_HANDLED=$(curl --silent --location --request GET 'http://localhost:8081/processes/handled')
  sleep 5
  echo $SUB1_HANDLED
  for ((i = 1; i <= $NUMBER_OF_SUBS; i++)); do
    RESULT=$(curl --silent --location --request GET "http://localhost:808$i/processes/handled")
    echo $RESULT
    HANDLED_CORRECTLY=$([[ "\"$RESULT\"" == "\"$SUB1_HANDLED\"" ]] && echo $HANDLED_CORRECTLY || echo false)
    sleep 5
  done

  echo "$j,$NUMBER_OF_SUBS,$PORT_TO_CRASH,$HANDLED_CORRECTLY,$UNCOMMITTED_CORRECTLY" >>$TESTOUT

  sleep 20
  for ((i = 0; i <= $NUMBER_OF_SUBS; i++)); do
    RESULT=$(curl --silent --location --request GET "http://localhost:808$i/memory/nonVol")
    echo $RESULT
  done

  # stop subordinates
  for port in "${SUBORDINATE_LIST[@]}"; do
    docker stop $port
  done
  docker stop $ROOT_COORDINATOR # stop root node

  docker stop "coordinator" && docker rm "coordinator"
  for ((i = 1; i <= $NUMBER_OF_SUBS; i++)); do
    docker stop "sub$i" && docker rm "sub$i"
  done
done

echo -e "\033[0m\033[1mCheck $TESTOUT for the results!\033[0m\a"
