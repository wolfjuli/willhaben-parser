FROM --platform=linux/amd64 debian:12-slim
MAINTAINER Invenium Data Insights Gmbh <noreply@invenium.io>

ARG DEBIAN_FRONTEND=noninteractive
ARG PACKAGE_SUFFIX=""

# Update system an install Java
RUN apt update && \
    apt -y upgrade

# Install application location-insights
COPY container/invenium-location-insights*.deb /tmp/invenium-location-insights.deb

RUN apt -y install /tmp/invenium-location-insights.deb && \
    rm -f /tmp/*.deb && \
    apt clean && \
    mkdir -p "/var/run/invenium/location-insights${PACKAGE_SUFFIX}" && \
    chown -R "invenium:invenium" /var/run/invenium/ && \
    ln -s "/usr/share/invenium/run-location-insights${PACKAGE_SUFFIX}.sh" /entrypoint.sh

USER invenium
ENTRYPOINT ["/entrypoint.sh"]
