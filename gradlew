#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="8.7"
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
DIST_DIR="$BASE_DIR/.gradle-dist"
GRADLE_HOME="$DIST_DIR/gradle-$GRADLE_VERSION"
ZIP_FILE="$DIST_DIR/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$DIST_DIR"
  echo "Baixando Gradle $GRADLE_VERSION..."
  curl -L --fail --retry 3 --retry-delay 2 \
    "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" \
    -o "$ZIP_FILE"
  unzip -q -o "$ZIP_FILE" -d "$DIST_DIR"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
