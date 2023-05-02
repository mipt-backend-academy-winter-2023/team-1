FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y curl zip unzip

RUN curl -s "https://get.sdkman.io" | bash
RUN exec bash && sdk install java 17.0.7-zulu
RUN exec bash && sdk install sbt

WORKDIR /workspace
ADD . .
RUN exec bash && sbt package

# EXPOSE 8080 # TODO Drakohka95 specify port
CMD exec bash && sbt run