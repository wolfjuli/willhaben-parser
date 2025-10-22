#!/usr/bin/env bash
set -e

GENERATE_SOURCEMAP=true
if [[ "$BRANCH_NAME" =~ "master" ]]; then
  GENERATE_SOURCEMAP=false
fi

#pnpm install --frozen-lockfile
#pnpm exec playwright install --with-deps
#GENERATE_SOURCEMAP=$GENERATE_SOURCEMAP pnpm test:integration
