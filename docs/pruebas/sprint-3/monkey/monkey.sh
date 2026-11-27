#!/usr/bin/env bash
# T35: Monkey con semillas fijas sobre el APK debug.
# Uso: ./docs/pruebas/sprint-3/monkey/monkey.sh [ruta/al/app-debug.apk]
# Variables: EVENTOS (5000), SEMILLAS ("9401 9402 9403"), THROTTLE (200), RESULTADOS, API_URL.
# Por cada semilla deja la salida de Monkey, el logcat y una fila en resumen-monkey.md.
# Exit: 0 todo OK; 1 alguna semilla con CRASH, ANR, ERROR, ABORTADO o SIN_COBERTURA; 2 entorno incompleto.
source "$(dirname "$0")/comun.sh"

EVENTOS=${EVENTOS:-5000}

verificar_entorno "$@"
mkdir -p "$RESULTADOS"
preparar_dispositivo

COBERTURA="$RESULTADOS/cobertura-monkey.txt"
: > "$COBERTURA"
FILAS=""
TODAS=""
FALLA=0

for s in $SEMILLAS; do
  echo "Semilla $s: $EVENTOS eventos..."
  salida="$RESULTADOS/monkey-$s.txt"
  logcat="$RESULTADOS/monkey-$s-logcat.txt"
  limpiar_app
  iniciar_logcat "$logcat"
  correr_monkey "$s" "$EVENTOS" "$salida"
  sleep 3
  detener_logcat
  IFS='|' read -r resultado inyectados detalle <<EOF
$(analizar "$salida" "$logcat")
EOF
  aviso=$(revisar_red)
  if [ -n "$aviso" ]; then detalle="${detalle:+$detalle; }$aviso"; fi
  hus=$(hus_de_logcat "$logcat")
  if [ "$resultado" = OK ] && ! grep -q VinilosNav "$logcat"; then
    resultado=SIN_COBERTURA
  fi
  [ "$resultado" = OK ] || FALLA=1
  echo "$s $hus" >> "$COBERTURA"
  TODAS="$TODAS $hus"
  FILAS="$FILAS| $s | $EVENTOS | $inyectados | $resultado | ${detalle:--} | $(con_total "$hus") | [monkey-$s.txt](monkey-$s.txt), [logcat](monkey-$s-logcat.txt) |
"
  echo "  $resultado ($inyectados eventos) $hus"
done

TODAS=$(echo "$TODAS" | tr ' ' '\n' | grep . | sort -u | tr '\n' ' ' | sed 's/ $//')
if [ "$FALLA" -eq 0 ]; then FINAL=OK; else FINAL=FALLA; fi

{
  echo "# Monkey (T35)"
  echo
  encabezado_entorno
  echo
  echo "Comando por semilla (tras \`pm clear\`):"
  echo
  echo '```'
  echo "adb shell monkey -p $PKG -s <semilla> --throttle $THROTTLE $PCT -v -v $EVENTOS"
  echo '```'
  echo
  echo "| Semilla | Eventos pedidos | Eventos inyectados | Resultado | Detalle | Pantallas visitadas | Registro |"
  echo "|---|---|---|---|---|---|---|"
  printf '%s' "$FILAS"
  echo
  echo "Pantallas visitadas por alguna semilla: $(con_total "$TODAS")"
  echo
  echo "RESULTADO: $FINAL"
} > "$RESULTADOS/resumen-monkey.md"

cat "$RESULTADOS/resumen-monkey.md"
exit "$FALLA"
