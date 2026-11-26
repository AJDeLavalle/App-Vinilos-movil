# Chequeos automáticos de accesibilidad — Sprint 3 (T32 / #91)

## 1. Configuración técnica

- **Dependencia:** `androidx.compose.ui:ui-test-junit4-accessibility` declarada en `gradle/libs.versions.toml` y `app/build.gradle.kts` (`androidTestImplementation`), sin fijar versión explícita (resuelta por `androidx.compose:compose-bom:2025.06.00` a Compose UI `1.8.2`, que trae `com.google.android.apps.common.testing.accessibility.framework:accessibility-test-framework:4.1.1`).
- **Configuración del validador (`AccessibilityChecks.kt`):**
  - `AccessibilityValidator().setRunChecksFromRootView(true)` con el preset por defecto `AccessibilityCheckPreset.LATEST`.
  - Lanza `AccessibilityViewCheckException` ante resultados de severidad `ERROR`. Los resultados `WARNING` e `INFO` no hacen fallar las pruebas.
- **Limitación por nivel de API (Compose + ATF 4.1.1):**
  - `AndroidComposeTestRule.enableAccessibilityChecks` requiere `@RequiresApi(34)` y `AccessibilityHierarchyAndroid$BuilderAndroid.build()` en ATF `4.1.1` solo recorre `AccessibilityNodeInfo` (donde Compose expone sus nodos virtuales) cuando `BuildCompat.isAtLeastU()` es verdadero (API ≥ 34). En API < 34, ATF solo inspeccionaría el árbol clásico de `View`, donde Compose aparece como una única vista opaca.
  - Por esta razón, `enableVinilosAccessibilityChecks()` hace un retorno temprano explícito cuando `Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE` (API 34).
  - **Cobertura efectiva:** los chequeos automáticos de ATF se ejecutan y reportan únicamente en **API 37 (`Medium_Phone_API_37.0`)**. En **API 29 (`Vinilos_API29`)** y **API 22 (`Vinilos_API22`)** ATF permanece inactivo (no-op); en esas versiones la suite corre como verificación de compatibilidad funcional y de compilación, sin cobertura de ATF.

## 2. Checks activos en `AccessibilityCheckPreset.LATEST`

El preset `LATEST` de ATF `4.1.1` incluye 14 checks:

1. `ClassNameCheck`
2. `ClickableSpanCheck`
3. `DuplicateClickableBoundsCheck`
4. `DuplicateSpeakableTextCheck`
5. `EditableContentDescCheck`
6. `ImageContrastCheck`
7. `LinkPurposeUnclearCheck`
8. `RedundantDescriptionCheck`
9. `SpeakableTextPresentCheck`
10. `TextContrastCheck`
11. `TextSizeCheck`
12. `TouchTargetSizeCheck`
13. `TraversalOrderCheck`
14. `UnexposedTextCheck`

Notas sobre alcance:
- Únicamente los hallazgos de tipo `ERROR` hacen fallar las pruebas (`WARNING` e `INFO` no detienen la ejecución).
- No se afirma que los 14 checks hayan sido disparados individualmente por mutaciones en esta entrega.
- No se atribuye a ATF la verificación del tamaño táctil mínimo en Compose: Compose expande las cotas del área táctil en el árbol de accesibilidad, por lo que una reducción visual puede no disparar `TouchTargetSizeCheck` en ATF; la altura mínima de `48.dp` en enlaces y controles se valida además de forma explícita en `AccessibilitySemanticsTest`.

## 3. Estrategia de pruebas

1. **Chequeos implícitos en la suite existente (55 pruebas de Compose):**
   - `composeRule.enableVinilosAccessibilityChecks()` se invoca en el helper de montaje de las 7 clases E2E (`AlbumCatalogTest`, `AlbumDetailNavigationTest`, `ArtistDetailTest`, `ArtistListTest`, `AssociateTrackUiTest`, `CollectorTest`, `CreateAlbumTest` — 42 pruebas; `ExampleInstrumentedTest` no usa Compose) y en `@Before` de `AccessibilitySemanticsTest` (13 pruebas).
   - Con esto, ATF evalúa la jerarquía raíz antes de cada acción de Compose (`performClick`, `performTextInput`, `performScrollTo`, `performScrollToNode`, `performSemanticsAction`).
2. **Suite dedicada `AccessibilityChecksTest` (10 pruebas):**
   - Porque `enableAccessibilityChecks()` solo se dispara en acciones, se creó `AccessibilityChecksTest` para recorrer explícitamente las 8 pantallas en todos sus estados (incluyendo estados finales sin acción posterior como `Loading`, `Empty`, `Error` y secciones inferiores tras scroll) invocando `composeRule.onRoot().tryPerformAccessibilityChecks()`.
   - Incluye una prueba sobre `VinilosApp` recorriendo las 3 pestañas de la barra de navegación inferior y una prueba canario (`canario_elementoInteractivoSinEtiqueta_fallaConAtf`, con `Assume.assumeTrue(SDK_INT >= 34)`) que monta un elemento interactivo sin etiqueta y exige `AccessibilityViewCheckException` para detectar un eventual no-op silencioso.

## 4. Matriz de cobertura (`AccessibilityChecksTest`)

Total contado directamente del código de `AccessibilityChecksTest.kt`: **8 pantallas + barra inferior = 38 estados de pantalla + 3 pestañas de navegación (41 estados/vistas; 46 llamadas explícitas a `tryPerformAccessibilityChecks()`, sin contar el canario)**.

| Pantalla / componente | Prueba | Estados verificados explícitamente | Llamadas `tryPerformAccessibilityChecks()` |
|---|---|---|---|
| 1. Catálogo de álbumes (`AlbumListContent`) | `pantalla01_catalogoDeAlbumes_todosLosEstados` | `Loading`, `Empty`, `Error(NETWORK)`, `Success` (4) | 4 |
| 2. Detalle de álbum (`AlbumDetailContent`) | `pantalla02_detalleDeAlbum_todosLosEstados` | `Loading`, `Error(NETWORK)`, `Error(NOT_FOUND)`, `Success` arriba y tras scroll a comentarios (4) | 5 |
| 3. Listado de artistas (`ArtistListContent`) | `pantalla03_listadoDeArtistas_todosLosEstados` | `Loading`, `Empty`, `Error(NETWORK)`, `Success` (4) | 4 |
| 4. Detalle de artista (`ArtistDetailContent`) | `pantalla04_detalleDeArtista_todosLosEstados` | `Loading`, `Error(NETWORK)`, `Error(NOT_FOUND)`, `Success` arriba y tras scroll a premios (4) | 5 |
| 5. Listado de coleccionistas (`CollectorListContent`) | `pantalla05_listadoDeColeccionistas_todosLosEstados` | `Loading`, `Empty`, `Error(NETWORK)`, `Success` (4) | 4 |
| 6. Detalle de coleccionista (`CollectorDetailContent`) | `pantalla06_detalleDeColeccionista_todosLosEstados` | `Loading`, `Error(NETWORK)`, `Error(NOT_FOUND)`, `Success` arriba y tras scroll a colección (4) | 5 |
| 7. Crear álbum (`CreateAlbumContent`) | `pantalla07_crearAlbum_todosLosEstados` | Inicial con artistas cargados, `ArtistOptions.Loading`, `ArtistOptions.Error`, errores en los 7 campos (arriba y tras scroll a guardar), `Submission.Sending`, `SubmitError.NETWORK`, `albumCreated + SubmitError.ARTIST_LINK` (7) | 8 |
| 8. Asociar track (`AssociateTrackContent`) | `pantalla08_asociarTrack_todosLosEstados` | `AlbumLoad.Loading`, `AlbumLoad.Error(NETWORK)`, `AlbumLoad.Error(NOT_FOUND)`, cargado inicial, errores de validación (arriba y tras scroll a guardar), `TrackSubmission.Sending`, `TrackSubmitError.NETWORK` (7) | 8 |
| Barra inferior (`VinilosApp`) | `navegacionBarraInferior_todasLasPestanas` | Pestañas Álbumes, Artistas y Coleccionistas (3) | 3 |
| Canario anti no-op | `canario_elementoInteractivoSinEtiqueta_fallaConAtf` | `Box` clickable sin etiqueta en API ≥ 34 (espera `AccessibilityViewCheckException`) | 1 (negativo) |

## 5. Resultados por nivel de API

El listado completo por clase y caso de prueba está en [chequeos-automaticos-junit.txt](chequeos-automaticos-junit.txt).

| AVD | Nivel de API | Estado de ATF | Comando | Total | Pasadas | Omitidas (`Assume`) | Fallas reales | Duración medida |
|---|---|---|---|---:|---:|---:|---:|---:|
| `Medium_Phone_API_37.0` (corrida 1) | API 37 | **Activo** | `connectedDebugAndroidTest` | 66 | 66 | 0 | 0 | `142.130 s` (XML) / `2m 40s` (Gradle) |
| `Medium_Phone_API_37.0` (corrida 2) | API 37 | **Activo** | `connectedDebugAndroidTest` | 66 | 66 | 0 | 0 | `141.668 s` (XML) / `2m 41s` (Gradle) |
| `Vinilos_API29` | API 29 | Inactivo (no-op, API < 34) | `connectedDebugAndroidTest` | 66 | 65 | 1 | 0 | `73.194 s` (XML) / `1m 19s` (Gradle) |
| `Vinilos_API22` | API 22 | Inactivo (no-op, API < 34) | `scripts/instrument.sh` (`am instrument`) | 66 | 65 | 1 | 0 | `62.434 s` (`am instrument`) |

Notas:
- En API 22 `connectedDebugAndroidTest` se bloquea al recuperar `additionalTestOutput` en AGP, por lo que la validación de compatibilidad usa `am instrument` (`scripts/instrument.sh`).
- En API 29 y API 22 la prueba `canario_elementoInteractivoSinEtiqueta_fallaConAtf` se omite mediante `Assume.assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)` (en el XML de AGP en API 29 se serializa como `AssumptionViolatedException`, pero Gradle concluye en `BUILD SUCCESSFUL`).
- **Comparación de duración en API 37 (antes vs. después):**
  - En `develop` antes de T31 (`43` pruebas en XML de `connectedDebugAndroidTest`): `137.278 s`.
  - En `develop` tras T31 (`56` pruebas en `am instrument`): `167.464 s`.
  - En esta rama con ATF activo (`66` pruebas en XML de `connectedDebugAndroidTest`): `142.130 s` (corrida 1) y `141.668 s` (corrida 2). Dentro de ese total, `AccessibilityChecksTest` tomó `29.646 s` / `33.711 s` y `CreateAlbumTest` tomó `43.155 s` / `41.493 s`.

## 6. Hallazgos encontrados y resolución

No se agregó ninguna supresión (`setSuppressingResultMatcher`). El único hallazgo reportado por ATF en API 37 se corrigió en producción:

| Archivo | Check de ATF | Síntoma medido en API 37 | Causa técnica | Resolución |
|---|---|---|---|---|
| `app/src/main/java/co/edu/uniandes/vinilos/ui/create/CreateAlbumScreen.kt` | `SpeakableTextPresentCheck` | En la primera corrida fallaron 9/10 pruebas de `CreateAlbumTest` (`cp21`–`cp29`) sobre un `EditText` en el borde inferior (`[42,1568][1038,1580]`, `[42,1549][1038,1580]`, `[42,1530][1038,1561]`) con `children=0`. | `.imePadding()` estaba antes de `.verticalScroll(rememberScrollState())`, reduciendo el viewport del `ScrollView` al abrirse el teclado virtual y recortando el borde superior (`12–31 px`) del siguiente `OutlinedTextField` mientras su hijo `Text(label)` quedaba fuera del área visible. Sin el hijo visible, el `EditText` se exponía sin etiqueta enunciable. | Se movió `.imePadding()` dentro de `.verticalScroll(rememberScrollState())` (`.verticalScroll(rememberScrollState()).imePadding()`), conservando el margen desplazable sobre el teclado y evitando recortar campos sobre su etiqueta. Tras el cambio, las 10/10 pruebas de `CreateAlbumTest` y las 66/66 pruebas totales pasaron en dos corridas consecutivas en API 37 sin supresiones. |

## 7. Verificación por mutaciones

Cada mutación se aplicó sobre el código ya confirmado en verde, se midió de forma aislada y se revirtió inmediatamente (`git status` limpio):

| # | Mutación aplicada | AVD y prueba ejecutada | Resultado medido | Evidencia |
|---|---|---|---|---|
| M1 | En `ui/common/StateViews.kt` (`ErrorView`), eliminar `Text(actionLabel)` dentro del `Button` de reintento | API 37: `AccessibilityChecksTest#pantalla05_listadoDeColeccionistas_todosLosEstados` | **FALLA** (`exit=1`) | `AccessibilityViewCheckException` — `SpeakableTextPresentCheck` en `[464,1299][616,1425]` (estado de error sin acción de clic previa). |
| M2 | En `ui/albums/AlbumListScreen.kt`, cambiar `contentDescription` del `Icon` en `FloatingActionButton` a `null` | API 37: `AccessibilityChecksTest#pantalla01_catalogoDeAlbumes_todosLosEstados` | **FALLA** (`exit=1`) | `AccessibilityViewCheckException` — `SpeakableTextPresentCheck` en `[891,2148][1038,2295]`. |
| M2 (E2E) | Misma mutación en `AlbumListScreen.kt` | API 37: `AlbumCatalogTest#barraInferior_pestanaColeccionistas_yRegresoAAlbumes` | **FALLA** (`exit=1`) | `AccessibilityViewCheckException` — `SpeakableTextPresentCheck` en `[891,1938][1038,2085]` (confirma protección sobre E2E existentes). |
| M2 (no-op) | Misma mutación en `AlbumListScreen.kt` | API 29: `AccessibilityChecksTest#pantalla01_catalogoDeAlbumes_todosLosEstados` | **PASA** (`exit=0`) | Confirma empíricamente que ATF es no-op en API 29 (API < 34). |
| M3 | En `ui/common/VinilosTopBar.kt`, cambiar `contentDescription` del `Icon` de volver a `null` | API 37: `AccessibilityChecksTest#pantalla07_crearAlbum_todosLosEstados` | **FALLA** (`exit=1`) | `AccessibilityViewCheckException` — `SpeakableTextPresentCheck` en `[12,85][138,211]` (formulario sin clic previo al chequeo explícito). |
