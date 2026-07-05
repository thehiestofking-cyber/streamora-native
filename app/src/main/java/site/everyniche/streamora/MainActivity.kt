package site.everyniche.streamora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import site.everyniche.streamora.ui.StreamoraApp
import site.everyniche.streamora.ui.theme.BgDark
import site.everyniche.streamora.ui.theme.StreamoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as StreamoraApplication).container
        setContent {
            StreamoraTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BgDark) {
                    StreamoraApp(container = container)
                }
            }
        }
    }
}
