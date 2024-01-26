#
# https://hub.docker.com/_/maven
#
FROM maven:3.9.3-eclipse-temurin-17 as maven_build
WORKDIR /stone.lunchtime
ARG USE_MAVEN_PROFILE=${USE_MAVEN_PROFILE}
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2  mvn -Dmaven.test.skip=true package
RUN mkdir -p target/docker-packaging && cd target/docker-packaging && jar -xf ../stone.lunchtime*.war

#
# Caution : 17-jdk-alpine version does not 
# work on Mac M1 (Apple chipset)
# So I use latest instead
# https://hub.docker.com/_/eclipse-temurin
FROM eclipse-temurin:latest
WORKDIR /stone.lunchtime
ARG DOCKER_PACKAGING_DIR=/stone.lunchtime/target/docker-packaging
COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/WEB-INF/lib /stone.lunchtime/lib
COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/WEB-INF/classes /stone.lunchtime/classes
COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/META-INF /stone.lunchtime/META-INF
CMD java -cp .:classes:lib/* \
         -Dspring.application.json=${SPRING_APPLICATION_JSON} \
         -Djava.security.egd=file:/dev/./urandom \
         stone.lunchtime.SpringBootConfiguration