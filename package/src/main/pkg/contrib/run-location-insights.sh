#!/usr/bin/env sh

CONFIG="${APPLICATION_CONFIG:-/etc/invenium/location-insights${PACKAGE_SUFFIX}/application.yml}"

/usr/bin/java ${JAVA_OPTS} \
-jar "/usr/share/invenium/location-insights${PACKAGE_SUFFIX}/location-insights.jar" \
-c "${CONFIG}"
