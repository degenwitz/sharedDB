based on the tutorial on https://spring.io/guides/gs/spring-boot-docker/

TODO:
* implement Host (N)
* finish Client (D)
* SetupAs[Client/Host] declare node as client (D) resp. host (N)
* FailOn[Commit/Prepare] -> induce failures (D)

LATER:
* Recovery service
* Tree structure
* PA
* PC
* case by case choice between PA / PC
* !!! N: force write outside of docker containers ([website](https://docs.docker.com/storage/))
    * docker volumes: managed by docker
