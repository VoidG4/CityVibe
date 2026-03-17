@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.example.unipicityvibe.ui.screens

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.example.unipicityvibe.R
import com.example.unipicityvibe.model.Event
import com.example.unipicityvibe.model.fetchEventsFromFirestore
import com.example.unipicityvibe.ui.theme.ShadcnColors
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import androidx.compose.foundation.layout.FlowRow
import androidx.preference.PreferenceManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapScreen(
    isDarkTheme: Boolean,
    onEventClick: (String) -> Unit
) {
    val context = LocalContext.current
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    // --- Colors ---
    val bgColor = if (isDarkTheme) Color(0xFF020617) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkTheme) Color(0xFF0F172A) else Color.White
    val legendTextColor = if (isDarkTheme) Color(0xFF94A3B8) else ShadcnColors.Primary

    // --- List for Legend ---
    val categoriesForLegend = listOf(
        Pair(R.string.cat_theater, Color(0xFF9333EA)),
        Pair(R.string.cat_concert, Color(0xFFE11D48)),
        Pair(R.string.cat_festival, Color(0xFFF97316)),
        Pair(R.string.cat_exhibition, Color(0xFF16A34A)),
        Pair(R.string.cat_cinema, Color(0xFF2563EB)),
        Pair(R.string.cat_sports, Color(0xFFDC2626))
    )

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
        fetchEventsFromFirestore { fetched -> events = fetched }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(bgColor)) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().background(ShadcnColors.BrandGradient).padding(24.dp)) {
            Column {
                Text(stringResource(R.string.map_title), fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(stringResource(R.string.map_desc), fontSize = 16.sp, color = Color(0xFFE0E7FF))
            }
        }

        // MAP CONTAINER
        Box(modifier = Modifier.fillMaxWidth().height(600.dp).padding(20.dp)) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                key(events) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(14.0)
                                controller.setCenter(GeoPoint(37.942986, 23.646983))

                                events.forEach { event ->
                                    val marker = Marker(this)
                                    marker.position = GeoPoint(event.lat, event.lng)
                                    marker.title = event.title

                                    val colorInt = getCategoryColor(event.category)
                                    marker.icon = getColoredMarker(ctx, colorInt)

                                    marker.infoWindow = object : InfoWindow(R.layout.map_info_window, this) {
                                        override fun onOpen(item: Any?) {
                                            closeAllInfoWindowsOn(this.mMapView)
                                            val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                                            val layout = inflater.inflate(R.layout.map_info_window, null)
                                            val titleView = layout.findViewById<TextView>(R.id.bubble_title)
                                            val descView = layout.findViewById<TextView>(R.id.bubble_description)
                                            val container = layout.findViewById<View>(R.id.bubble_layout)

                                            titleView.text = event.title
                                            descView.text = event.category
                                            container.setOnClickListener { onEventClick(event.id) }
                                            mView = layout
                                        }
                                        override fun onClose() {}
                                    }

                                    marker.setOnMarkerClickListener { m, _ ->
                                        if (m.isInfoWindowShown) m.closeInfoWindow() else m.showInfoWindow()
                                        true
                                    }
                                    overlays.add(marker)
                                }
                            }
                        }
                    )
                }
            }
        }

        // LEGEND SECTION
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)) {
            Text(stringResource(R.string.legend), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = legendTextColor, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

            FlowRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categoriesForLegend.forEach { (stringId, color) ->
                    LegendItem(color = color, text = stringResource(stringId), isDarkTheme = isDarkTheme)
                }
            }
        }
    }
}

/**
 * Maps category names to specific colors for markers and legend.
 */
fun getCategoryColor(category: String): Int {
    val cat = category.trim().lowercase()
    return when {
        cat.contains("theater") || cat.contains("theatre") || cat.contains("θέατρο") -> Color(0xFF9333EA).toArgb()
        cat.contains("concert") || cat.contains("music") || cat.contains("συναυλία") -> Color(0xFFE11D48).toArgb()
        cat.contains("festival") || cat.contains("φεστιβάλ") -> Color(0xFFF97316).toArgb()
        cat.contains("exhibition") || cat.contains("museum") || cat.contains("έκθεση") -> Color(0xFF16A34A).toArgb()
        cat.contains("cinema") || cat.contains("movie") || cat.contains("σινεμά") -> Color(0xFF2563EB).toArgb()
        cat.contains("sport") || cat.contains("gym") || cat.contains("αθλη") -> Color(0xFFDC2626).toArgb()
        else -> Color.Gray.toArgb()
    }
}

/**
 * Creates a colored marker drawable.
 */
fun getColoredMarker(context: Context, color: Int): Drawable? {
    val defaultDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pin) ?: return null
    val wrappedDrawable = defaultDrawable.mutate()
    wrappedDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    return wrappedDrawable
}

@Composable
fun LegendItem(color: Color, text: String, isDarkTheme: Boolean) {
    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    val textColor = if (isDarkTheme) Color(0xFFE0E7FF) else ShadcnColors.Primary

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(bgColor).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(color))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}