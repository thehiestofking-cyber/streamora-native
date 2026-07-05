package site.everyniche.streamora

import android.content.Context
import site.everyniche.streamora.data.network.SupabaseAuth
import site.everyniche.streamora.data.network.SupabaseData
import site.everyniche.streamora.data.repo.StreamoraRepository

/** Simple manual dependency container (no DI framework needed for this app size). */
class AppContainer(context: Context) {
    val repository = StreamoraRepository()
    val auth = SupabaseAuth(context)
    val supabaseData = SupabaseData(auth)
}
