# Resultados de pruebas — Sprint 2

## Pruebas automatizadas

| Nivel | Herramienta | Resultado |
|---|---|---|
| Unitarias e integración HTTP | JUnit + MockWebServer (`./gradlew testDebugUnitTest`) | 100 de 100 |
| UI / E2E | Espresso + Compose UI Test (`./gradlew connectedDebugAndroidTest`) | 43 de 43 en API 22, 29 y 37 |
| Análisis estático | Lint con `checkAllWarnings` (`./gradlew lintDebug`) | 0 advertencias (antes: 36) |

Reportes de Espresso por dispositivo, en formato JUnit XML: [`espresso/`](espresso/). Corresponden a la integración de las 8 HU (punta de la pila #63–#66, 14-nov-2026); tras cada PR de optimización se repitió la suite completa en API 29.

| Emulador | Android | Pruebas | Fallas |
|---|---|---|---|
| Medium Phone | 17 (API 37) | 43 | 0 |
| Pixel 4 | 10 (API 29) | 43 | 0 |
| Pixel 2 | 5.1.1 (API 22) | 43 | 0 |

## Recorrido manual (exploratorio)

Las 8 HU se recorrieron a mano en API 37 contra BackVynils local, con video y capturas: [`docs/evidencias/sprint-2/`](../../evidencias/sprint-2/). En HU07 y HU08 se verificó además que el álbum y el track creados existen en el backend.

## Perfilamiento

[`docs/desempeno/sprint-2/reporte-perfilamiento.md`](../../desempeno/sprint-2/reporte-perfilamiento.md).

## Defectos encontrados

| ID | Issue | Severidad | Estado |
|---|---|---|---|
| DEF-03 | [Guardar dos veces crea el álbum o el track duplicado](https://github.com/AJDeLavalle/App-Vinilos-movil/issues/69) | Alta | Corregido en #68 |
| DEF-04 | [Al asociar un track el detalle vuelve arriba y el track nuevo no se ve](https://github.com/AJDeLavalle/App-Vinilos-movil/issues/70) | Media | Corregido en #68 |
| DEF-05 | [La duración del track acepta minutos de tres cifras](https://github.com/AJDeLavalle/App-Vinilos-movil/issues/71) | Baja | Corregido en #68 |
| DEF-06 | [Track creado sin id se muestra como error](https://github.com/AJDeLavalle/App-Vinilos-movil/issues/72) | Baja | Corregido en #68 |
| DEF-07 | [Imágenes de Wikimedia responden 403 y se piden en cada visita](https://github.com/AJDeLavalle/App-Vinilos-movil/issues/83) | Baja | Corregido en el Sprint 3 para la portada de Queen; 2 URL dependen de los datos del backend ([detalle](../sprint-3/defectos/DEF-07.md)) |

Los defectos del Sprint 1 están en [`docs/pruebas/sprint-1/defectos.md`](../sprint-1/defectos.md).
