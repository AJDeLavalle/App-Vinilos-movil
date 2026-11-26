#!/usr/bin/env bash
# Funciones comunes de monkey.sh y monkey-mejorado.sh. No se ejecuta solo: los dos scripts lo cargan
# con `source`.
# Requiere adb (en el PATH o en ~/Library/Android/sdk/platform-tools), curl, el APK debug instalado
# (o su ruta como primer argumento) y BackVynils respondiendo en $API_URL.
# Con varios dispositivos conectados, usar ANDROID_SERIAL.
set -u
export MSYS_NO_PATHCONV=1

PKG=co.edu.uniandes.vinilos
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULTADOS=${RESULTADOS:-$DIR/resultados}
API_URL=${API_URL:-http://localhost:3000}
THROTTLE=${THROTTLE:-200}
SEMILLAS=${SEMILLAS:-"9401 9402 9403"}
PCT="--pct-touch 55 --pct-motion 20 --pct-nav 10 --pct-majornav 10 --pct-syskeys 0 --pct-rotation 5"
ADB="$(command -v adb || echo "$HOME/Library/Android/sdk/platform-tools/adb")"
LOGCAT_PID=""
TEMPORALES=""

adb_() { "$ADB" "$@" | tr -d '\r'; }

api_nivel() { adb_ shell getprop ro.build.version.sdk; }

# Criterio de entrada: dispositivo, backend y APK debug. Cualquier fallo termina con exit 2.
verificar_entorno() {
  if [ "$(adb_ get-state 2>/dev/null)" != "device" ]; then
    echo "ERROR: no hay un dispositivo o emulador conectado (adb get-state)." >&2
    exit 2
  fi
  if [ $# -ge 1 ] && [ -n "$1" ]; then
    # -g (conceder permisos) existe desde Android 6.
    if [ "$(api_nivel)" -ge 23 ]; then
      "$ADB" install -r -g "$1" >/dev/null || { echo "ERROR: no se pudo instalar $1" >&2; exit 2; }
    else
      "$ADB" install -r "$1" >/dev/null || { echo "ERROR: no se pudo instalar $1" >&2; exit 2; }
    fi
  fi
  if ! curl -sf -m 5 "$API_URL/albums" >/dev/null; then
    echo "ERROR: el backend no responde en $API_URL/albums." >&2
    echo "Enciéndelo con: docker --context desktop-linux start backvynils-db-1 backvynils-web-1" >&2
    exit 2
  fi
  # start_route y la etiqueta VinilosNav solo existen en el APK debug.
  if ! adb_ shell dumpsys package "$PKG" | grep -q DEBUGGABLE; then
    echo "ERROR: $PKG no está instalada o no es el APK debug (./gradlew installDebug)." >&2
    exit 2
  fi
}

preparar_dispositivo() {
  adb_ shell input keyevent KEYCODE_WAKEUP >/dev/null 2>&1
  adb_ shell wm dismiss-keyguard >/dev/null 2>&1
  adb_ shell svc power stayon true >/dev/null 2>&1
  adb_ shell settings put system accelerometer_rotation 0 >/dev/null 2>&1
  adb_ shell settings put system user_rotation 0 >/dev/null 2>&1
  adb_ shell cmd statusbar collapse >/dev/null 2>&1
  trap restaurar EXIT
  trap 'exit 130' INT TERM
}

# Monkey deja la rotación congelada donde haya quedado: se vuelve a vertical.
restaurar() {
  detener_logcat
  adb_ shell settings put system user_rotation 0 >/dev/null 2>&1
  adb_ shell svc power stayon false >/dev/null 2>&1
  if [ -n "$TEMPORALES" ]; then rm -f $TEMPORALES; fi
}

# Estado limpio de la app antes de cada semilla.
limpiar_app() {
  adb_ shell am force-stop "$PKG" >/dev/null 2>&1
  adb_ shell pm clear "$PKG" >/dev/null 2>&1
  # Desde Android 17 (API 37) pm clear revoca el permiso para llegar al backend local.
  if [ "$(api_nivel)" -ge 37 ]; then
    adb_ shell pm grant "$PKG" android.permission.ACCESS_LOCAL_NETWORK 2>/dev/null || true
  fi
  adb_ shell cmd statusbar collapse >/dev/null 2>&1
}

# Sin tr: $! tiene que ser el PID de adb para poder detenerlo.
iniciar_logcat() {
  adb_ logcat -c
  "$ADB" logcat -v threadtime -s VinilosNav:I AndroidRuntime:E ActivityManager:E > "$1" 2>/dev/null &
  LOGCAT_PID=$!
}

detener_logcat() {
  if [ -n "${LOGCAT_PID:-}" ]; then
    kill "$LOGCAT_PID" 2>/dev/null
    wait "$LOGCAT_PID" 2>/dev/null
    LOGCAT_PID=""
  fi
}

# correr_monkey <semilla> <eventos> <salida>
correr_monkey() {
  adb_ shell monkey -p "$PKG" -s "$1" --throttle "$THROTTLE" $PCT -v -v "$2" > "$3" 2>&1
}

# analizar <salida-monkey> <logcat>: imprime RESULTADO|INYECTADOS|DETALLE.
analizar() {
  local salida=$1 logcat=$2 resultado detalle inyectados
  # Sin ^: Monkey escribe "// CRASH:" por stderr y puede quedar a mitad de una línea de stdout.
  if grep -Eq "// CRASH: $PKG( |$)" "$salida" || grep -Eq "AndroidRuntime.*Process: $PKG," "$logcat"; then
    resultado=CRASH
  elif grep -Eq "// NOT RESPONDING: $PKG( |$)" "$salida" || grep -Fq "ANR in $PKG" "$logcat"; then
    resultado=ANR
  elif grep -Fq '** Error' "$salida" || grep -Fq 'No activities found' "$salida" \
    || grep -Fiq 'monkey aborted' "$salida" || grep -Eq 'device .* not found' "$salida"; then
    resultado=ERROR
  elif grep -Fq '// Monkey finished' "$salida"; then
    resultado=OK
  else
    resultado=ABORTADO
  fi
  inyectados=$(grep -oE 'Events injected: [0-9]+' "$salida" | tail -1 | grep -oE '[0-9]+')
  # La excepción del logcat es la línea que sigue a "Process: <paquete>".
  detalle=$(
    {
      grep -m1 -E '^// Long Msg:|Reason:' "$salida"
      awk -v p="Process: $PKG," 'f && /AndroidRuntime/ { sub(/.*AndroidRuntime: /, ""); print; exit }
        index($0, p) { f = 1 }' "$logcat"
      grep -m1 -E 'FATAL EXCEPTION|Caused by' "$logcat" | sed 's/.*AndroidRuntime: //'
    } | head -1 | cut -c1-120 | sed 's/|/\\|/g'
  )
  echo "$resultado|${inyectados:-0}|$detalle"
}

rutas_visitadas() { grep -oE 'destino=[^[:space:]]+' "$1" | sed 's/destino=//' | sort -u; }

hu_de_ruta() {
  case "$1" in
    albums) echo HU01 ;;
    'albums/{albumId}') echo HU02 ;;
    artists) echo HU03 ;;
    'artists/{artistType}/{artistId}') echo HU04 ;;
    collectors) echo HU05 ;;
    'collectors/{collectorId}') echo HU06 ;;
    create_album) echo HU07 ;;
    'albums/{albumId}/tracks/new') echo HU08 ;;
  esac
}

nombre_hu() {
  case "$1" in
    HU01) echo "Catálogo de álbumes" ;;
    HU02) echo "Detalle de álbum" ;;
    HU03) echo "Listado de artistas" ;;
    HU04) echo "Detalle de artista" ;;
    HU05) echo "Listado de coleccionistas" ;;
    HU06) echo "Detalle de coleccionista" ;;
    HU07) echo "Crear álbum" ;;
    HU08) echo "Asociar track" ;;
  esac
}

# HU visitadas según las líneas destino= del logcat, en una sola línea ordenada.
hus_de_logcat() {
  rutas_visitadas "$1" | while read -r ruta; do hu_de_ruta "$ruta"; done | sort -u | tr '\n' ' ' | sed 's/ $//'
}

# "HU01 HU03 (2/8)"
con_total() {
  local n
  n=$(echo "$1" | wc -w | tr -d ' ')
  echo "${1:--} ($n/8)"
}

encabezado_entorno() {
  echo "- Fecha: $(date '+%Y-%m-%d %H:%M')"
  echo "- Dispositivo: $(adb_ shell getprop ro.product.model)"
  echo "- Android: $(adb_ shell getprop ro.build.version.release) (API $(api_nivel))"
  echo "- Serial: ${ANDROID_SERIAL:-$(adb_ get-serialno)}"
  echo "- versionName: $(adb_ shell dumpsys package "$PKG" | grep -m1 -oE 'versionName=[^ ]+' | sed 's/versionName=//')"
  echo "- Commit: $(git -C "$DIR" rev-parse --short HEAD 2>/dev/null || echo desconocido)"
  echo "- Backend: $API_URL"
}

# Monkey puede bajar el panel de notificaciones y activar el modo avión: se avisa y se revierte.
revisar_red() {
  if [ "$(adb_ shell settings get global airplane_mode_on)" = "1" ]; then
    adb_ shell cmd connectivity airplane-mode disable >/dev/null 2>&1 \
      || adb_ shell settings put global airplane_mode_on 0 >/dev/null 2>&1
    echo "AVISO: Monkey activó el modo avión"
  fi
}
