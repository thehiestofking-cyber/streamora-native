package site.everyniche.streamora.ui.screens

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import site.everyniche.streamora.StreamoraApplication
import site.everyniche.streamora.data.model.ContentItem
import site.everyniche.streamora.ui.theme.StreamoraTheme

/**
 * Dedicated full-screen activity for video playback. Kept separate from
 * MainActivity so the system status/nav bars, orientation, and lifecycle can
 * be managed independently of the rest of the app (like a real player app).
 */
class PlayerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_SUBJECT_ID = "subjectId"
        const val EXTRA_DETAIL_PATH = "detailPath"
        const val EXTRA_BACKDROP = "backdrop"
        const val EXTRA_SUBJECT_TYPE = "subjectType"

        fun intentFor(activity: ComponentActivity, item: ContentItem): Intent =
            Intent(activity, PlayerActivity::class.java).apply {
                putExtra(EXTRA_TITLE, item.title)
                putExtra(EXTRA_SUBJECT_ID, item.subjectId)
                putExtra(EXTRA_DETAIL_PATH, item.detailPath)
                putExtra(EXTRA_BACKDROP, item.backdrop ?: item.poster)
                putExtra(EXTRA_SUBJECT_TYPE, item.subjectType ?: 1)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        val item = ContentItem(
            title = intent.getStringExtra(EXTRA_TITLE) ?: "",
            subjectId = intent.getStringExtra(EXTRA_SUBJECT_ID),
            detailPath = intent.getStringExtra(EXTRA_DETAIL_PATH),
            backdrop = intent.getStringExtra(EXTRA_BACKDROP),
            subjectType = intent.getIntExtra(EXTRA_SUBJECT_TYPE, 1),
        )
        val container = (application as StreamoraApplication).container

        setContent {
            StreamoraTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.ui.graphics.Color.Black) {
                    PlayerScreen(
                        container = container,
                        item = item,
                        onBack = { finish() },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onDestroy()
    }
}
