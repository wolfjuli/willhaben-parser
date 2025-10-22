#!/bin/sh

printf "\033[32m Pre install\033[0m\n"

if [ -f "/etc/alpine-release" ]; then
  # Create user if not exists on alpine
  getent group lykos >/dev/null || addgroup -S lykos
  getent passwd lykos >/dev/null || adduser -D -S -G lykos -s /sbin/nologin -h /var/lib/lykos/ lykos
else
  # Create user if not exists RHEL/DEB
  getent group lykos >/dev/null || groupadd -r lykos
  getent passwd lykos >/dev/null || useradd -r -g lykos -s /sbin/nologin lykos -m -d /var/lib/lykos/
fi

# Create directories
mkdir -p "/var/lib/lykos/willhaben-parser${PACKAGE_SUFFIX}"
chown -R "lykos:lykos" "/var/lib/lykos/willhaben-parser${PACKAGE_SUFFIX}"

mkdir -p "/usr/share/lykos/willhaben-parser${PACKAGE_SUFFIX}/brandings"
mkdir -p "/usr/share/lykos/willhaben-parser${PACKAGE_SUFFIX}/lib"
chown -R "lykos:lykos" "/usr/share/lykos/willhaben-parser${PACKAGE_SUFFIX}/brandings"
chown -R "lykos:lykos" "/usr/share/lykos/willhaben-parser${PACKAGE_SUFFIX}/lib"

mkdir -p "/var/log/lykos/willhaben-parser${PACKAGE_SUFFIX}"
chown -R "lykos:lykos" "/var/log/lykos/willhaben-parser${PACKAGE_SUFFIX}"
