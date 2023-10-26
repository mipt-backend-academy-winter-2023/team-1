#!/bin/bash

SBT_VERSION="$1"

source ~/.sdkman/bin/sdkman-init.sh
sdk install sbt "$SBT_VERSION"
