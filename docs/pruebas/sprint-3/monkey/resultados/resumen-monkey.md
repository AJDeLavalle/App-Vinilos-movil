# Monkey (T35)

- Fecha: 2026-11-26 16:43
- Dispositivo: Android SDK built for arm64
- Android: 10 (API 29)
- Serial: emulator-5556
- versionName: 2.0
- Commit: 597c12c
- Backend: http://localhost:3000

Comando por semilla (tras `pm clear`):

```
adb shell monkey -p co.edu.uniandes.vinilos -s <semilla> --throttle 200 --pct-touch 55 --pct-motion 20 --pct-nav 10 --pct-majornav 10 --pct-syskeys 0 --pct-rotation 5 -v -v 5000
```

| Semilla | Eventos pedidos | Eventos inyectados | Resultado | Detalle | Pantallas visitadas | Registro |
|---|---|---|---|---|---|---|
| 9401 | 5000 | 5000 | OK | - | HU01 HU02 HU03 HU04 HU05 HU06 HU08 (7/8) | [monkey-9401.txt](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/monkey-9401.txt), [logcat](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/monkey-9401-logcat.txt) |
| 9402 | 5000 | 5000 | OK | - | HU01 HU02 HU03 HU04 HU05 HU06 HU08 (7/8) | [monkey-9402.txt](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/monkey-9402.txt), [logcat](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/monkey-9402-logcat.txt) |
| 9403 | 5000 | 5000 | OK | - | HU01 HU02 HU03 HU04 HU05 HU06 HU08 (7/8) | [monkey-9403.txt](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/monkey-9403.txt), [logcat](https://raw.githubusercontent.com/wiki/AJDeLavalle/App-Vinilos-movil/pruebas/sprint-3/monkey/monkey-9403-logcat.txt) |

Pantallas visitadas por alguna semilla: HU01 HU02 HU03 HU04 HU05 HU06 HU08 (7/8)

RESULTADO: OK
