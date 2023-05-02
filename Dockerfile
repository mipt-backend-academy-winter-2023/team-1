FROM sbtscala/scala-sbt:eclipse-temurin-jammy-19.0.1_10_1.8.2_2.13.10 AS build
COPY . /root/
WORKDIR /root
ENV SBT_OPTS="-Xms4G -Xmx4G -Xss8M"
RUN sbt assembly

FROM eclipse-temurin:19-jre-focal
RUN mkdir -p  /opt/app
COPY --from=build /root/auth/target/scala-2.13/project-auth-assembly-0.1.0-SNAPSHOT.jar /opt/app/auth.jar
COPY --from=build /root/routing/target/scala-2.13/project-routing-assembly-0.1.0-SNAPSHOT.jar /opt/app/routing.jar
