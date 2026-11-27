# Monkey y Monkey mejorado (Sprint 3, T35 y T36)

## Qué hay aquí

| Archivo | Para qué |
|---|---|
| `monkey.sh` | T35 (#94). Monkey con 3 semillas fijas × 5000 eventos sobre la app completa. |
| `monkey-mejorado.sh` | T36 (#95). Abre a la fuerza cada una de las 8 pantallas con `adb shell am start` y en cada una lanza una ráfaga de Monkey. |
| `comun.sh` | Funciones que comparten los dos scripts (verificación del entorno, logcat, análisis de la salida). No se ejecuta solo. |
| `resultados/` | Salidas de Monkey, logcat, cobertura y resúmenes de las corridas completas. |

Los dos scripts detectan CRASH y ANR por la salida de Monkey y por logcat, guardan los registros y un
resumen en Markdown, y miden qué pantallas visitó Monkey.

## Requisitos

- Un emulador o dispositivo conectado con el **APK debug** instalado (`./gradlew installDebug`) o su ruta
  como primer argumento del script. Tiene que ser debug porque los dos mecanismos que usan los scripts
  solo existen ahí (`BuildConfig.DEBUG`):
  - el extra `start_route` de `MainActivity`, que abre una pantalla interna al arrancar;
  - la etiqueta de logcat `VinilosNav`, con una línea `destino=<ruta>` por cada pantalla visitada.

  En release y `perfil` el extra se ignora y no se registra nada.
- BackVynils local respondiendo en `http://localhost:3000` (el APK debug apunta a `10.0.2.2:3000`):

  ```
  docker --context desktop-linux start backvynils-db-1 backvynils-web-1
  ```
- `adb` (en el `PATH` o en `~/Library/Android/sdk/platform-tools`) y `curl`. Con varios dispositivos
  conectados, fijar `ANDROID_SERIAL`.

Los scripts funcionan con el bash 3.2 de macOS.

## Cómo reproducir

Corrida completa (unos 55 min cada script), desde la raíz del repositorio:

```
./docs/pruebas/sprint-3/monkey/monkey.sh
./docs/pruebas/sprint-3/monkey/monkey-mejorado.sh
```

Corrida rápida:

```
EVENTOS=500 SEMILLAS=9401 ./docs/pruebas/sprint-3/monkey/monkey.sh
EVENTOS_POR_PANTALLA=60 SEMILLAS=9401 ./docs/pruebas/sprint-3/monkey/monkey-mejorado.sh
```

Para no pisar `resultados/`, usar `RESULTADOS=/otra/carpeta`. Otras variables: `THROTTLE`, `API_URL` y,
en el mejorado, `ALBUM_ID`, `ARTISTA_TIPO` (`BAND` o `MUSICIAN`), `ARTISTA_ID` y `COLECCIONISTA_ID`
(por defecto, los primeros que devuelve el backend).

Reproducir una falla puntual de `monkey.sh` (misma semilla, mismo estado inicial):

```
adb shell pm clear co.edu.uniandes.vinilos
adb shell monkey -p co.edu.uniandes.vinilos -s <semilla> --throttle 200 --pct-touch 55 --pct-motion 20 \
  --pct-nav 10 --pct-majornav 10 --pct-syskeys 0 --pct-rotation 5 -v -v 5000
```

Reproducir una ráfaga de `monkey-mejorado.sh` (la ruta sale de la columna Ruta del resumen):

```
adb shell am start -W -S -a android.intent.action.MAIN -c android.intent.category.LAUNCHER \
  -n co.edu.uniandes.vinilos/.MainActivity --es start_route <ruta>
adb shell monkey -p co.edu.uniandes.vinilos -s <semilla> --throttle 200 --pct-touch 55 --pct-motion 20 \
  --pct-nav 10 --pct-majornav 10 --pct-syskeys 0 --pct-rotation 5 -v -v 625
```

La ráfaga reproduce la pantalla y la semilla, pero no todo el estado: dentro de una semilla las
ráfagas comparten los datos de la app desde el `pm clear` del inicio.

## Parámetros y justificación

| Parámetro | Valor | Por qué |
|---|---|---|
| Eventos | 5000 por semilla | Estrategia de Pruebas 5.1 e issue #94. |
| Throttle | 200 ms | Estrategia 5.1: deja que la UI y las llamadas al backend avancen entre eventos. |
| Mejorado | 8 pantallas × 625 eventos | 8 × 625 = 5000: el mismo presupuesto que el Monkey normal, para comparar la cobertura en igualdad. |
| Semillas | 9401, 9402, 9403 | Fijas para que cada corrida sea reproducible. |
| `--pct-syskeys` | 0 (la Estrategia decía 5) | Las teclas del sistema incluyen ENDCALL, que apaga la pantalla del emulador, y HOME/CALL, que sacan de la app; Monkey dejaría de ejercitarla. Ese 5 % pasa a toques (55 %). |
| `--pct-rotation` | 5 | Prueba los cambios de configuración (recreación de la actividad). |
| `--pct-touch/motion/nav/majornav` | 55/20/10/10 | Mayoría de toques y gestos sobre el contenido; navegación suficiente para cambiar de pantalla. |
| Sin `--ignore-crashes` ni `--ignore-timeouts` | | Se quiere detectar las fallas: Monkey se detiene en el primer CRASH o ANR. |

Antes de cada semilla se hace `pm clear` para empezar desde el mismo estado. Durante la corrida la
rotación automática queda apagada (Monkey la controla) y al terminar se deja en vertical.

## Cómo se detecta una falla y códigos de salida

Cada semilla (o cada ráfaga del mejorado) recibe un resultado:

| Resultado | Cuándo |
|---|---|
| CRASH | `// CRASH: co.edu.uniandes.vinilos` en la salida de Monkey, o `FATAL EXCEPTION` con `Process: co.edu.uniandes.vinilos` en logcat. |
| ANR | `// NOT RESPONDING: co.edu.uniandes.vinilos` en Monkey, o `ANR in co.edu.uniandes.vinilos` en logcat. |
| ERROR | Monkey no pudo correr (`** Error`, `No activities found`, `monkey aborted`, dispositivo no encontrado). |
| ABORTADO | La salida de Monkey terminó sin `// Monkey finished` y sin ninguno de los casos anteriores. |
| SIN_COBERTURA | (`monkey.sh`) Monkey terminó, pero no hay ninguna línea `VinilosNav`: no se puede medir la cobertura. |
| NO_ABRIO | (`monkey-mejorado.sh`) en 15 s no apareció `destino=<patrón>` de la pantalla pedida; esa ráfaga no se corre. |
| OK | Monkey terminó (`// Monkey finished`) sin fallas. |

El resumen trae, en Detalle, la línea `// Long Msg:` o `Reason:` de Monkey o la excepción del logcat.
También deja avisos (no cambian el resultado) si Monkey activó el modo avión (el script lo desactiva)
o si abrió una segunda instancia de la actividad.

| Exit | Significado |
|---|---|
| 0 | Todas las filas OK. |
| 1 | Alguna fila con CRASH, ANR, NO_ABRIO, ABORTADO, ERROR o SIN_COBERTURA. El script sigue con las demás semillas y pantallas. |
| 2 | Falta el dispositivo, el backend o el APK debug. No se corre Monkey. |

Archivos que deja cada script en `resultados/`:

- `monkey.sh`: `monkey-<semilla>.txt` (salida de Monkey), `monkey-<semilla>-logcat.txt` (logcat filtrado
  con `VinilosNav:I AndroidRuntime:E ActivityManager:E`), `cobertura-monkey.txt` y `resumen-monkey.md`.
- `monkey-mejorado.sh`: `mejorado-<semilla>.txt` y `mejorado-<semilla>-logcat.txt` (una sección
  `===== HUxx <ruta> =====` por pantalla), `cobertura-mejorado.txt`, `resumen-mejorado.md` y, si ya existe
  `cobertura-monkey.txt`, `comparacion-cobertura.md`.

## Resultados

Corridas completas del 26-nov en el emulador Vinilos_API29 (Android 10) contra BackVynils local:
3 semillas (9401, 9402 y 9403) × 5000 eventos con cada script. **Sin crashes ni ANR.**

| | Monkey (`resultados/resumen-monkey.md`) | Monkey mejorado (`resultados/resumen-mejorado.md`) |
|---|---|---|
| Eventos inyectados por semilla | 5000 de 5000 | 8 × 625 = 5000 |
| Resultado de las 3 semillas | OK | OK (24 de 24 ráfagas) |
| Pantallas visitadas | 7/8 (nunca HU07 Crear álbum) | 8/8 |

Cobertura por pantalla (`resultados/comparacion-cobertura.md`), semillas que la visitaron:

| Pantalla | Monkey | Monkey mejorado |
|---|---|---|
| HU01 Catálogo de álbumes | 3/3 | 3/3 |
| HU02 Detalle de álbum | 3/3 | 3/3 |
| HU03 Listado de artistas | 3/3 | 3/3 |
| HU04 Detalle de artista | 3/3 | 3/3 |
| HU05 Listado de coleccionistas | 3/3 | 3/3 |
| HU06 Detalle de coleccionista | 3/3 | 3/3 |
| HU07 Crear álbum | 0/3 | 3/3 |
| HU08 Asociar track | 3/3 | 3/3 |

## Defectos encontrados

Ninguno: ninguna corrida tuvo CRASH ni ANR, así que no se abrieron issues de defecto.

## Cómo validar

1. **Ver los videos** en la wiki (semilla 9401, corridas cortas, unos 3 y 4 minutos):
   - [Monkey normal](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/videos/monkey-normal.mp4): 600 eventos; recorre la app y nunca entra a Crear álbum.
   - [Monkey mejorado](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/videos/monkey-mejorado.mp4): 75 eventos por pantalla; salta a cada una de las 8 pantallas, incluida
     Crear álbum.
2. **Revisar los resultados:** las tablas de arriba y los resúmenes de `resultados/`.
3. **Repetirlo** (ver [Cómo reproducir](#cómo-reproducir)):
   ```
   ./gradlew installDebug
   EVENTOS=600 SEMILLAS=9401 docs/pruebas/sprint-3/monkey/monkey.sh
   EVENTOS_POR_PANTALLA=75 SEMILLAS=9401 docs/pruebas/sprint-3/monkey/monkey-mejorado.sh
   ```
   Con la misma semilla se repite la misma secuencia de eventos. Cada script termina con
   `RESULTADO: OK` o `FALLA` y lista las pantallas visitadas.

La técnica, los resultados y los videos también están en la wiki, en *Estrategia de Pruebas — Sprint 3*,
secciones 5.1 a 5.4.

## Limitaciones

- Monkey corre contra el backend real: si llega a guardar un formulario válido (crear álbum, asociar
  track), cambia los datos y las corridas siguientes ya no parten del mismo estado del servidor.
- Se corrió en el emulador Vinilos_API29 (Android 10); la Estrategia pide API 37 y un dispositivo físico.
  Los scripts sirven en otro dispositivo con `ANDROID_SERIAL` y `RESULTADOS`, siempre con el APK debug
  (el release no tiene `start_route`).
- La cobertura cuenta pantallas visitadas, no código ejecutado.
