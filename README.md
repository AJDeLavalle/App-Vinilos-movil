# App-Vinilos-movil

Aplicación móvil Android de Vinilos, desarrollada en Kotlin con Jetpack Compose para el curso de Ingeniería de Software para Aplicaciones Móviles (Uniandes).

## Versiones y tecnologías

| Componente | Versión |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| Android Gradle Plugin | 9.4.1 |
| Gradle | 9.6.0 (wrapper incluido) |
| Jetpack Compose BOM | 2026.02.01 |
| Material 3 | vía Compose BOM |
| Activity Compose | 1.13.0 |
| Lifecycle Runtime KTX | 2.11.0 |
| minSdk | 26 (Android 8.0) |
| targetSdk | 37 |
| Java | 11 |
| applicationId | `co.edu.uniandes.vinilos` |

## Requisitos previos

- [Android Studio](https://developer.android.com/studio) en una versión compatible con AGP 9.4.1 (la más reciente estable).
- JDK 11 o superior (incluido con Android Studio).
- Android SDK con la plataforma API 37 instalada.
- Un emulador Android (API 26 o superior) o un dispositivo físico con la depuración USB activada.
- Git.

## Instalación

1. Clona el repositorio:

   ```bash
   git clone https://github.com/AJDeLavalle/App-Vinilos-movil.git
   cd App-Vinilos-movil
   ```

2. Abre la carpeta `Vinilos/` en Android Studio (`File > Open`).
3. Espera a que Gradle sincronice las dependencias. Si Android Studio lo solicita, instala el SDK faltante desde el SDK Manager.

## Uso

### Desde Android Studio

1. Selecciona un emulador o dispositivo en la barra superior.
2. Pulsa **Run ▶** (`Shift + F10`) con la configuración `app`.

### Desde la línea de comandos

Ubícate en la carpeta `Vinilos/`:

```bash
# Compilar el APK de depuración
./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug

# Instalar en el emulador o dispositivo conectado
./gradlew installDebug

# Pruebas unitarias
./gradlew test

# Pruebas instrumentadas (requieren emulador o dispositivo)
./gradlew connectedAndroidTest
```

El APK generado queda en `Vinilos/app/build/outputs/apk/debug/`.

## Estructura del repositorio

```
App-Vinilos-movil/
├── README.md
└── Vinilos/          # Proyecto Android (abrir esta carpeta en Android Studio)
    ├── app/          # Módulo de la aplicación
    ├── gradle/       # Wrapper y catálogo de versiones (libs.versions.toml)
    └── build.gradle.kts
```

## Flujo de ramas

- `main`: versión estable.
- `develop`: integración.
- `feature/<tarea>`: desarrollo de cada tarea (por ejemplo `feature/T01-proyecto-base`), que se integra en `develop` mediante Pull Request.
