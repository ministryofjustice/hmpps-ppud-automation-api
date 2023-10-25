#!/bin/bash

set -euo pipefail

./gradlew ktlintFormat
./gradlew clean check
