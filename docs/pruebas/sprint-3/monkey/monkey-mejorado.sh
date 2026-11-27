#!/usr/bin/env bash
# T36: Monkey mejorado. Para cada semilla abre a la fuerza cada pantalla (HU01 a HU08) con
# `am start --es start_route <ruta>` y lanza ahí una ráfaga de Monkey, para que no se quede en el catálogo.
# Uso: ./docs/pruebas/sprint-3/monkey/monkey-mejorado.sh [ruta/al/app-debug.apk]
# Variables: EVENTOS_POR_PANTALLA (625; 8 x 625 = los 5000 eventos del Monkey normal), SEMILLAS,
# THROTTLE, RESULTADOS, API_URL y los IDs ALBUM_ID, ARTISTA_TIPO (BAND|MUSICIAN), ARTISTA_ID,
# COLECCIONISTA_ID (por defecto, los primeros que devuelve el backend).
# Exit: 0 todo OK; 1 alguna fila con CRASH, ANR, NO_ABRIO, ERROR o ABORTADO; 2 entorno incompleto.
source "$(dirname "$0")/comun.sh"

EVENTOS_POR_PANTALLA=${EVENTOS_POR_PANTALLA:-625}

verificar_entorno "$@"

primer_id() { curl -sf -m 10 "$API_URL/$1" | grep -oE '"id": ?[0-9]+' | head -1 | grep -oE '[0-9]+'; }

ALBUM_ID=${ALBUM_ID:-$(primer_id albums)}
if [ -z "${ARTISTA_ID:-}" ]; then
  ARTISTA_TIPO=BAND
  ARTISTA_ID=$(primer_id bands)
  if [ -z "$ARTISTA_ID" ]; then
    ARTISTA_TIPO=MUSICIAN
    ARTISTA_ID=$(primer_id musicians)
  fi
fi
ARTISTA_TIPO=${ARTISTA_TIPO:-BAND}
COLECCIONISTA_ID=${COLECCIONISTA_ID:-$(primer_id collectors)}
if [ -z "$ALBUM_ID" ] || [ -z "$ARTISTA_ID" ] || [ -z "$COLECCIONISTA_ID" ]; then
  echo "ERROR: el backend no tiene álbumes, artistas o coleccionistas" \
    "(album=$ALBUM_ID artista=$ARTISTA_ID coleccionista=$COLECCIONISTA_ID)." >&2
  exit 2
fi

# Pantalla | ruta enviada en start_route (vacía: arranque normal) | patrón que registra VinilosNav.
PANTALLAS=(HU01 HU02 HU03 HU04 HU05 HU06 HU07 HU08)
RUTAS=("" "albums/$ALBUM_ID" "artists_tab" "artists/$ARTISTA_TIPO/$ARTISTA_ID" "collectors_tab"
  "collectors/$COLECCIONISTA_ID" "create_album" "albums/$ALBUM_ID/tracks/new")
PATRONES=("albums" "albums/{albumId}" "artists" "artists/{artistType}/{artistId}" "collectors"
  "collectors/{collectorId}" "create_album" "albums/{albumId}/tracks/new")

mkdir -p "$RESULTADOS"
preparar_dispositivo

# abrio <logcat> <patrón>: espera hasta 15 s a que VinilosNav registre la pantalla.
abrio() {
  local _
  for _ in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do
    if tr -d '\r' < "$1" | awk -v d="destino=$2" '$NF == d { e = 1 } END { exit !e }'; then return 0; fi
    sleep 1
  done
  return 1
}

COBERTURA="$RESULTADOS/cobertura-mejorado.txt"
: > "$COBERTURA"
FILAS=""
FALLA=0

for s in $SEMILLAS; do
  salida_semilla="$RESULTADOS/mejorado-$s.txt"
  logcat_semilla="$RESULTADOS/mejorado-$s-logcat.txt"
  : > "$salida_semilla"
  : > "$logcat_semilla"
  cobertura_semilla=""
  limpiar_app
  i=0
  while [ $i -lt 8 ]; do
    hu=${PANTALLAS[$i]}
    ruta=${RUTAS[$i]}
    patron=${PATRONES[$i]}
    i=$((i + 1))
    echo "Semilla $s, $hu ($(nombre_hu "$hu")): ${ruta:-arranque normal}"
    tmp_logcat=$(mktemp)
    tmp_salida=$(mktemp)
    TEMPORALES+=("$tmp_logcat" "$tmp_salida" "$tmp_salida.rafaga")
    extra=""
    if [ -n "$ruta" ]; then extra="--es start_route $ruta"; fi

    iniciar_logcat "$tmp_logcat"
    # MAIN + LAUNCHER: el Intent es igual (filterEquals) al que lanza Monkey al empezar, así Monkey trae
    # la tarea al frente en vez de crear otra instancia en el catálogo.
    inicio=$(adb_ shell am start -W -S -a android.intent.action.MAIN -c android.intent.category.LAUNCHER \
      -n "$PKG/.MainActivity" $extra 2>&1)
    detalle=""
    visitadas=""
    if abrio "$tmp_logcat" "$patron"; then
      abrio_txt="sí"
      adb_ shell log -p i -t VinilosNav "rafaga=inicio"
      correr_monkey "$s" "$EVENTOS_POR_PANTALLA" "$tmp_salida"
      sleep 3
      detener_logcat
      IFS='|' read -r resultado inyectados detalle <<EOF
$(analizar "$tmp_salida" "$tmp_logcat")
EOF
      aviso=$(revisar_red)
      if [ -n "$aviso" ]; then detalle="${detalle:+$detalle; }$aviso"; fi
      visitadas=$( {
        echo "$hu"
        awk '/rafaga=inicio/ { f = 1; next } f' "$tmp_logcat" > "$tmp_salida.rafaga"
        hus_de_logcat "$tmp_salida.rafaga" | tr ' ' '\n'
        rm -f "$tmp_salida.rafaga"
      } | grep . | sort -u | tr '\n' ' ' | sed 's/ $//')
    else
      abrio_txt="no"
      detener_logcat
      # Aunque no haya ráfaga, un crash al abrir la pantalla se reporta como CRASH.
      IFS='|' read -r resultado inyectados detalle <<EOF
$(analizar "$tmp_salida" "$tmp_logcat")
EOF
      case "$resultado" in
        CRASH | ANR) ;;
        *) resultado=NO_ABRIO; detalle="no apareció destino=$patron en 15 s" ;;
      esac
      inyectados=0
    fi
    instancias=$(adb_ shell dumpsys activity activities | grep -cE "Hist[[:space:]]+#.*$PKG/\.MainActivity")
    if [ "${instancias:-0}" -gt 1 ]; then
      detalle="${detalle:+$detalle; }AVISO: Monkey abrió otra instancia"
    fi
    [ "$resultado" = OK ] || FALLA=1
    cobertura_semilla="$cobertura_semilla $visitadas"

    {
      echo "===== $hu ${ruta:-(sin start_route)} ====="
      echo "$inicio"
      cat "$tmp_salida"
    } >> "$salida_semilla"
    {
      echo "===== $hu ${ruta:-(sin start_route)} ====="
      cat "$tmp_logcat"
    } >> "$logcat_semilla"
    rm -f "$tmp_logcat" "$tmp_salida" "$tmp_salida.rafaga"
    TEMPORALES=()

    FILAS="$FILAS| $s | $hu | ${ruta:-(arranque normal)} | $abrio_txt | $inyectados | $resultado | ${detalle:--} | $(con_total "$visitadas") |
"
    echo "  $resultado ($inyectados eventos) $visitadas"
  done
  cobertura_semilla=$(echo "$cobertura_semilla" | tr ' ' '\n' | grep . | sort -u | tr '\n' ' ' | sed 's/ $//')
  echo "$s $cobertura_semilla" >> "$COBERTURA"
done

if [ "$FALLA" -eq 0 ]; then FINAL=OK; else FINAL=FALLA; fi

{
  echo "# Monkey mejorado (T36)"
  echo
  encabezado_entorno
  echo "- IDs: álbum $ALBUM_ID, artista $ARTISTA_TIPO/$ARTISTA_ID, coleccionista $COLECCIONISTA_ID"
  echo
  echo "Por semilla (tras \`pm clear\`) y por pantalla:"
  echo
  echo '```'
  echo "adb shell am start -W -S -a android.intent.action.MAIN -c android.intent.category.LAUNCHER" \
    "-n $PKG/.MainActivity --es start_route <ruta>"
  echo "adb shell monkey -p $PKG -s <semilla> --throttle $THROTTLE $PCT -v -v $EVENTOS_POR_PANTALLA"
  echo '```'
  echo
  echo "| Semilla | Pantalla | Ruta | Abrió | Eventos inyectados | Resultado | Detalle | Pantallas visitadas en la ráfaga |"
  echo "|---|---|---|---|---|---|---|---|"
  printf '%s' "$FILAS"
  echo
  echo "Registros: \`mejorado-<semilla>.txt\` y \`mejorado-<semilla>-logcat.txt\`."
  echo
  echo "RESULTADO: $FINAL"
} > "$RESULTADOS/resumen-mejorado.md"

# Comparación de cobertura: semillas que visitaron cada pantalla con cada técnica.
if [ -f "$RESULTADOS/cobertura-monkey.txt" ]; then
  n_normal=$(grep -c . "$RESULTADOS/cobertura-monkey.txt")
  n_mejorado=$(grep -c . "$COBERTURA")
  k_normal=0
  k_mejorado=0
  {
    echo "# Cobertura de pantallas: Monkey normal vs mejorado"
    echo
    echo "Semillas que visitaron cada pantalla."
    echo
    echo "| Pantalla | Monkey normal | Monkey mejorado |"
    echo "|---|---|---|"
    for hu in "${PANTALLAS[@]}"; do
      a=$(grep -c "$hu" "$RESULTADOS/cobertura-monkey.txt")
      b=$(grep -c "$hu" "$COBERTURA")
      [ "$a" -gt 0 ] && k_normal=$((k_normal + 1))
      [ "$b" -gt 0 ] && k_mejorado=$((k_mejorado + 1))
      echo "| $hu $(nombre_hu "$hu") | $a/$n_normal | $b/$n_mejorado |"
    done
    echo "| **Total pantallas** | **$k_normal/8** | **$k_mejorado/8** |"
  } > "$RESULTADOS/comparacion-cobertura.md"
fi

cat "$RESULTADOS/resumen-mejorado.md"
[ -f "$RESULTADOS/comparacion-cobertura.md" ] && cat "$RESULTADOS/comparacion-cobertura.md"
exit "$FALLA"
