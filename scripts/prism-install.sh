#!/usr/bin/env bash
#
# Logvex Client — Prism Launcher installer.
#
# Creates (or updates) a "Logvex 1.21.11" Prism instance with Fabric Loader,
# Fabric API and the built Logvex JAR in its mods/ folder.
#
# Usage:
#   ./scripts/prism-install.sh [PRISM_DATA_DIR] [--loader VERSION] [--no-download]
#
# PRISM_DATA_DIR defaults to the first existing path among:
#   $PRISM_DATA, ~/.local/share/PrismLauncher, ~/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher
#
# Run ./gradlew build first so the mod JAR exists.

set -u

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MC_VERSION="1.21.11"
LOADER_VERSION="0.20.1"
INSTANCE_NAME="Logvex ${MC_VERSION}"
FABRIC_API_MAVEN="https://maven.fabricmc.net/net/fabricmc/fabric-loader"
NO_DOWNLOAD=0
PRISM_DIR=""

while [ $# -gt 0 ]; do
    case "$1" in
        --loader) LOADER_VERSION="$2"; shift 2 ;;
        --no-download) NO_DOWNLOAD=1; shift ;;
        -h|--help)
            sed -n '2,15p' "$0" | sed 's/^# \{0,1\}//'
            exit 0
            ;;
        *) PRISM_DIR="$1"; shift ;;
    esac
done

log()  { printf '\033[1;34m==>\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m[!]\033[0m %s\n' "$*"; }
die()  { printf '\033[1;31m[x]\033[0m %s\n' "$*" >&2; exit 1; }

# ------------------------------------------------------------------ locate Prism

if [ -z "$PRISM_DIR" ]; then
    for candidate in \
        "${PRISM_DATA:-}" \
        "$HOME/.local/share/PrismLauncher" \
        "$HOME/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher" \
        "$HOME/Library/Application Support/PrismLauncher" \
        "$APPDATA/PrismLauncher"
    do
        if [ -n "$candidate" ] && [ -d "$candidate" ]; then
            PRISM_DIR="$candidate"
            break
        fi
    done
fi

if [ -z "$PRISM_DIR" ]; then
    die "Could not find a Prism Launcher data directory. Pass it as the first argument."
fi
[ -d "$PRISM_DIR" ] || die "No such directory: $PRISM_DIR"
log "Prism data directory: $PRISM_DIR"

# ------------------------------------------------------------------ mod jar

JAR="$(ls -1 "$REPO_DIR"/build/libs/logvex-*.jar 2>/dev/null | grep -v sources | head -1)"
[ -n "$JAR" ] || die "No Logvex JAR found. Run ./gradlew build first."
log "Mod JAR: $JAR"

# ------------------------------------------------------------------ instance

INSTANCE_DIR="$PRISM_DIR/instances/$INSTANCE_NAME"
mkdir -p "$INSTANCE_DIR/.minecraft/mods" "$INSTANCE_DIR/.minecraft/config/logvex"
log "Instance directory: $INSTANCE_DIR"

cat > "$INSTANCE_DIR/instance.cfg" <<EOF
InstanceType=OneSix
name=$INSTANCE_NAME
iconKey=default
OverrideMemory=true
MaxMemAlloc=4096
MinMemAlloc=1024
OverrideJavaArgs=false
JoinServerOnLaunch=false
EOF

# Fabric Loader component manifest. Prism resolves the loader jar from its
# meta server; the cached requires keep it from re-resolving Minecraft.
cat > "$INSTANCE_DIR/mmc-pack.json" <<EOF
{
  "components": [
    { "uid": "net.minecraft", "version": "$MC_VERSION", "important": true },
    { "uid": "net.fabricmc.fabric-loader", "version": "$LOADER_VERSION" },
    { "uid": "org.lwjgl3", "version": "3.3.3" }
  ],
  "formatVersion": 1
}
EOF

cp -f "$JAR" "$INSTANCE_DIR/.minecraft/mods/$(basename "$JAR")"
log "Copied $(basename "$JAR") into mods/"

# ------------------------------------------------------------------ Fabric API

API_JAR="$INSTANCE_DIR/.minecraft/mods/fabric-api.jar"
if ls "$INSTANCE_DIR/.minecraft/mods/"fabric-api*.jar >/dev/null 2>&1; then
    log "Fabric API already present in mods/"
elif [ "$NO_DOWNLOAD" = "1" ]; then
    warn "Skipping the Fabric API download (--no-download). Add it by hand."
elif command -v curl >/dev/null 2>&1; then
    log "Resolving Fabric API for $MC_VERSION from Modrinth"
    API_URL="$(curl -fsSL \
        "https://api.modrinth.com/v2/project/fabric-api/version?game_versions=%5B%22${MC_VERSION}%22%5D&loaders=%5B%22fabric%22%5D" \
        2>/dev/null \
        | tr '}' '\n' \
        | grep -o '"url":"[^"]*\.jar"' \
        | head -1 \
        | sed 's/"url":"//;s/"$//')"
    if [ -n "$API_URL" ]; then
        if curl -fsSL "$API_URL" -o "$API_JAR"; then
            log "Downloaded Fabric API: $(basename "$API_URL")"
        else
            warn "The Fabric API download failed. Add it manually from modrinth.com/mod/fabric-api"
        fi
    else
        warn "Could not resolve a Fabric API build for $MC_VERSION."
        warn "Download it manually from https://modrinth.com/mod/fabric-api"
    fi
else
    warn "curl not found. Download Fabric API manually into:"
    warn "  $INSTANCE_DIR/.minecraft/mods/"
fi

# ------------------------------------------------------------------ profiles

# Seed the bundled example profiles so they show up in .config profiles.
mkdir -p "$INSTANCE_DIR/.minecraft/config/logvex/profiles"
for profile in default anarchy crystal; do
    src="$REPO_DIR/config-examples/$profile.json"
    [ -f "$src" ] || continue
    cp -f "$src" "$INSTANCE_DIR/.minecraft/config/logvex/profiles/$profile.json"
done
log "Seeded example profiles into config/logvex/profiles/"

cat <<EOF

Prism instance ready.

  Instance : $INSTANCE_NAME
  Path     : $INSTANCE_DIR
  Mods     : $INSTANCE_DIR/.minecraft/mods/

Start the instance from Prism, then press Right Shift in game for the ClickGUI.
EOF
