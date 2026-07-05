package site.everyniche.streamora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.everyniche.streamora.ui.theme.AccentBlue
import site.everyniche.streamora.ui.theme.TextSecondary

enum class NavTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    SEARCH("Search", Icons.Filled.Search),
    DOWNLOADS("Downloads", Icons.Filled.Download),
    PROFILE("Profile", Icons.Filled.Person),
}

@Composable
fun BottomNavBar(active: NavTab, onChange: (NavTab) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(androidx.compose.ui.graphics.Color(0xE0111827))
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            NavTab.entries.forEach { tab ->
                val isActive = tab == active
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onChange(tab) }.padding(horizontal = 14.dp, vertical = 2.dp),
                ) {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        tint = if (isActive) AccentBlue else TextSecondary,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        tab.label,
                        color = if (isActive) AccentBlue else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
