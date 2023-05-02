FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y curl zip unzip

ARG JAVA_VERSION="17.0.7-zulu"

RUN rm /bin/sh && ln -s /bin/bash /bin/sh

#RUN useradd -ms /bin/bash -p randompass service
#USER service

WORKDIR /workspace

ADD ./ci ./ci

RUN ./ci/install_sdk.sh && ./ci/install_java.sh "$JAVA_VERSION" && ./ci/install_sbt.sh

ADD ./build.sbt .
RUN bash -c "source ~/.sdkman/bin/sdkman-init.sh && sbt --version"
ADD . .
RUN bash -c "source ~/.sdkman/bin/sdkman-init.sh && sbt package"

EXPOSE 7777
EXPOSE 8081
EXPOSE 8082

ENTRYPOINT bash -c "source ~/.sdkman/bin/sdkman-init.sh && sbt run"