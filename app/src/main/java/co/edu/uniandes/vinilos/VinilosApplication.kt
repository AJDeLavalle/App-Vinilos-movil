package co.edu.uniandes.vinilos

import android.app.Application
import co.edu.uniandes.vinilos.di.AppContainer

class VinilosApplication : Application() {
    val container: AppContainer by lazy { AppContainer() }
}
