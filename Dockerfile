FROM maven:3.6.3-jdk-8-slim AS builder

COPY . .
RUN mvn package

FROM felixklauke/paperspigot:1.8.8

# TODO: pull maps from cloud storage

COPY maps/ /opt/minecraft/plugins/Flash/maps
COPY config/ /opt/minecraft/config
COPY --from=builder target/flash.jar /opt/minecraft/plugins
