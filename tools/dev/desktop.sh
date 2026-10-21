#!/bin/bash
# Drives the desktop build in a Pixel 8 Pro shaped window (448x997, 20:9).
#   tools/dev/desktop.sh start            launch the game with the test harness
#   tools/dev/desktop.sh send "tap 224 400" "wait 1500" "shot menu"   run commands, wait until done
#   tools/dev/desktop.sh stop
# Screenshots are written to $CTO_DEV_DIR (default /tmp/cto_dev) as NAME.png.
export CTO_DEV_DIR="${CTO_DEV_DIR:-/tmp/cto_dev}"
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17}"
export PATH="$JAVA_HOME/bin:$PATH"
cd "$(dirname "$0")/../.."
case "$1" in
  start)
    rm -rf "$CTO_DEV_DIR"; mkdir -p "$CTO_DEV_DIR"
    nohup gradle :desktop:run -q > "$CTO_DEV_DIR/run.log" 2>&1 &
    for i in $(seq 1 90); do [ -f "$CTO_DEV_DIR/idle" ] && exit 0; sleep 1; done
    echo "game did not start"; tail -20 "$CTO_DEV_DIR/run.log"; exit 1 ;;
  send)
    shift; rm -f "$CTO_DEV_DIR/idle"
    printf '%s\n' "$@" > "$CTO_DEV_DIR/cmd.tmp"; mv "$CTO_DEV_DIR/cmd.tmp" "$CTO_DEV_DIR/cmd"
    for i in $(seq 1 120); do [ -f "$CTO_DEV_DIR/idle" ] && exit 0; sleep 0.5; done
    echo "timed out"; exit 1 ;;
  stop)
    pkill -f DesktopLauncher; sleep 1 ;;
esac
