#!/bin/sh
# call this script with one integer argument, namely the number of subordinate nodes should be created
./gradlew build                                                    # build jar file
docker build --build-arg JAR_FILE="build/libs/*.jar" -t db-image . # build docker image

ROOT_COORDINATOR=$(docker container create -p 8080:8080 db-image) # name for root node
SUBORDINATE_LIST=()                                               # list for pure subordinates

SUB_PORTS="" # port list for host setup
for ((i = 1; i <= $1; i++)); do
  SUBORDINATE_LIST+=($(docker container create -p "808$i":8080 db-image))
  SUB_PORTS+="\"808$i\","
done

docker start $ROOT_COORDINATOR # run root node
sleep 10

# post setup for root node
curl --location --request POST 'http://localhost:8080/client/setup' \
  --header 'Content-Type: application/json' \
  --data-raw "{
	\"hostPort\": \"8080\",
	\"address\": \"http://docker.host.internal:localhost\",
	\"myPort\": \"8080\",
	\"isCoordinator\": \"true\",
	\"subPorts\": [${SUB_PORTS%?}]
}"

# start and setup subordinates
for ((i = 1; i <= $1; i++)); do
  docker start ${SUBORDINATE_LIST[$((i - 1))]} #&
  sleep 10
  curl --location --request POST "http://localhost:808$i/client/setup" \
    --header 'Content-Type: application/json' \
    --data-raw "{
	\"hostPort\": \"8080\",
	\"address\": \"http://docker.host.internal:localhost\",
	\"myPort\": \"808$i\",
	\"isCoordinator\": \"false\",
	\"subPorts\": []
}"
done

# set time until crash and port to crash
CRASH_TIME=$((RANDOM % 30 + 30))
PORT_TO_CRASH=$((RANDOM % ($1 + 1)))

sleep $CRASH_TIME

if [ $PORT_TO_CRASH -eq 0 ]; then
  curl --location --request GET 'http://localhost:8080/memory/nonVol' \
  --header 'Content-Type: application/json'

  docker stop $ROOT_COORDINATOR
  echo "stopped root node after $CRASH_TIME seconds"

  docker start $ROOT_COORDINATOR
  sleep 15
  curl --location --request GET 'http://localhost:8080/memory/nonVol' \
  --header 'Content-Type: application/json'
else
  curl --location --request GET "http://localhost:808$PORT_TO_CRASH/memory/nonVol" \
  --header 'Content-Type: application/json'

  docker stop ${SUBORDINATE_LIST[$((PORT_TO_CRASH - 1))]}
  echo "stopped subordinate node on port 808$PORT_TO_CRASH in $CRASH_TIME seconds"

  docker start ${SUBORDINATE_LIST[$((PORT_TO_CRASH - 1))]}
  sleep 15
  curl --location --request GET "http://localhost:808$PORT_TO_CRASH/memory/nonVol" \
  --header 'Content-Type: application/json'
fi

# stop subordinates
for port in "${SUBORDINATE_LIST[@]}"; do
  docker stop $port
done
docker stop $ROOT_COORDINATOR # stop root node
