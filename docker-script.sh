#!/bin/sh
./gradlew build                                                    # build jar file
docker build --build-arg JAR_FILE="build/libs/*.jar" -t db-image . # build docker image

ROOT_COORDINATOR=$(docker container create -p 8080:8080 db-image) # name for root node
SUBORDINATE_LIST=()                                               # list for pure subordinates

SUB_PORTS="" # port list for host setup
for (( i = 0; i < $1; i++ )); do
  SUBORDINATE_LIST+=($(docker container create -p "808$i":8080 db-image))
  SUB_PORTS+="\"808$i\","
done

docker start $ROOT_COORDINATOR &# run root node
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

curl --location --request GET 'http://localhost:8080/memory/nonVol' \
  --header 'Content-Type: application/json'

docker stop $ROOT_COORDINATOR # stop root node
