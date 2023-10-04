FROM ubuntu:latest AS builder

RUN apt-get update
RUN apt-get install -y curl zip unzip

ARG JAVA_VERSION="17.0.7-amzn"

RUN rm /bin/sh && ln -s /bin/bash /bin/sh

WORKDIR /workspace

COPY ../ci ./ci

RUN ./ci/install_sdk.sh && ./ci/install_java.sh "$JAVA_VERSION"
RUN ./ci/install_sbt.sh

RUN apt-get install -y locales && locale-gen en_DK.UTF-8

COPY ../build.sbt .
RUN bash -c "source ~/.sdkman/bin/sdkman-init.sh && sbt --version"
COPY .. .
RUN bash -c "source ~/.sdkman/bin/sdkman-init.sh && sbt clean && LC_ALL=en_DK.UTF-8 sbt assembly"

FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y curl zip unzip

ARG JAVA_VERSION="17.0.7-amzn"

RUN rm /bin/sh && ln -s /bin/bash /bin/sh

WORKDIR /workspace

COPY ../ci ./ci

RUN ./ci/install_sdk.sh && ./ci/install_java.sh "$JAVA_VERSION"

RUN apt-get install -y locales && locale-gen en_DK.UTF-8

COPY --from=builder /workspace/photos/target/scala-2.13/*.jar .

EXPOSE 8083

ENTRYPOINT bash -c "source ~/.sdkman/bin/sdkman-init.sh && LC_ALL=en_DK.UTF-8 java -cp /workspace/*.jar photos.PhotosMain"
