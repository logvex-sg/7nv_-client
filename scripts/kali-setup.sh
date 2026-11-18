#!/usr/bin/env bash
#
# 7nv_ Client — Kali Linux / Debian build setup.
#
# Installs the JDKs the build needs, verifies the Gradle wrapper, builds the mod
# and prints the JAR path. Pass --prism to also register a Prism Launcher
# instance.
#
# The build itself needs no root; only the package installs do.

set -u

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_PRISM=0
for arg in "$@"; do
    case "$arg" in
        --prism) RUN_PRISM=1 ;;
        -h|--help)
            sed -n '2,12p' "$0" | sed 's/^# \{0,1\}//'
            exit 0
            ;;
    esac
done

log()  { printf '\033[1;34m==>\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m[!]\033[0m %s\n' "$*"; }
die()  { printf '\033[1;31m[x]\033[0m %s\n' "$*" >&2; exit 1; }

# ------------------------------------------------------------------ JDKs

# Loom 1.19.x needs a JDK 25 to run the build tool; the mod targets Java 21.

find_java() {
    # $1 = required feature version
    local want="$1" home v
    for home in /usr/lib/jvm/*; do
        [ -x "$home/bin/java" ] || continue
        v="$("$home/bin/java" -version 2>&1 | head -1 | sed -n 's/.*version "\([0-9]*\).*/\1/p')"
        if [ "$v" = "$want" ]; then
            echo "$home"
            return 0
        fi
    done
    return 1
}

JAVA25_HOME="$(find_java 25 || true)"
JAVA21_HOME="$(find_java 21 || true)"

if [ -z "$JAVA25_HOME" ] || [ -z "$JAVA21_HOME" ]; then
    log "Installing the JDKs the build needs (this asks for sudo)"
    if command -v apt-get >/dev/null 2>&1; then
        sudo apt-get update
        # Debian trixie ships openjdk-21 and openjdk-25; fall back if 25 is absent.
        sudo apt-get install -y openjdk-21-jdk openjdk-25-jdk \
            || sudo apt-get install -y openjdk-21-jdk default-jdk
    else
        warn "apt-get not found. Install a JDK 21 and a JDK 25 by hand, then re-run."
    fi
    JAVA25_HOME="$(find_java 25 || true)"
    JAVA21_HOME="$(find_java 21 || true)"
fi

if [ -n "$JAVA25_HOME" ]; then
    log "JDK 25 for the build tool: $JAVA25_HOME"
else
    warn "No JDK 25 found; the build may fail. Loom 1.19.x needs one to run."
fi
[ -n "$JAVA21_HOME" ] && log "JDK 21 for the mod target: $JAVA21_HOME"

# ------------------------------------------------------------------ build

cd "$REPO_DIR" || die "Could not enter $REPO_DIR"
[ -x ./gradlew ] || die "gradlew is missing from $REPO_DIR"
chmod +x ./gradlew

log "Building the mod (this can take a few minutes on the first run)"
./gradlew clean build || die "Build failed. Fix the errors above and re-run."

JAR="$(ls -1 build/libs/sevennv-*.jar 2>/dev/null | grep -v sources | head -1)"
[ -n "$JAR" ] || die "The build finished but no JAR was produced."
log "Built: $REPO_DIR/$JAR"

# ------------------------------------------------------------------ prism

if [ "$RUN_PRISM" = "1" ]; then
    log "Registering a Prism Launcher instance"
    "$REPO_DIR/scripts/prism-install.sh" || warn "Prism setup failed; see the output above."
fi

cat <<EOF

Done.

  Mod JAR   : $REPO_DIR/$JAR
  Config    : ~/.minecraft/config/sevennv/  (created on first launch)
  ClickGUI  : Right Shift
  HUD editor: Right Control

Copy the JAR and Fabric API for 1.21.11 into your mods/ folder, or run
scripts/prism-install.sh to have that done for you.
EOF
