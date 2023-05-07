FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y curl zip unzip

ARG JAVA_VERSION="17.0.7-zulu"

RUN rm /bin/sh && ln -s /bin/bash /bin/sh

WORKDIR /workspace

ADD ../ci ./ci

RUN ./ci/install_sdk.sh && ./ci/install_java.sh "$JAVA_VERSION" && ./ci/install_sbt.sh

ADD ../build.sbt .
RUN bash -c "source ~/.sdkman/bin/sdkman-init.sh && sbt --version"
ADD ../.. .
RUN bash -c "source ~/.sdkman/bin/sdkman-init.sh && sbt package"

EXPOSE 8081

ENTRYPOINT bash -c "source ~/.sdkman/bin/sdkman-init.sh && sbt 'runMain routing.RoutingMain'"
