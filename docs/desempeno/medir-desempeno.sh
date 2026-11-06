#!/usr/bin/env bash
# Mide el desempeño de la app en el emulador o dispositivo conectado.
# Uso: ./docs/desempeno/medir-desempeno.sh ruta/al/app-release.apk [repeticiones]
# Requiere adb en el PATH o en $LOCALAPPDATA/Android/Sdk, y curl.
# Usar el APK release: el de depuración es más lento y no representa la app entregada.
set -u

APK="${1:?Indica la ruta del APK release}"
RUNS="${2:-10}"
PKG="co.edu.uniandes.vinilos"
API="https://us-central1-vinilos-backend-app.cloudfunctions.net/api"
export MSYS_NO_PATHCONV=1

ADB="$(command -v adb || echo "$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe")"
adb_() { "$ADB" "$@" | tr -d '\r'; }

# Mediana y extremos de los números recibidos por stdin.
stats() {
  sort -n | awk '{ v[NR] = $1 } END {
    m = (NR % 2) ? v[(NR + 1) / 2] : (v[NR / 2] + v[NR / 2 + 1]) / 2
    printf "mediana=%s min=%s max=%s n=%d\n", m, v[1], v[NR], NR }'
}

# Toca el primer elemento en pantalla cuyo texto contiene $1.
tap() {
  adb_ shell "uiautomator dump /sdcard/ui.xml" >/dev/null
  local nums
  nums=$(adb_ shell "cat /sdcard/ui.xml" \
    | grep -oE "text=\"[^\"]*$1[^\"]*\"[^>]*bounds=\"\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]\"" \
    | head -1 | sed -E 's/.*bounds="//' | grep -oE '[0-9]+')
  if [ -z "$nums" ]; then echo "AVISO: no se encontró '$1'" >&2; return 1; fi
  set -- $nums
  adb_ shell input tap $(( ($1 + $3) / 2 )) $(( ($2 + $4) / 2 ))
}

launch() { adb_ shell am start -W -n "$PKG/.MainActivity"; }
total_time() { grep -E '^TotalTime' | awk '{ print $2 }'; }
pss() { adb_ shell dumpsys meminfo "$PKG" | awk '/TOTAL PSS:/ { printf "%.1f MB\n", $3 / 1024; exit }'; }

echo "== Entorno"
echo "fecha: $(date '+%Y-%m-%d %H:%M')"
echo "dispositivo: $(adb_ shell getprop ro.product.model), Android $(adb_ shell getprop ro.build.version.release), API $(adb_ shell getprop ro.build.version.sdk)"
echo "apk: $(basename "$APK"), $(awk -v b="$(wc -c < "$APK")" 'BEGIN { printf "%.2f MB", b / 1048576 }')"

"$ADB" uninstall "$PKG" >/dev/null 2>&1
# En Git Bash adb necesita la ruta de Windows, porque MSYS_NO_PATHCONV evita convertirla.
APK_PATH="$APK"
if command -v cygpath >/dev/null; then APK_PATH="$(cygpath -w "$APK")"; fi
"$ADB" install -g "$APK_PATH" >/dev/null || exit 1

echo
echo "== Primer arranque tras instalar (ms)"
launch | total_time
sleep 8

echo
echo "== Arranque en frío (ms)"
for _ in $(seq "$RUNS"); do
  adb_ shell am force-stop "$PKG"; sleep 2
  launch | total_time
  sleep 3
done | stats

echo
echo "== Arranque en caliente (ms)"
for _ in $(seq "$RUNS"); do
  adb_ shell input keyevent KEYCODE_HOME; sleep 2
  launch | total_time
  sleep 1
done | stats

echo
echo "== Memoria por pantalla (PSS)"
adb_ shell am force-stop "$PKG"; sleep 2
launch >/dev/null; sleep 9
echo "catálogo: $(pss)"
tap "Buscando"; sleep 5; echo "detalle de álbum: $(pss)"
adb_ shell input keyevent KEYCODE_BACK; sleep 2
tap "Artistas"; sleep 5; echo "artistas: $(pss)"
tap "Queen"; sleep 5; echo "detalle de artista: $(pss)"
adb_ shell input keyevent KEYCODE_BACK; sleep 2
tap "Coleccionistas"; sleep 5; echo "coleccionistas: $(pss)"
tap "Manolo"; sleep 5; echo "detalle de coleccionista: $(pss)"

echo
echo "== Fuga de memoria: 5 vueltas por todas las pantallas (PSS)"
adb_ shell input keyevent KEYCODE_BACK; sleep 2
for i in 1 2 3 4 5; do
  tap "lbumes"; sleep 2
  tap "Buscando"; sleep 3
  adb_ shell input keyevent KEYCODE_BACK; sleep 2
  tap "Artistas"; sleep 2
  tap "Queen"; sleep 3
  adb_ shell input keyevent KEYCODE_BACK; sleep 2
  tap "Coleccionistas"; sleep 2
  tap "Manolo"; sleep 3
  adb_ shell input keyevent KEYCODE_BACK; sleep 2
  echo "vuelta $i: $(pss)"
done

echo
echo "== Fluidez: una vuelta por todas las pantallas"
adb_ shell dumpsys gfxinfo "$PKG" reset >/dev/null
tap "lbumes"; sleep 2
tap "Buscando"; sleep 3
adb_ shell input swipe 540 1800 540 600 300; sleep 1
adb_ shell input keyevent KEYCODE_BACK; sleep 2
tap "Artistas"; sleep 2
tap "Queen"; sleep 3
adb_ shell input swipe 540 1800 540 600 300; sleep 1
adb_ shell input keyevent KEYCODE_BACK; sleep 2
adb_ shell dumpsys gfxinfo "$PKG" | grep -E '^(Total frames rendered|Janky frames|50th percentile|90th percentile|95th percentile|99th percentile)'

echo
echo "== CPU en reposo (%, 5 muestras)"
sleep 5
for _ in 1 2 3 4 5; do
  adb_ shell "top -b -n 1 -o %CPU,ARGS" | awk -v p="$PKG" 'index($0, p) { print $1; exit }'
  sleep 2
done | stats

echo
echo "== API desde este computador (ms, tamaño en bytes)"
for path in albums albums/100 musicians bands prizes collectors collectors/100/albums; do
  for _ in $(seq 5); do
    curl -s -o /dev/null -m 20 -w '%{time_total}\n' "$API/$path"
  done | awk '{ printf "%d\n", $1 * 1000 }' > "${TMPDIR:-/tmp}/lat.txt"
  size=$(curl -s -m 20 "$API/$path" | wc -c)
  echo "/$path: $(stats < "${TMPDIR:-/tmp}/lat.txt") bytes=$size"
done
rm -f "${TMPDIR:-/tmp}/lat.txt"
