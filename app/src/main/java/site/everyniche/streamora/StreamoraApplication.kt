package site.everyniche.streamora

import android.app.Application

class StreamoraApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
