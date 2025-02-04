#!/bin/sh

printf "\033[32m Pre install\033[0m\n"

if [ -f "/etc/alpine-release" ]; then
  # Create user if not exists on alpine
  getent group invenium >/dev/null || addgroup -S invenium
  getent passwd invenium >/dev/null || adduser -D -S -G invenium -s /sbin/nologin -h /var/lib/invenium/ invenium
else
  # Create user if not exists RHEL/DEB
  getent group invenium >/dev/null || groupadd -r invenium
  getent passwd invenium >/dev/null || useradd -r -g invenium -s /sbin/nologin invenium -m -d /var/lib/invenium/
fi

# Create directories
mkdir -p "/var/lib/invenium/location-insights${PACKAGE_SUFFIX}"
chown -R "invenium:invenium" "/var/lib/invenium/location-insights${PACKAGE_SUFFIX}"

mkdir -p "/usr/share/invenium/location-insights${PACKAGE_SUFFIX}/brandings"
mkdir -p "/usr/share/invenium/location-insights${PACKAGE_SUFFIX}/lib"
chown -R "invenium:invenium" "/usr/share/invenium/location-insights${PACKAGE_SUFFIX}/brandings"
chown -R "invenium:invenium" "/usr/share/invenium/location-insights${PACKAGE_SUFFIX}/lib"

mkdir -p "/var/log/invenium/location-insights${PACKAGE_SUFFIX}"
chown -R "invenium:invenium" "/var/log/invenium/location-insights${PACKAGE_SUFFIX}"
