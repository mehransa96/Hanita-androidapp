#!/usr/bin/env bash
# Installs each APK on the running emulator, launches it and fails if it crashes on start.
# Usage: smoke-test.sh <apk> <package> [<apk> <package> ...]
set -uo pipefail
adb wait-for-device
status=0
while [ $# -ge 2 ]; do
  apk="$1"; pkg="$2"; shift 2
  echo "=== $pkg ($apk)"
  adb install -r "$apk" || { echo "install failed: $pkg"; status=1; continue; }
  adb logcat -c
  adb shell monkey -p "$pkg" -c android.intent.category.LAUNCHER 1 > /dev/null
  sleep 20
  if ! adb shell pidof "$pkg" > /dev/null; then
    echo "::error::$pkg is not running 20s after launch"; status=1
  fi
  if adb logcat -d -b crash | grep -q "$pkg"; then
    echo "::error::$pkg crashed on start"; adb logcat -d -b crash; status=1
  fi
  adb shell am force-stop "$pkg"
done
exit $status
