# Monkey mejorado (T36)

- Fecha: 2026-11-26 17:33
- Dispositivo: Android SDK built for arm64
- Android: 10 (API 29)
- Serial: emulator-5556
- versionName: 2.0
- Commit: 597c12c
- Backend: http://localhost:3000
- IDs: álbum 100, artista BAND/101, coleccionista 100

Por semilla (tras `pm clear`) y por pantalla:

```
adb shell am start -W -S -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n co.edu.uniandes.vinilos/.MainActivity --es start_route <ruta>
adb shell monkey -p co.edu.uniandes.vinilos -s <semilla> --throttle 200 --pct-touch 55 --pct-motion 20 --pct-nav 10 --pct-majornav 10 --pct-syskeys 0 --pct-rotation 5 -v -v 625
```

| Semilla | Pantalla | Ruta | Abrió | Eventos inyectados | Resultado | Detalle | Pantallas visitadas en la ráfaga |
|---|---|---|---|---|---|---|---|
| 9401 | HU01 | (arranque normal) | sí | 625 | OK | - | HU01 HU02 HU03 HU04 HU05 HU06 HU08 (7/8) |
| 9401 | HU02 | albums/100 | sí | 625 | OK | - | HU01 HU02 HU03 HU04 HU05 HU06 HU08 (7/8) |
| 9401 | HU03 | artists_tab | sí | 625 | OK | - | HU02 HU03 HU04 HU05 HU06 HU08 (6/8) |
| 9401 | HU04 | artists/BAND/101 | sí | 625 | OK | - | HU02 HU04 HU05 HU06 HU08 (5/8) |
| 9401 | HU05 | collectors_tab | sí | 625 | OK | - | HU02 HU03 HU04 HU05 HU06 HU08 (6/8) |
| 9401 | HU06 | collectors/100 | sí | 625 | OK | - | HU02 HU03 HU04 HU06 HU08 (5/8) |
| 9401 | HU07 | create_album | sí | 625 | OK | - | HU02 HU03 HU04 HU05 HU06 HU07 HU08 (7/8) |
| 9401 | HU08 | albums/100/tracks/new | sí | 625 | OK | - | HU02 HU03 HU04 HU05 HU06 HU08 (6/8) |
| 9402 | HU01 | (arranque normal) | sí | 625 | OK | - | HU01 HU02 HU03 HU04 HU05 HU06 HU08 (7/8) |
| 9402 | HU02 | albums/100 | sí | 625 | OK | - | HU02 HU03 HU04 HU05 HU06 HU08 (6/8) |
| 9402 | HU03 | artists_tab | sí | 625 | OK | - | HU02 HU03 HU04 HU05 HU06 HU08 (6/8) |
| 9402 | HU04 | artists/BAND/101 | sí | 625 | OK | - | HU02 HU04 HU05 HU06 HU08 (5/8) |
| 9402 | HU05 | collectors_tab | sí | 625 | OK | - | HU02 HU03 HU04 HU05 HU06 HU08 (6/8) |
| 9402 | HU06 | collectors/100 | sí | 625 | OK | - | HU02 HU03 HU04 HU06 HU08 (5/8) |
| 9402 | HU07 | create_album | sí | 625 | OK | - | HU01 HU02 HU03 HU04 HU05 HU06 HU07 HU08 (8/8) |
| 9402 | HU08 | albums/100/tracks/new | sí | 625 | OK | - | HU01 HU02 HU03 HU04 HU05 HU06 HU08 (7/8) |
| 9403 | HU01 | (arranque normal) | sí | 625 | OK | - | HU01 HU02 HU03 HU04 HU08 (5/8) |
| 9403 | HU02 | albums/100 | sí | 625 | OK | AVISO: Monkey activó el modo avión | HU01 HU02 HU03 HU04 HU08 (5/8) |
| 9403 | HU03 | artists_tab | sí | 625 | OK | - | HU02 HU03 HU04 HU08 (4/8) |
| 9403 | HU04 | artists/BAND/101 | sí | 625 | OK | - | HU01 HU02 HU03 HU04 HU08 (5/8) |
| 9403 | HU05 | collectors_tab | sí | 625 | OK | - | HU02 HU03 HU04 HU05 HU06 HU08 (6/8) |
| 9403 | HU06 | collectors/100 | sí | 625 | OK | - | HU01 HU02 HU03 HU04 HU05 HU06 HU08 (7/8) |
| 9403 | HU07 | create_album | sí | 625 | OK | - | HU01 HU02 HU03 HU04 HU07 HU08 (6/8) |
| 9403 | HU08 | albums/100/tracks/new | sí | 625 | OK | - | HU01 HU02 HU03 HU04 HU08 (5/8) |

Registros completos en la wiki: 9401: [salida](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/mejorado-9401.txt) y [logcat](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/mejorado-9401-logcat.txt), 9402: [salida](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/mejorado-9402.txt) y [logcat](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/mejorado-9402-logcat.txt), 9403: [salida](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/mejorado-9403.txt) y [logcat](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/mejorado-9403-logcat.txt).

RESULTADO: OK
