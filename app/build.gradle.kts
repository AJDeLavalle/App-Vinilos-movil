plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// URL del backend BackVynils según el tipo de build (deben terminar en "/"):
// - debug: BackVynils local; 10.0.2.2 es el localhost del computador visto desde el emulador.
// - release: BackVynils desplegado en Firebase (Cloud Functions), accesible desde dispositivos físicos.
// Se pueden sobrescribir con -Pvinilos.apiUrl=... (debug) y -Pvinilos.apiUrl.release=... o en gradle.properties.
val debugApiUrl = (findProperty("vinilos.apiUrl") as String?) ?: "http://10.0.2.2:3000/"
val releaseApiUrl = (findProperty("vinilos.apiUrl.release") as String?)
    ?: "https://us-central1-vinilos-backend-app.cloudfunctions.net/api/"

android {
    namespace = "co.edu.uniandes.vinilos"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "co.edu.uniandes.vinilos"
        minSdk = 22
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiUrl\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"$releaseApiUrl\"")
            // Firmado con la llave de debug para que el APK se pueda instalar directamente
            // en dispositivos (proyecto académico, no se publica en Play Store).
            signingConfig = signingConfigs.getByName("debug")
            // Sin R8: al optimizar solo androidx/kotlin, OkHttp quedaba apuntando a clases de Kotlin
            // renombradas y la app se cerraba al abrir (IllegalAccessError en OkHttpClient).
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // -g concede los permisos de ejecución con installDebug, así el permiso de red local de debug
    // no muestra el diálogo en desarrollo. Las pruebas instrumentadas lo conceden por su cuenta.
    installation {
        installOptions += "-g"
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
