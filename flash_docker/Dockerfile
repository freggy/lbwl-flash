#FROM openjdk:17-alpine AS builder
#
#WORKDIR /minecraft
#ADD https://api.papermc.io/v2/projects/paper/versions/1.8.8/builds/443/downloads/paper-1.8.8-443.jar paper.jar
#ADD https://api.papermc.io/v2/projects/paper/versions/1.19/builds/36/downloads/paper-mojmap-1.19-36.jar paper.jar
#RUN java -jar -Dcom.mojang.eula.agree=true paper.jar

FROM openjdk:17-alpine

EXPOSE 25565
WORKDIR /opt/paper
ADD https://api.papermc.io/v2/projects/paper/versions/1.19/builds/36/downloads/paper-1.19-36.jar paper.jar
#COPY --from=builder /minecraft/versions/1.19/paper*.jar /opt/paper/paper.jar

RUN apk --update add --no-cache ca-certificates
RUN echo "eula=true" > eula.txt

CMD java -jar -Dcom.mojang.eula.agree=true paper.jar --nogui
