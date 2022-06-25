FROM openjdk:17-alpine

EXPOSE 25565
WORKDIR /opt/paper
ADD https://api.papermc.io/v2/projects/paper/versions/1.19/builds/36/downloads/paper-1.19-36.jar paper.jar

RUN apk --update add --no-cache ca-certificates

CMD java -jar -Dcom.mojang.eula.agree=true paper.jar --nogui
