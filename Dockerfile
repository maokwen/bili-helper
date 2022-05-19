# download s6-overlay
FROM lsiobase/alpine:3.13 as builder

WORKDIR /
COPY install-s6-overlay.sh /
RUN set -ex \
	&& chmod +x install-s6-overlay.sh \
	&& bash install-s6-overlay.sh

# bilibili-helper
FROM openjdk:17-jdk-slim
ENV TZ=Asia/Shanghai TASK=1d CRON=false \
    PUID=1026 PGID=100
# copy files
COPY --from=builder s6-overlay/ /
COPY bili-helper.jar  /app/bili-helper.jar
COPY config.json  /app-conf/config.json

# create abc user
RUN apt -y update && apt -y install tzdata cron \
&&  chmod +x /app/bili-helper.jar \
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
# volume
VOLUME [ "/config" ]

ENTRYPOINT [ "/init" ]
