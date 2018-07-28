FROM openjdk:8

COPY deploy /usr/src/betaville-server
WORKDIR /usr/src/betaville-server

ENTRYPOINT ["java", "-jar", "BetavilleServer.jar"]
