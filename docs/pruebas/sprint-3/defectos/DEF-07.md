# DEF-07

**Portadas e imágenes alojadas en Wikimedia responden 403 y se piden en cada visita.**

- **Severidad:** baja. La app muestra el ícono de reemplazo, pero gasta red pidiendo la imagen en cada visita.
- **Issue:** [#83](https://github.com/AJDeLavalle/App-Vinilos-movil/issues/83).
- **Encontrado en:** el perfilamiento del Sprint 2
  ([reporte, sección 5](../../../desempeno/sprint-2/reporte-perfilamiento.md#5-red-network-inspector-api-29)).
- **Ambiente de la re-prueba:** emulador `Medium_Phone_API_37.0` (Android 17, API 37), build debug y
  [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils) local en Docker con los datos de `sql/data.sql`.
- **Pasos:**
  1. Abrir la app en el catálogo de álbumes.
  2. Abrir el detalle de "A Night at the Opera".
  3. Cerrar y volver a abrir la app.
- **Esperado:** la portada se descarga una vez y se muestra en el catálogo y en el detalle.
- **Observado:** la portada de "A Night at the Opera" (`upload.wikimedia.org/…/Queen_A_Night_At_The_Opera.png`) responde
  403. La app muestra el ícono de reemplazo y vuelve a pedirla en cada visita.
- **Causa:** Coil usaba su propio `OkHttpClient`, que envía `User-Agent: okhttp/4.12.0`, y Wikimedia rechaza ese
  User-Agent. Como Coil no guarda en caché las respuestas de error, la imagen se pedía en cada visita.
- **Corrección:** un `UserAgentInterceptor` en el `OkHttpClient` único de la app, que ahora comparten Retrofit (API) y
  Coil (imágenes). El User-Agent es genérico: `Vinilos/<versión> (Android <versión>; co.edu.uniandes.vinilos)`, sin
  personas, correos ni URL. Archivos: `data/remote/UserAgentInterceptor.kt`, `di/AppContainer.kt` y
  `VinilosApplication.kt`.

## Re-prueba

| | Catálogo | Detalle de "A Night at the Opera" |
|---|---|---|
| Antes | [captura](def-07-antes-catalogo.png): ícono de reemplazo | [captura](def-07-antes-detalle.png): ícono de reemplazo |
| Después | [captura](def-07-despues-catalogo.png): portada real | [captura](def-07-despues-detalle.png): portada real |

- **Una sola descarga en 3 aperturas** ([logcat](def-07-logcat.txt)): instalación limpia, catálogo, detalle de "A Night
  at the Opera" y luego 2 veces `am force-stop` y volver a abrir. Hay una sola línea
  `<-- 200 https://upload.wikimedia.org/wikipedia/en/4/4d/Queen_A_Night_At_The_Opera.png`. El detalle la toma de la
  caché en memoria y las aperturas 2 y 3, de la caché en disco: solo repiten la petición `GET /albums` al backend.
- **Códigos HTTP por User-Agent** ([salida de curl](def-07-curl.txt)):

  | Imagen | `okhttp/4.12.0` | `Vinilos/2.0 (Android 17; co.edu.uniandes.vinilos)` |
  |---|---|---|
  | `upload.wikimedia.org/…/Queen_A_Night_At_The_Opera.png` | 403 | 200 |
  | `upload.wikimedia.org/…/thumb/…/800px-Ruben_Blades_by_Gage_Skidmore.jpg` | 403 | 400 |
  | `pm1.narvii.com/…_hq.jpg` (Queen) | sin respuesta | sin respuesta |
  | `upload.wikimedia.org/…/Ruben_Blades_by_Gage_Skidmore.jpg` (original, sin miniatura) | — | 200 |

- **Pruebas automáticas:** `testDebugUnitTest` 103/103, incluida `UserAgentInterceptorTest` (3/3);
  `connectedDebugAndroidTest` 43/43 en API 37; Lint sin issues.
- **Control del cableado con Coil:** al quitar `.okHttpClient { container.okHttpClient }` de `VinilosApplication`, la
  portada vuelve al ícono de reemplazo y el logcat ya no tiene líneas de `upload.wikimedia.org`.

## Fuera de alcance (datos del backend)

- **Rubén Blades:** la miniatura `…/thumb/…/800px-Ruben_Blades_by_Gage_Skidmore.jpg` responde 400 con cualquier
  User-Agent. El archivo original `…/commons/b/bb/Ruben_Blades_by_Gage_Skidmore.jpg` responde 200.
- **Queen (artista):** `pm1.narvii.com` no responde.
- **Corrección sugerida:** cambiar esas dos URL en `sql/data.sql` de BackVynils. Mientras tanto la app muestra el ícono
  de reemplazo. Que las vuelva a pedir en cada visita es intencional: no se guardan errores en caché, para tomar el dato
  corregido sin limpiar la caché.
- **Política de Wikimedia:** pide idealmente un User-Agent con contacto (URL o correo). Por privacidad no se incluye; el
  formato genérico responde 200 (verificado el 2026-11-26). Si Wikimedia endurece la política, la solución de fondo es
  la de los datos del backend.
