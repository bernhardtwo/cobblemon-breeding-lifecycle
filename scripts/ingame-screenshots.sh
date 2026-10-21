#!/usr/bin/env bash
# Prepares a dev server and client for the in-game screenshots listed in docs/screenshots/TODO.md.
# Usage: scripts/ingame-screenshots.sh server   (dev server with the daycare on, Dev as op, two Charmander given)
#        scripts/ingame-screenshots.sh client   (dev client named Dev that joins localhost:25565)
#        scripts/ingame-screenshots.sh loss     (switches the running server to p1 = 1.0 and reloads)
# It types nothing into the client: the chat commands are taken by hand, as TODO.md lists them.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ -z "${JAVA_HOME:-}" ]; then
  if JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null); then
    export JAVA_HOME
  elif [ -d /opt/homebrew/opt/openjdk@21 ]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
  fi
fi

RUN=mod/run
PLAYER=Dev
CONSOLE="$RUN/console.fifo"
LOG="$RUN/ingame-server.log"

write_config() {
  mkdir -p "$RUN/config"
  cat >"$RUN/config/breeding-lifecycle.json" <<EOF
{
  "preset": "default",
  "presets": { "default": { "p1": 0.33, "r": 0.764, "maxBreedings": null } },
  "overrides": $1,
  "referenceDaycare": { "enabled": true },
  "eggEventAdapter": { "enabled": false },
  "audit": { "enabled": true }
}
EOF
}

send() {
  printf '%s\n' "$1" >"$CONSOLE"
}

wait_for() {
  local pattern=$1 seconds=$2
  for _ in $(seq "$seconds"); do
    if grep -q "$pattern" "$LOG" 2>/dev/null; then
      return 0
    fi
    sleep 1
  done
  echo "timed out waiting for: $pattern" >&2
  return 1
}

server() {
  mkdir -p "$RUN"
  echo "eula=true" >"$RUN/eula.txt"
  cat >"$RUN/server.properties" <<EOF
online-mode=false
level-type=minecraft\:flat
spawn-protection=0
gamemode=creative
EOF
  printf '[{"uuid":"%s","name":"%s","level":4,"bypassesPlayerLimit":false}]\n' \
    "$(offline_uuid "$PLAYER")" "$PLAYER" >"$RUN/ops.json"
  write_config '{}'
  rm -f "$CONSOLE" "$LOG"
  mkfifo "$CONSOLE"
  # Keeps the FIFO open so the server's console never sees end of input.
  sleep 100000 >"$CONSOLE" &
  local keeper=$!
  trap "kill $keeper 2>/dev/null" EXIT
  ./gradlew :mod:runServer --args="nogui" <"$CONSOLE" >"$LOG" 2>&1 &
  echo "server starting, log: $LOG"
  wait_for 'Done (' 600
  echo "server ready; run '$0 client' in another terminal"
  wait_for "$PLAYER joined the game" 1800
  send "pokegiveother $PLAYER charmander gender=male level=10"
  send "pokegiveother $PLAYER charmander gender=female level=10"
  echo "gave two Charmander to $PLAYER; follow docs/screenshots/TODO.md"
  wait
}

client() {
  ./gradlew :mod:runClient --args="--username $PLAYER --quickPlayMultiplayer localhost:25565"
}

loss() {
  write_config '{ "p1": 1.0 }'
  send "breedinglifecycle reload"
  echo "p1 = 1.0 active; the next /daycare breed loses both parents"
}

offline_uuid() {
  python3 -c 'import hashlib, sys, uuid
h = bytearray(hashlib.md5(("OfflinePlayer:" + sys.argv[1]).encode()).digest())
h[6] = (h[6] & 0x0f) | 0x30
h[8] = (h[8] & 0x3f) | 0x80
print(uuid.UUID(bytes=bytes(h)))' "$1"
}

case "${1:-}" in
  server) server ;;
  client) client ;;
  loss) loss ;;
  *) echo "usage: $0 server|client|loss" >&2; exit 2 ;;
esac
