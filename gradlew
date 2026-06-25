#!/usr/bin/env sh
set -eu

LOCAL_GRADLE="$(find "${HOME}/.gradle/wrapper/dists" -path "*/bin/gradle" -type f 2>/dev/null | sort -V | tail -n 1 || true)"

if [ -n "$LOCAL_GRADLE" ] && [ -x "$LOCAL_GRADLE" ]; then
  exec "$LOCAL_GRADLE" "$@"
fi

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

echo "Gradle is not installed and no wrapper JAR is checked in." >&2
echo "Install Gradle or run this project from Android Studio." >&2
exit 127
