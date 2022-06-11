FROM  adoptopenjdk/openjdk11:alpine-slim AS builder

WORKDIR /minecraft
ADD https://api.papermc.io/v2/projects/paper/versions/1.8.8/builds/443/downloads/paper-1.8.8-443.jar paper.jar
RUN java -jar paper.jar

FROM adoptopenjdk/openjdk11:alpine-slim

EXPOSE 25565
WORKDIR /opt/spigot
COPY --from=builder /minecraft/cache/patched*.jar /opt/spigot/spigot.jar

RUN apk --update add --no-cache ca-certificates

CMD java -jar -Dcom.mojang.eula.agree=true spigot.jar
