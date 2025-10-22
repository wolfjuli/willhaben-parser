#!/usr/bin/env sh

CONFIG="${APPLICATION_CONFIG:-/etc/lykos/willhaben-parser${PACKAGE_SUFFIX}/application.yml}"

/usr/bin/java ${JAVA_OPTS} \
-jar "/usr/share/lykos/willhaben-parser${PACKAGE_SUFFIX}/willhaben-parser.jar" \
-c "${CONFIG}"
