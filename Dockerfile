# builder: jre
FROM openjdk:17-alpine as jre-builder

RUN apk add --no-cache binutils

# gradle build
# cp ./build/libs/bili-helper-*-all.jar ./app.jar
# mkdir app
# cd ./app
# unzip ../app.jar
# cd ..
# jdeps --print-module-deps --ignore-missing-deps --recursive --multi-release 17 --class-path="./app/BOOT-INF/lib/*" --module-path="./app/BOOT-INF/lib/*" ./app.jar
# rm -rf ./app

RUN $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules java.base,java.compiler,java.desktop,java.management,java.naming,java.security.jgss,java.sql,jdk.unsupported \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre

# builder: s6-overlay
FROM alpine:latest as s6-builder

WORKDIR /
COPY install-s6-overlay.sh /
RUN set -ex \
	&& chmod +x install-s6-overlay.sh \
	&& bash install-s6-overlay.sh

# bili-helper
FROM alpine:latest

ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV TZ=Asia/Shanghai TASK=1d CRON=false \
    PUID=1026 PGID=100

COPY --from=jre-builder /customjre $JAVA_HOME
COPY --from=s6-builder s6-overlay/ /
COPY app.jar  /app/app.jar
COPY config.json  /app-conf/config.json

# create abc user
RUN apt -y update && apt -y install tzdata cron \
&&  chmod +x /app/app.jar \
&&  useradd -u 1000 -U -d /config -s /bin/false abc \
&&  usermod -G users abc  \
&&  echo "**** cleanup ****" \
&&  apt-get clean \
&&  rm -rf \
    /tmp/* \
    /var/lib/apt/lists/* \
    /var/tmp/*

COPY root/ /

WORKDIR /app

VOLUME [ "/config" ]

ENTRYPOINT [ "/init" ]
