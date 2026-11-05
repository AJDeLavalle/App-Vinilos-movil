# App-Vinilos-movil

Aplicación móvil Android de Vinilos, desarrollada en Kotlin con Jetpack Compose para el curso de Ingeniería de Software para Aplicaciones Móviles (Uniandes).

## Versiones y tecnologías

| Componente | Versión |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| Android Gradle Plugin | 9.4.1 |
| Gradle | 9.6.0 (wrapper incluido) |
| Jetpack Compose BOM | 2025.06.00 (Compose 1.8) |
| Material 3 | vía Compose BOM |
| Activity Compose | 1.10.1 |
| Core KTX | 1.16.0 |
| Lifecycle (Runtime, ViewModel, Compose) | 2.9.1 |
| Navigation Compose | 2.8.9 |
| Retrofit / Gson converter | 2.11.0 |
| OkHttp (logging, MockWebServer) | 4.12.0 |
| Coil Compose (portadas) | 2.7.0 |
| Kotlin Coroutines Test | 1.10.2 |
| minSdk | 22 (Android 5.1) |
| targetSdk | 37 |
| Java (nivel de compilación) | 11 |
| JDK del daemon de Gradle | 25 |
| applicationId | `co.edu.uniandes.vinilos` |

> **minSdk 22:** las versiones más recientes de Compose, Activity, Lifecycle y Core exigen API 23 o superior. Por eso esas librerías están fijadas en las últimas versiones compatibles con API 21/22; no las actualices sin revisar su `minSdk`.

## Requisitos previos

- [Android Studio](https://developer.android.com/studio) en una versión compatible con AGP 9.4.1 (la más reciente estable).
- JDK 25 para el daemon de Gradle (incluido con Android Studio; ver `gradle/gradle-daemon-jvm.properties`). Gradle lo detecta aunque `JAVA_HOME` apunte a otra versión. El código se compila con nivel Java 11.
- Android SDK con la plataforma API 37 instalada.
- Un emulador Android (API 22 o superior) o un dispositivo físico con la depuración USB activada.
- Git.

## Instalación

1. Clona el repositorio:

   ```bash
   git clone https://github.com/AJDeLavalle/App-Vinilos-movil.git
   cd App-Vinilos-movil
   ```

2. Abre la carpeta raíz `App-Vinilos-movil/` en Android Studio (`File > Open`).
3. Espera a que Gradle sincronice las dependencias. Si Android Studio lo solicita, instala el SDK faltante desde el SDK Manager.

## Instalar el APK sin compilar

Descarga el APK desde la [última release](https://github.com/AJDeLavalle/App-Vinilos-movil/releases/latest) y ábrelo en el teléfono (Android 5.1 o superior; hay que permitir la instalación de apps de origen desconocido), o instálalo con `adb install <archivo>.apk`. Usa el backend desplegado, así que no requiere levantar BackVynils.

## Configuración del backend (BackVynils)

La app consume [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils) (`GET /albums` y `GET /albums/{id}`). La URL depende del tipo de build:

| Build | URL por defecto | Uso |
|---|---|---|
| `debug` | `http://10.0.2.2:3000/` | Desarrollo con BackVynils local; `10.0.2.2` es el `localhost` del computador visto desde el **emulador** |
| `release` | `https://us-central1-vinilos-backend-app.cloudfunctions.net/api/` | APK de entrega: BackVynils desplegado en Firebase, funciona en dispositivos físicos |

Para desarrollar con el backend local:

1. Levanta el backend en tu máquina (puerto 3000), por ejemplo con `docker compose up` desde el repositorio de BackVynils.
2. Ejecuta la app en `debug` (Run ▶ en Android Studio o `./gradlew installDebug`).
3. Para usar otra URL, compila con la propiedad `vinilos.apiUrl` (debug) o `vinilos.apiUrl.release` (release):

   ```bash
   ./gradlew assembleDebug -Pvinilos.apiUrl=http://192.168.0.10:3000/
   ```

   También se pueden fijar en `gradle.properties`. La URL debe terminar en `/`.

> Si el emulador muestra "No pudimos conectarnos con el servidor" con el backend encendido, el firewall de Windows puede estar bloqueando el puerto 3000. Alternativa sin tocar el firewall: `adb reverse tcp:3000 tcp:3000` y compilar con `-Pvinilos.apiUrl=http://localhost:3000/`.

## Uso

### Desde Android Studio

1. Selecciona un emulador o dispositivo en la barra superior.
2. Pulsa **Run ▶** (`Shift + F10`) con la configuración `app`.

### Desde la línea de comandos

Desde la raíz del repositorio:

```bash
# Compilar el APK de depuración (backend local)
./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug

# Compilar el APK de entrega (backend en Firebase), firmado e instalable
./gradlew assembleRelease

# Instalar en el emulador o dispositivo conectado
./gradlew installDebug

# Pruebas unitarias
./gradlew test

# Pruebas instrumentadas (requieren emulador o dispositivo)
./gradlew connectedAndroidTest
```

Los APK quedan en `app/build/outputs/apk/debug/app-debug.apk` y `app/build/outputs/apk/release/app-release.apk`. El APK `release` se firma con la llave de debug para poder instalarlo directamente con `adb install` o desde el teléfono.

Reportes de pruebas: `app/build/reports/tests/testDebugUnitTest/index.html` (unitarias) y `app/build/reports/androidTests/connected/debug/index.html` (instrumentadas).

## Arquitectura (MVVM + Repository + Service Adapter)

```
UI (Compose)  →  ViewModel (StateFlow)  →  AlbumRepository  →  AlbumServiceAdapter  →  Retrofit / BackVynils
```

| Capa | Clases | Responsabilidad |
|---|---|---|
| UI | `AlbumListScreen`, `AlbumDetailScreen` (info, tracks y comentarios), `ArtistDetailScreen` (info, álbumes y premios), `VinilosApp` (NavHost + barra inferior) | Pintar el estado y emitir eventos; no hace llamadas HTTP |
| ViewModel | `AlbumListViewModel`, `AlbumDetailViewModel`, `ArtistDetailViewModel` | Estados `Loading`, `Success`, `Empty`, `Error` y reintento; sobreviven a la rotación |
| Repository | `AlbumRepository`, `ArtistRepository` (interfaces) y sus `Impl` | Punto único de acceso a los datos; se reemplaza por un fake en las pruebas |
| Service Adapter | `AlbumServiceAdapter`, `ArtistServiceAdapter`, `AlbumApiService`, `ArtistApiService`, DTO | Traduce JSON y códigos HTTP (404 → `AlbumNotFoundException` / `ArtistNotFoundException`) al modelo de dominio. `ArtistServiceAdapter` elige `/musicians` o `/bands` y cruza `/prizes`, porque el artista solo trae el id y la fecha de cada premio |
| Dominio | `Album`, `Track`, `Performer`, `Comment`, `Artist`, `ArtistPrize` | Modelo independiente de la UI y de la red |

Las dependencias se crean en `di/AppContainer.kt` (inyección manual) y se exponen desde `VinilosApplication`.

## Pruebas del Sprint 1

| Caso | HU | Prueba | Tipo |
|---|---|---|---|
| CP-01 | HU01 | `AlbumCatalogTest.cp01_conAlbumes_muestraListaConNombreYArtista` | UI instrumentada |
| CP-02 | HU01 | `AlbumCatalogTest.cp02_sinAlbumes_muestraEstadoVacio` | UI instrumentada |
| CP-03 | HU01 | `AlbumCatalogTest.cp03_apiInaccesible_muestraError_yReintentarCargaLaLista` | UI instrumentada |
| CP-04 | HU01 | `AlbumCatalogTest.cp04_respuestaDemorada_muestraIndicadorDeCarga` | UI instrumentada |
| CP-05 | HU02 | `AlbumDetailNavigationTest.cp05_seleccionarAlbum_muestraDetalleDelMismoAlbum` | E2E UI |
| CP-06 | HU02 | `AlbumDetailNavigationTest.cp06_botonAtrasDelSistema_regresaAlCatalogo` (Espresso `pressBack`) | E2E UI |
| CP-07 | HU02 | `AlbumServiceAdapterTest.getAlbum_404_lanzaAlbumNotFound`, `AlbumDetailViewModelTest.idInexistente_estadoErrorNotFound` | Integración / unitaria |
| CP-10 | Ambas | `AlbumDetailNavigationTest.cp10_flujoCatalogoDetalleVolver_conBotonDeLaBarra` | E2E UI |
| CP-11 | HU02 | `AlbumDetailNavigationTest.cp11_detalleMuestraComentarios_oMensajeSiNoHay`, `AlbumServiceAdapterTest.getAlbum_mapeaComentarios_yAcotaLaCalificacion` | E2E UI / integración |

Las pruebas de UI usan un `FakeAlbumRepository` con datos fijos, así que no dependen del backend. Como la interfaz está en Jetpack Compose, las aserciones usan Compose UI Test (`androidx.compose.ui.test`); Espresso se usa para acciones del sistema, como el botón Atrás. CP-08 (rotación) y CP-09 (instalación del APK) se validan manualmente.

Resultados de ejecución y defectos encontrados: [reporte de defectos del Sprint 1](docs/pruebas/sprint-1/defectos.md).

Capturas en el emulador: [catálogo](docs/evidencias/sprint-1/HU01-catalogo.png) y [detalle](docs/evidencias/sprint-1/HU02-detalle.png).

## Pruebas del Sprint 2

| Caso | HU | Prueba | Tipo |
|---|---|---|---|
| CP-12 | HU04 | `ArtistDetailTest.cp12_desdeElAlbum_muestraDatosAlbumesYPremiosDelArtista` | E2E UI |
| CP-13 | HU04 | `ArtistDetailTest.cp13_bandaSinAlbumesNiPremios_muestraMensajesVacios` | UI instrumentada |
| CP-14 | HU04 | `ArtistDetailTest.cp14_artistaInexistente_informaYPermiteVolverAlAlbum`, `ArtistServiceAdapterTest.artista404_lanzaArtistNotFound` | UI instrumentada / integración |
| CP-15 | HU04 | `ArtistDetailTest.cp15_errorDeRed_muestraReintentar_yRecupera` | UI instrumentada |
| CP-16 | HU04 | `ArtistDetailTest.cp16_seleccionarAlbumDelArtista_abreElAlbum_yAtrasRegresa` (Espresso `pressBack`) | E2E UI |

El detalle de artista se abre desde el detalle de un álbum con "Ver artista". La pestaña Artistas sigue pendiente del listado (T14).

Capturas con el backend desplegado: [músico](docs/evidencias/sprint-2/HU04-detalle-musico.png) y [banda](docs/evidencias/sprint-2/HU04-detalle-banda.png). Las dos muestran la imagen por defecto porque las URL de esos artistas en el backend ya no responden.

## Estructura del repositorio

```
App-Vinilos-movil/
├── README.md
├── docs/
│   ├── evidencias/   # Capturas de las entregas
│   └── pruebas/      # Reportes de defectos por sprint
├── app/
│   └── src/
│       ├── debug/           # Manifest solo de debug (permiso de red local)
│       ├── main/java/co/edu/uniandes/vinilos/
│       │   ├── data/        # remote (Retrofit, DTO, Service Adapter) y repository
│       │   ├── domain/      # modelo e interfaz AlbumRepository
│       │   ├── di/          # AppContainer
│       │   └── ui/          # albums, detail, navigation, common, theme
│       ├── test/            # Unitarias e integración HTTP (JUnit, MockWebServer)
│       └── androidTest/     # UI instrumentadas (Compose UI Test + Espresso)
├── gradle/           # Wrapper y catálogo de versiones (libs.versions.toml)
├── build.gradle.kts
└── settings.gradle.kts
```

## Flujo de ramas

- `main`: versión estable.
- `develop`: integración.
- `feature/<tarea>`: desarrollo de cada tarea (por ejemplo `feature/T01-proyecto-base`), que se integra en `develop` mediante Pull Request.
