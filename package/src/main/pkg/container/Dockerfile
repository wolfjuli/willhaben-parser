FROM --platform=linux/amd64 debian:12-slim
MAINTAINER Julian Wolf <noreply@lykos.solutions>

ARG DEBIAN_FRONTEND=noninteractive
ARG PACKAGE_SUFFIX=""

# Update system an install Java
RUN apt update && \
    apt -y upgrade

# Install application location-insights
COPY container/willhaben-parser*.deb /tmp/willhaben-parser.deb

RUN apt -y install /tmp/willhaben-parser.deb && \
    rm -f /tmp/*.deb && \
    apt clean && \
    mkdir -p "/var/run/lykos/willhaben-parser${packageSuffix}" && \
    chown -R "lykos:lykos" /var/run/invenium/ && \
    ln -s "/usr/share/lykos/run-willhaben-parser${packageSuffix}.sh" /entrypoint.sh

USER lykos
ENTRYPOINT ["/entrypoint.sh"]
