#!/usr/bin/env bash
set -e

if [[ "$3" == "true" ]]; then
  mkdir -p frontend/dist
  echo "-DskipFrontend provided - skipping (re-)building of frontend"
  exit 0
fi

GENERATE_SOURCEMAP=true
if [[ "$BRANCH_NAME" =~ "master" ]]; then
  GENERATE_SOURCEMAP=false
fi

cd frontend


echo "
REACT_APP_VERSION=$1
REACT_APP_BUILDTIME=$2
" > .env

pnpm install --frozen-lockfile
GENERATE_SOURCEMAP=$GENERATE_SOURCEMAP pnpm build

if [[ $BRANCH_NAME == "master" ]]; then
  # Scripts for master branch stuff
  echo "isMaster"
elif [[ $BRANCH_NAME == "develop" ]]; then
  # Scripts for develop branch stuff
  echo "isDev"
  elif [[ ($BRANCH_NAME =~ ^PR-.* && ($CHANGE_TARGET == "master" || $CHANGE_TARGET == "develop")) || $BRANCH_NAME == "master" || $BRANCH_NAME == "develop" ]]; then
    # Scripts for master or develop or something going to be master or develop

    echo "is PR to or is develop or master"
    pnpm check
elif [[ $BRANCH_INFO != '' ]]; then
  # Scripts for feature branch stuff
  echo "isFeature"
elif [[ $BRANCH_NAME =~ ^PR-.* && $CHANGE_TARGET != "" ]]; then
  # Scripts for PRs from feature-branch to feature-branch stuff
  echo "isPrIntoFeature"
else
  echo "
  Don't know what to do:

  BRANCH_INFO: $BRANCH_INFO
  BRANCH_NAME: $BRANCH_NAME
  CHANGE_BRANCH: $CHANGE_BRANCH
  CHANGE_TARGET: $CHANGE_TARGET
  "
  # exit 1
fi
