#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [ -z "${JAVA_HOME:-}" ]; then
  if JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null); then
    export JAVA_HOME
  else
    export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
  fi
fi

./gradlew -q :cli:installDist
mkdir -p docs/screenshots

for lang in en es; do
  for cmd in curve simulate compare; do
    args="$cmd --lang $lang"
    if [ "$cmd" = simulate ]; then
      args="$args --parents 10000 --seed 42"
    fi
    { echo "\$ cli $args"; cli/build/install/cli/bin/cli $args; } \
      | freeze --language text --window --padding 20 --font.size 14 --theme dracula \
        --output "docs/screenshots/cli-$cmd-$lang.png"
  done
done
