#Build stage

FROM gradle:8.3.0-jdk17-alpine AS BUILD
WORKDIR /usr/app/
COPY . . 
# gradle build would run the tests as well
RUN gradle assemble

# Package stage

FROM ubuntu:22.04
RUN apt-get update && apt-get install -y gcc-riscv64-linux-gnu openjdk-17-jdk
WORKDIR /usr/app
COPY --from=BUILD /usr/app .
EXPOSE 8000
ENTRYPOINT ./gradlew run --args="server --host=0.0.0.0 --port=8000"