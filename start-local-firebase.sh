#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
project_id=$(node -p 'require("./app/google-services.json").project_info.project_id')

if command -v adb >/dev/null 2>&1; then
    adb_tool=adb
else
    adb_tool="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
fi

if ! "$adb_tool" get-state >/dev/null 2>&1; then
    echo "Abra o emulador Android antes de iniciar o Firebase local." >&2
    exit 1
fi

"$adb_tool" reverse tcp:5001 tcp:5001

if command -v firebase >/dev/null 2>&1; then
    exec firebase emulators:start --only functions --project "$project_id"
fi

exec npx firebase-tools emulators:start --only functions --project "$project_id"
