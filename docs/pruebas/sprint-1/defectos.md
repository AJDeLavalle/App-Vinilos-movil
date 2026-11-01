# Reporte de defectos — Sprint 1 (HU01 y HU02)

Formato tomado de la sección 8 de la [página del Sprint 1 en la Wiki](https://github.com/AJDeLavalle/App-Vinilos-movil/wiki/Sprint-No.1-%E2%80%90-Iteraci%C3%B3n-No.-1-de-Desarrollo#8-reporte-de-defectos).

## Ambiente de ejecución

| Elemento | Valor |
|---|---|
| Fecha | 2026-10-25 |
| Versión probada | Rama `feature/T09-comentarios-album` (`e388182` → `f89f3af`) y release [`v1.0-sprint1`](https://github.com/AJDeLavalle/App-Vinilos-movil/releases/tag/v1.0-sprint1) |
| Dispositivo | Emulador `sdk_gphone64_x86_64`, Android 17 (API 37) |
| Host | Windows 11, Android Studio JBR, Docker Desktop |
| Backend | [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils) en Docker (`postgres:16`), datos de `sql/data.sql` |
| Pruebas automáticas | `./gradlew testDebugUnitTest` y `./gradlew connectedDebugAndroidTest` |

## Resultado de los casos de prueba

| Caso | HU | Tipo | Resultado | Evidencia |
|---|---|---|---|---|
| CP-01 | HU01 | UI instrumentada | ✅ Pasa | `AlbumCatalogTest` |
| CP-02 | HU01 | UI instrumentada | ✅ Pasa | `AlbumCatalogTest` |
| CP-03 | HU01 | UI instrumentada | ✅ Pasa | `AlbumCatalogTest` |
| CP-04 | HU01 | UI instrumentada | ✅ Pasa | `AlbumCatalogTest` |
| CP-05 | HU02 | E2E UI | ❌ Falla en `e388182` ([DEF-01](#def-01)) · ✅ Pasa en `f89f3af` | `AlbumDetailNavigationTest` |
| CP-06 | HU02 | E2E UI | ✅ Pasa | `AlbumDetailNavigationTest` |
| CP-07 | HU02 | Integración / unitaria | ✅ Pasa | `AlbumServiceAdapterTest`, `AlbumDetailViewModelTest` |
| CP-08 | Ambas | Manual (rotación) | ✅ Pasa: catálogo y detalle conservan el estado y no repiten la petición HTTP | [catálogo vertical](../../evidencias/sprint-1/cp08-catalogo-vertical.png), [catálogo horizontal](../../evidencias/sprint-1/cp08-catalogo-horizontal.png), [detalle horizontal](../../evidencias/sprint-1/cp08-detalle-horizontal.png), [detalle vertical](../../evidencias/sprint-1/cp08-detalle-vertical.png) |
| CP-09 | Ambas | Manual (smoke del APK) | ✅ Pasa: se instala y abre sin cierre. Con backend real se observa [DEF-02](#def-02) | [captura](../../evidencias/sprint-1/cp09-release-apk.png) |
| CP-10 | Ambas | E2E UI | ❌ Falla en `e388182` ([DEF-01](#def-01)) · ✅ Pasa en `f89f3af` | `AlbumDetailNavigationTest` |
| CP-11 | HU02 | E2E UI | ❌ Falla en `e388182` ([DEF-01](#def-01)) · ✅ Pasa en `f89f3af` | `AlbumDetailNavigationTest` |

Totales en `f89f3af`: unitarias e integración **15/15**, UI instrumentadas **11/11**.

## Defectos

| ID | HU / Caso | Severidad | Resultado esperado / observado | Issue | Estado |
|---|---|---|---|---|---|
| [DEF-01](#def-01) | HU02 / CP-05, CP-10, CP-11 | Bloqueante | Ver el detalle / la app se cierra | [#52](https://github.com/AJDeLavalle/App-Vinilos-movil/issues/52) | Corregido en PR [#51](https://github.com/AJDeLavalle/App-Vinilos-movil/pull/51), re-probado |
| [DEF-02](#def-02) | HU01, HU02 / CP-01, CP-09 | Alta | Ver el catálogo / "No pudimos conectarnos con el servidor" | [#53](https://github.com/AJDeLavalle/App-Vinilos-movil/issues/53) | Corregido en PR [#51](https://github.com/AJDeLavalle/App-Vinilos-movil/pull/51), re-probado |

### DEF-01

**El detalle del álbum se cierra cuando un track y un comentario tienen el mismo id.**

- **Severidad:** bloqueante. Ocurre con los datos reales: el álbum 100 "Buscando América" tiene un track y un comentario con id 100.
- **Versión:** `e388182`, rama `feature/T09-comentarios-album`.
- **Precondiciones:** backend con `sql/data.sql`, o `FakeAlbumRepository` (track 1 y comentario 1 en el álbum 100).
- **Pasos:**
  1. Abrir la app en el catálogo.
  2. Seleccionar "Buscando América".
- **Esperado:** se muestra el detalle con tracks y comentarios.
- **Observado:** la app se cierra con:
  ```
  java.lang.IllegalArgumentException: Key "1" was already used. If you are using LazyColumn/Row please make sure you provide a unique key for each item.
  ```
- **Causa:** la `LazyColumn` del detalle usaba el `id` como clave tanto para los tracks como para los comentarios.
- **Corrección:** claves con prefijo por sección (`track_${id}`, `comment_${id}`) en `AlbumDetailScreen.kt`, commit `f89f3af`, PR [#51](https://github.com/AJDeLavalle/App-Vinilos-movil/pull/51).
- **Re-prueba:** `connectedDebugAndroidTest` 11/11; CP-08 manual sobre el álbum 100 sin cierre ([captura](../../evidencias/sprint-1/cp08-detalle-vertical.png)).

### DEF-02

**En el emulador API 37 la app no se conecta al backend con la URL por defecto `10.0.2.2`.**

- **Severidad:** alta. Impide usar el APK publicado contra el backend en este emulador; no afecta las pruebas automáticas, que usan un repositorio falso.
- **Versión:** release `v1.0-sprint1`, URL base `http://10.0.2.2:3000/`.
- **Precondiciones:** BackVynils corriendo; `curl http://localhost:3000/albums` responde 200 en el host.
- **Pasos:**
  1. `adb install vinilos-sprint1.apk`.
  2. Abrir la app.
- **Esperado:** el catálogo muestra los álbumes.
- **Observado:** a los 10 s aparece "No pudimos conectarnos con el servidor. Revisa tu conexión." ([captura](../../evidencias/sprint-1/cp09-release-apk.png)):
  ```
  okhttp.OkHttpClient: <-- HTTP FAILED: java.net.SocketTimeoutException: failed to connect to /10.0.2.2 (port 3000) from /10.0.2.16 (port 59596) after 10000ms
  ```
- **Causa:** con `targetSdk = 37`, Android 17 exige el permiso de ejecución `ACCESS_LOCAL_NETWORK` para que una app se conecte a IPs privadas o locales como `10.0.2.2`. Sin él, el sistema bloquea la conexión y OkHttp termina en timeout.
  - Desde `adb shell` la misma dirección responde `HTTP/1.1 200 OK`, incluso forzando la interfaz Wi-Fi (`nc -s 10.0.2.16 10.0.2.2 3000`): la red del emulador sí alcanza el host.
  - Ejecutando con el usuario de la app (`run-as co.edu.uniandes.vinilos`), la app llega a internet (`8.8.8.8:53`) pero no a `10.0.2.2:3000`: el bloqueo es del permiso, no de la red.
  - `adb reverse` con `localhost` funcionaba porque `localhost` no cuenta como red local.
- **Corrección:** PR [#51](https://github.com/AJDeLavalle/App-Vinilos-movil/pull/51), commit `89f457d` en `develop`.
  - El permiso se declara solo en el build de debug (`app/src/debug/AndroidManifest.xml`). Producción usa un servidor público y no lo necesita ni lo pide.
  - `installOptions -g` en `app/build.gradle.kts` lo concede al instalar con `./gradlew installDebug` y en las pruebas instrumentadas.
  - Si el APK de debug se instala de otra forma (por ejemplo, con **Run ▶** de Android Studio), la app muestra el diálogo de permiso una vez y recarga el catálogo al aceptarlo.
  - El APK de release no incluye el permiso, así que no puede usarse contra un backend local en Android 17. Es intencional: release apuntará a la URL de producción. Para probar contra BackVynils local se usa el build de debug.
- **Re-prueba:** emulador Android 17 (API 37) con BackVynils en Docker. Instalación limpia con `installDebug`: permiso concedido sin diálogo y `GET /albums` → `200 OK`. Con **Run ▶**: diálogo una vez, se acepta y `GET /albums` → `200 OK`. `testDebugUnitTest` 15/15 y `connectedDebugAndroidTest` 11/11.

## Observaciones (no son defectos)

- **Compilador:** Kotlin advierte en `ui/navigation/AppNavigation.kt` (líneas 49 y 50) que el destino por defecto de las anotaciones cambiará en versiones futuras ([KT-73255](https://youtrack.jetbrains.com/issue/KT-73255)). Hoy no afecta el comportamiento.
- **Horizontal:** en el catálogo y el detalle, la barra superior y la inferior ocupan cerca de la mitad de la pantalla; el contenido se desplaza bien, pero se ven pocos elementos a la vez. Revisar con diseño (T06).
- **Backend:** el `docker-compose.yml` de BackVynils usa `postgres` sin versión (hoy la 18), cuya imagen cambió la ruta de datos respecto al volumen que monta el compose. De forma preventiva se usó un `docker-compose.override.yml` local con `image: postgres:16`; no se probó la versión 18.

## Regla de cierre

No quedan defectos abiertos en HU01/HU02: DEF-01 y DEF-02 están corregidos en el PR [#51](https://github.com/AJDeLavalle/App-Vinilos-movil/pull/51), ya fusionado en `develop`.
