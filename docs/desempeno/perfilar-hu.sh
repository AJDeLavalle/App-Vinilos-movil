#!/usr/bin/env bash
# Perfila cada historia de usuario en el emulador o dispositivo conectado.
# Uso: ./docs/desempeno/perfilar-hu.sh ruta/al/app-release.apk [etiqueta]
#
# Recorre HU01 a HU08 sin escribir en el backend (los formularios se abren y se validan, pero no
# se guardan) y, al terminar cada historia, registra:
#   - memoria: PSS total, heap de Java, heap nativo y gráficos (dumpsys meminfo)
#   - fluidez: fotogramas, % con tirones y percentiles 50/90 (dumpsys gfxinfo, reiniciado por HU)
#   - CPU: milisegundos de CPU que usó la app durante la historia (/proc/<pid>/stat)
# Imprime una fila CSV por historia. Con varios dispositivos conectados, usar ANDROID_SERIAL.
set -u

APK="${1:?Indica la ruta del APK}"
LABEL="${2:-$(basename "$APK" .apk)}"
PKG="co.edu.uniandes.vinilos"
ADB="$(command -v adb || echo "$HOME/Library/Android/sdk/platform-tools/adb")"
adb_() { "$ADB" "$@" | tr -d '\r'; }

read -r W H < <(adb_ shell wm size | awk -F'[ x]' '/Physical/ { print $3, $4 }')
API=$(adb_ shell getprop ro.build.version.sdk)
MODEL=$(adb_ shell getprop ro.product.model)

# Toca el primer nodo cuyo texto o descripción contiene $1. Reintenta mientras la pantalla carga.
tap() {
  local nums=""
  for _ in 1 2 3 4 5 6 7 8 9 10; do
    adb_ shell uiautomator dump /data/local/tmp/ui.xml >/dev/null 2>&1
    nums=$(adb_ shell cat /data/local/tmp/ui.xml \
      | grep -oE "(text|content-desc)=\"[^\"]*$1[^\"]*\"[^>]*bounds=\"\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]\"" \
      | head -1 | sed -E 's/.*bounds="//' | grep -oE '[0-9]+')
    [ -n "$nums" ] && break
    sleep 1
  done
  if [ -z "$nums" ]; then echo "AVISO: no se encontró '$1'" >&2; return 1; fi
  set -- $nums
  adb_ shell input tap $(( ($1 + $3) / 2 )) $(( ($2 + $4) / 2 ))
}
swipe_up() { adb_ shell input swipe $((W / 2)) $((H * 3 / 4)) $((W / 2)) $((H / 4)) 300; sleep 1; }
swipe_down() { adb_ shell input swipe $((W / 2)) $((H / 4)) $((W / 2)) $((H * 3 / 4)) 300; sleep 1; }
back() { adb_ shell input keyevent KEYCODE_BACK; sleep 2; }
# Cierra el teclado si está abierto; si no, no hace nada (un BACK de más cerraría la pantalla).
hide_keyboard() {
  if adb_ shell dumpsys input_method | grep -qE 'mInputShown=true|isInputViewShown=true'; then back; fi
}
# Si la app dejó de estar en primer plano (un BACK de más), la vuelve a abrir.
ensure_app() {
  adb_ shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp' | grep -q "$PKG" \
    || { adb_ shell am start -n "$PKG/.MainActivity" >/dev/null; sleep 4; }
}

# pidof no existe en Android 5.1: se usa ps como respaldo.
pid() { adb_ shell pidof "$PKG" 2>/dev/null | grep -E '^[0-9]+$' || adb_ shell ps | awk -v p="$PKG" '$NF == p { print $2; exit }'; }
# utime + stime de la app, en ticks de reloj (100 por segundo en Android).
cpu_ticks() { adb_ shell cat "/proc/$(pid)/stat" | awk '{ print $14 + $15 }'; }

# Memoria en MB: PSS total, heap de Java y heap nativo asignados, y gráficos.
# Los heaps salen de la columna "Heap Alloc" de la tabla de meminfo, que existe en todas las
# versiones; su posición cambia entre versiones, así que se busca en el encabezado. En el
# emulador de Android 5.1 el PSS sale en 0 (el kernel no lo calcula): queda vacío.
memory() {
  adb_ shell dumpsys meminfo "$PKG" | awk '
    /Alloc/ && !idx { for (i = 1; i <= NF; i++) if ($i == "Alloc") idx = i }
    /^ *Native Heap / && !native { native = $(idx + 2) }
    /^ *Dalvik Heap / && !java { java = $(idx + 2) }
    /^ *TOTAL / && !pss { pss = $2 }
    /Graphics:/ { gfx = $2 }
    END {
      printf "%s,%.1f,%.1f,%s", (pss > 0 ? sprintf("%.1f", pss / 1024) : ""), java / 1024, native / 1024,
        (gfx != "" ? sprintf("%.1f", gfx / 1024) : "")
    }'
}

# Fotogramas, % con tirones, p50 y p90 en ms. En API 22 no hay percentiles: quedan vacíos.
frames() {
  adb_ shell dumpsys gfxinfo "$PKG" | awk '
    /Total frames rendered:/ { total = $4 }
    /Janky frames:/ { jank = $3; gsub(/[()%]/, "", $4); pct = $4 }
    /^50th percentile:/ { p50 = $3 } /^90th percentile:/ { p90 = $3 }
    END { gsub(/ms/, "", p50); gsub(/ms/, "", p90); printf "%s,%s,%s,%s", total, pct, p50, p90 }'
}

START_TICKS=0
begin() { adb_ shell dumpsys gfxinfo "$PKG" reset >/dev/null; START_TICKS=$(cpu_ticks); }
end() {
  local cpu_ms=$(( ($(cpu_ticks) - START_TICKS) * 10 ))
  echo "$LABEL,$MODEL,$API,$1,$(memory),$(frames),$cpu_ms,,"
  ensure_app
}

"$ADB" uninstall "$PKG" >/dev/null 2>&1
# -g (conceder permisos) existe desde Android 6.
if [ "$API" -ge 23 ]; then "$ADB" install -g "$APK" >/dev/null || exit 1; else "$ADB" install "$APK" >/dev/null || exit 1; fi

echo "etiqueta,dispositivo,api,hu,pss_mb,java_heap_mb,native_heap_mb,graficos_mb,fotogramas,tirones_pct,p50_ms,p90_ms,cpu_ms,arranque_frio_ms,arranque_caliente_ms"

# Primer arranque: calienta el backend (Cloud Functions) para que no pese en HU01.
adb_ shell am start -W -n "$PKG/.MainActivity" >/dev/null; sleep 10
adb_ shell am force-stop "$PKG"; sleep 2

# Arranque: TotalTime de am start -W, mediana de 5. El frío incluye crear el proceso.
median() { sort -n | awk '{ v[NR] = $1 } END { print (NR % 2) ? v[(NR + 1) / 2] : (v[NR / 2] + v[NR / 2 + 1]) / 2 }'; }
cold=$(for _ in 1 2 3 4 5; do
  adb_ shell am force-stop "$PKG"; sleep 2
  adb_ shell am start -W -n "$PKG/.MainActivity" | awk '/TotalTime/ { print $2 }'; sleep 3
done | median)
warm=$(for _ in 1 2 3 4 5; do
  adb_ shell input keyevent KEYCODE_HOME; sleep 2
  adb_ shell am start -W -n "$PKG/.MainActivity" | awk '/TotalTime/ { print $2 }'; sleep 1
done | median)
echo "$LABEL,$MODEL,$API,arranque,,,,,,,,,,$cold,$warm"
adb_ shell am force-stop "$PKG"; sleep 2

# HU01 catálogo: desde que el proceso arranca, carga de la lista y desplazamiento.
# (El tiempo de arranque lo mide medir-desempeno.sh.)
adb_ shell am start -W -n "$PKG/.MainActivity" >/dev/null
begin; sleep 5; swipe_up; swipe_up; swipe_down; swipe_down
end "HU01"

# HU02 detalle de álbum.
begin; tap "Buscando"; sleep 4; swipe_up; swipe_up; back
end "HU02"

# HU03 listado de artistas.
begin; tap "Artistas"; sleep 4; swipe_up
end "HU03"

# HU04 detalle de artista.
begin; tap "Queen"; sleep 4; swipe_up; swipe_up; back
end "HU04"

# HU05 listado de coleccionistas.
begin; tap "Coleccionistas"; sleep 4; swipe_up
end "HU05"

# HU06 detalle de coleccionista.
begin; tap "Manolo"; sleep 4; swipe_up; back
end "HU06"

# HU07 crear álbum: abre el formulario, guarda vacío para ver las validaciones y vuelve.
begin; tap "lbumes"; sleep 3; tap "Crear álbum"; sleep 4; swipe_up; tap "Guardar"; sleep 2; hide_keyboard; back
end "HU07"

# HU08 asociar track: desde el detalle abre el formulario, valida vacío y vuelve.
begin; swipe_down; tap "Buscando"; sleep 4; swipe_up; tap "Asociar track"; sleep 4; tap "Asociar track"; sleep 2; hide_keyboard; back; back
end "HU08"

# Fuga de memoria: 5 vueltas por las pantallas de consulta.
for i in 1 2 3 4 5; do
  tap "lbumes"; sleep 2; swipe_down; tap "Buscando"; sleep 3; back
  tap "Artistas"; sleep 2; tap "Queen"; sleep 3; back
  tap "Coleccionistas"; sleep 2; tap "Manolo"; sleep 3; back
  ensure_app
  echo "$LABEL,$MODEL,$API,vuelta-$i,$(memory),,,,,,,"
done
