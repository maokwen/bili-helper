# builder: jre
FROM alpine:latest as jre-builder

WORKDIR /

RUN apk add --no-cache openjdk17 binutils

# gradle build
# cp ./build/libs/bili-helper-*-all.jar ./app.jar
# mkdir app
# cd ./app
# unzip ../app.jar
# cd ..
# jdeps --print-module-deps --ignore-missing-deps --recursive --multi-release 17 --class-path="./app/BOOT-INF/lib/*" --module-path="./app/BOOT-INF/lib/*" ./app.jar
# rm -rf ./app

RUN /usr/bin/jlink \
         --verbose \
         --add-modules java.base,java.compiler,java.desktop,java.management,java.naming,java.security.jgss,java.sql,jdk.unsupported \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre

# bili-helper
FROM alpine:latest

WORKDIR /

ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV PUID=1026 PGID=100

RUN apk add --update busybox-suid

COPY --from=jre-builder /customjre $JAVA_HOME
COPY app.jar /app.jar
COPY docker/ /

RUN chmod +x /run.sh /entry.sh /app.jar
RUN /usr/bin/crontab /crontab.txt

VOLUME [ "/config" ]

ENTRYPOINT [ "/entry.sh" ]
