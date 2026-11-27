# Auditoría de accesibilidad — Sprint 3 (T33, #92)

Auditoría de las 8 historias de usuario antes y después de los ajustes de accesibilidad de T31 (#90).

| Versión | Código | APK |
|---|---|---|
| Antes | Tag `v3.0-sprint3-avance` (commit `fa2b7a7`) | `vinilos-ANTES-v3.0-sprint3-avance.apk` |
| Después | `develop` con T31 (commit `3514fbd`) | `vinilos-DESPUES-develop-3514fbd.apk` |

Los dos APK son release (backend en Heroku), versión 2.0. Se compilan con `./gradlew assembleRelease` sobre cada commit.

**Estado:** Lint terminado. Accessibility Scanner y TalkBack están pendientes de ejecutarse en un teléfono físico (secciones 2 y 3).

## 1. Android Lint

Comando: `./gradlew lintDebug`, con la configuración del proyecto (`checkAllWarnings = true`, `abortOnError = true`, `lint.xml`). Lint 9.4.1.

| Versión | Hallazgos totales | Categoría Accessibility |
|---|---|---|
| Antes | 0 | 0 |
| Después | 0 | 0 |

Reportes: [`lint/lint-antes.xml`](lint/lint-antes.xml) y [`lint/lint-despues.xml`](lint/lint-despues.xml).

**Alcance de este resultado:** la app no tiene layouts XML; toda la interfaz está en Jetpack Compose. Las reglas de accesibilidad de Lint (`ContentDescription`, `LabelFor`, `ClickableViewAccessibility`, `KeyboardInaccessible`) revisan vistas XML, así que el 0 confirma que no hay problemas detectables por Lint, pero no evalúa la accesibilidad de las pantallas Compose. Esa evaluación la hacen Accessibility Scanner, TalkBack y la prueba `AccessibilitySemanticsTest` de T31.

## 2. Accessibility Scanner

Hacer el recorrido completo primero con el APK **antes** y luego con el **después**, en el mismo teléfono.

1. Instalar [Accessibility Scanner](https://play.google.com/store/apps/details?id=com.google.android.apps.accessibility.auditor) desde Play Store y activarlo en *Ajustes > Accesibilidad*.
2. Instalar el APK: `adb install -r ruta/al/apk` o copiándolo al teléfono.
3. Abrir Vinilos, tocar el botón de Scanner y elegir **Grabar**. Recorrer las 8 pantallas de la tabla y detener la grabación.
4. Guardar la captura de cada pantalla con sus sugerencias en `scanner/antes/` o `scanner/despues/`, con el nombre de la columna "Archivo".

| HU | Pantalla | Archivo | Sugerencias antes | Sugerencias después | Hallazgo principal |
|---|---|---|---|---|---|
| HU01 | Catálogo de álbumes | `01-albums.png` | | | |
| HU02 | Detalle de álbum | `02-album-detail.png` | | | |
| HU03 | Listado de artistas | `03-artists.png` | | | |
| HU04 | Detalle de artista | `04-artist-detail.png` | | | |
| HU05 | Listado de coleccionistas | `05-collectors.png` | | | |
| HU06 | Detalle de coleccionista | `06-collector-detail.png` | | | |
| HU07 | Crear álbum | `07-create-album.png` | | | |
| HU08 | Asociar track | `08-associate-track.png` | | | |

Anotar el tipo de cada sugerencia (etiqueta, tamaño del objetivo táctil, contraste de texto o de imagen, elemento duplicado).

## 3. TalkBack

Activar TalkBack (*Ajustes > Accesibilidad > TalkBack*) y recorrer cada pantalla de dos formas:

- **Navegación lineal:** deslizar a la derecha y a la izquierda, elemento por elemento.
- **Exploración táctil:** arrastrar el dedo sobre la pantalla.

Responder en cada pantalla las preguntas del codelab:

- **P1.** ¿La descripción de cada elemento transmite su propósito?
- **P2.** ¿Se llega a todos los elementos interactivos?
- **P3.** ¿Se anuncian los mensajes temporales (carga, error, validación)?

| HU | Pantalla | P1 antes | P1 después | P2 antes | P2 después | P3 antes | P3 después | Observaciones |
|---|---|---|---|---|---|---|---|---|
| HU01 | Catálogo de álbumes | | | | | | | |
| HU02 | Detalle de álbum | | | | | | | |
| HU03 | Listado de artistas | | | | | | | |
| HU04 | Detalle de artista | | | | | | | |
| HU05 | Listado de coleccionistas | | | | | | | |
| HU06 | Detalle de coleccionista | | | | | | | |
| HU07 | Crear álbum | | | | | | | |
| HU08 | Asociar track | | | | | | | |

Casos que conviene revisar en especial:

- Las tarjetas de las listas se leen como un solo elemento con nombre y artista.
- Los mensajes de carga y error se anuncian sin tocarlos.
- En crear álbum y asociar track, los errores de validación se anuncian al guardar.
- El botón "+" del catálogo y la flecha de volver tienen una descripción hablada.

## 4. Datos del teléfono

| Campo | Valor |
|---|---|
| Modelo | |
| Versión de Android | |
| Versión de TalkBack | |
| Versión de Accessibility Scanner | |
| Fecha | |

## Evidencias relacionadas

- Capturas con fuente al 100 % y al 200 %, antes y después de T31: [`docs/pruebas/sprint-3/accesibilidad/`](../../pruebas/sprint-3/accesibilidad/README.md).
- Prueba automática de semántica: `app/src/androidTest/java/co/edu/uniandes/vinilos/ui/AccessibilitySemanticsTest.kt`.
- Pendiente conocido: con fuente al 200 % la barra inferior parte «Coleccionistas» en dos líneas (#104).
