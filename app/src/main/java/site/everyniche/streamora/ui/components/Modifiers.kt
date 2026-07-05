package site.everyniche.streamora.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/** clickable() without the default ripple, for inline-text style click targets. */
fun Modifier.clickableNoIndication(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}
