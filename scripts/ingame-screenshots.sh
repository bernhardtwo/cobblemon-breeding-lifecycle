#!/usr/bin/env bash
# Takes the in-game screenshots of docs/screenshots/INDEX.md with a dev server and the dev client.
# Usage: scripts/ingame-screenshots.sh auto     (all of it: server, client, four screenshots, both closed)
#        scripts/ingame-screenshots.sh server   (dev server with the daycare on, Dev as op, two Charmander given)
#        scripts/ingame-screenshots.sh client   (dev client named Dev that joins localhost:25565)
#        scripts/ingame-screenshots.sh loss     (switches the running server to p1 = 1.0 and reloads)
# Nothing is typed into any window: in auto the dev client's own mod sends the chat commands.
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
CLIENT_LOG=mod/run-client/auto-client.log
SHOTS=(ingame-daycare-status ingame-info ingame-loss ingame-refused)
GRADLE_ONCE=(--no-daemon -Pkotlin.compiler.execution.strategy=in-process)

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
  local pattern=$1 seconds=$2 file=${3:-$LOG}
  for _ in $(seq "$seconds"); do
    if grep -q "$pattern" "$file" 2>/dev/null; then
      return 0
    fi
    sleep 1
  done
  echo "timed out waiting for: $pattern" >&2
  return 1
}

start_server() {
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
  KEEPER=$!
  disown "$KEEPER"
  ./gradlew "$@" :mod:runServer --args="nogui" <"$CONSOLE" >"$LOG" 2>&1 &
  SERVER=$!
  echo "server starting, log: $LOG"
  wait_for 'Done (' 600
}

server() {
  start_server
  trap "kill $KEEPER 2>/dev/null" EXIT
  echo "server ready; run '$0 client' in another terminal"
  wait_for "$PLAYER joined the game" 1800
  send "pokegiveother $PLAYER charmander gender=male level=10"
  send "pokegiveother $PLAYER charmander gender=female level=10"
  echo "gave two Charmander to $PLAYER; docs/ingame-screenshots.md lists the commands"
  wait
}

client() {
  ./gradlew :mod:runClient --args="--username $PLAYER --quickPlayMultiplayer localhost:25565"
}

auto() {
  local out=docs/screenshots shot
  for shot in "${SHOTS[@]}"; do rm -f "$out/$shot.png"; done
  trap stop_all EXIT
  start_server "${GRADLE_ONCE[@]}"
  mkdir -p mod/run-client
  ./gradlew "${GRADLE_ONCE[@]}" :mod:runClient -Pbreedinglifecycle.screenshots=true \
    -Pbreedinglifecycle.screenshots.serverConfig="$PWD/$RUN/config/breeding-lifecycle.json" \
    -Pbreedinglifecycle.screenshots.output="$PWD/$out" \
    --args="--username $PLAYER --quickPlayMultiplayer localhost:25565" >"$CLIENT_LOG" 2>&1 &
  CLIENT=$!
  echo "client starting, log: $CLIENT_LOG"
  local waited=0
  until grep -q '\[screenshots\] \(DONE\|FAILED\)' "$CLIENT_LOG" 2>/dev/null; do
    if ! kill -0 "$CLIENT" 2>/dev/null; then
      echo "client exited without [screenshots] DONE; see $CLIENT_LOG" >&2
      exit 1
    fi
    if [ "$waited" -ge 1200 ]; then
      echo "timed out waiting for: [screenshots] DONE" >&2
      exit 1
    fi
    sleep 1
    waited=$((waited + 1))
  done
  if grep -q '\[screenshots\] FAILED' "$CLIENT_LOG"; then
    grep -o '\[screenshots\] FAILED.*' "$CLIENT_LOG" | head -1 >&2
    exit 1
  fi
  for shot in "${SHOTS[@]}"; do
    [ -f "$out/$shot.png" ] || { echo "missing $out/$shot.png" >&2; exit 1; }
  done
  echo "${#SHOTS[@]} screenshots in $out"
}

# Stops the client and the server of this checkout, then puts the default config back.
stop_all() {
  local status=$?
  if [ -n "${SERVER:-}" ] && kill -0 "$SERVER" 2>/dev/null; then
    send stop
    for _ in $(seq 60); do kill -0 "$SERVER" 2>/dev/null || break; sleep 1; done
  fi
  kill ${CLIENT:-} ${SERVER:-} ${KEEPER:-} 2>/dev/null || true
  pkill -f "fabric.dli.config=$PWD/.gradle/loom-cache/" 2>/dev/null || true
  write_config '{}'
  exit "$status"
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
  auto) auto ;;
  server) server ;;
  client) client ;;
  loss) loss ;;
  *) echo "usage: $0 server|client|loss|auto" >&2; exit 2 ;;
esac
