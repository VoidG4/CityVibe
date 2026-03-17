package com.example.unipicityvibe

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unipicityvibe.model.Event
import com.example.unipicityvibe.model.fetchEventsFromFirestore
import com.example.unipicityvibe.ui.screens.BookingsScreen
import com.example.unipicityvibe.ui.screens.EventDetailsScreen
import com.example.unipicityvibe.ui.screens.HomeScreen
import com.example.unipicityvibe.ui.screens.LoginScreen
import com.example.unipicityvibe.ui.screens.MapScreen
import com.example.unipicityvibe.ui.screens.SettingsScreen
import com.example.unipicityvibe.ui.screens.VoiceCommandScreen
import com.example.unipicityvibe.ui.theme.ShadcnColors
import com.example.unipicityvibe.ui.theme.UnipiCityVibeTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.content.edit

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // 1. Read Language from Preferences
        val prefs = getSharedPreferences("UnipiSettings", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("language", "English") ?: "English"

        // 2. Apply Language Locale
        setAppLocale(this, savedLanguage)

        requestPermissions()

        setContent {
            val context = LocalContext.current
            // Theme State
            var isDarkTheme by remember { mutableStateOf(prefs.getString("theme", "Light") == "Dark") }

            // Events State
            var eventsList by remember { mutableStateOf<List<Event>>(emptyList()) }

            val toggleTheme: (Boolean) -> Unit = { makeDark ->
                isDarkTheme = makeDark
                prefs.edit { putString("theme", if (makeDark) "Dark" else "Light") }
            }

            LaunchedEffect(Unit) {
                fetchEventsFromFirestore { events -> eventsList = events }
            }

            UnipiCityVibeTheme(darkTheme = isDarkTheme) {
                // Manage Status Bar & Navigation Bar Colors
                val view = LocalView.current
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as Activity).window
                        val insetsController = WindowCompat.getInsetsController(window, view)
                        if (isDarkTheme) {
                            window.statusBarColor = android.graphics.Color.BLACK
                            window.navigationBarColor = android.graphics.Color.BLACK
                            insetsController.isAppearanceLightStatusBars = false
                            insetsController.isAppearanceLightNavigationBars = false
                        } else {
                            window.statusBarColor = android.graphics.Color.WHITE
                            window.navigationBarColor = android.graphics.Color.WHITE
                            insetsController.isAppearanceLightStatusBars = true
                            insetsController.isAppearanceLightNavigationBars = true
                        }
                    }
                }

                MainAppEntry(
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = toggleTheme,
                    intent = intent,
                    eventsList = eventsList,
                    currentLanguage = savedLanguage,
                    onLanguageChanged = {
                        recreate() // Restart Activity to apply new language
                    }
                )
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 101)
    }

    @Suppress("DEPRECATION")
    private fun setAppLocale(context: Context, language: String) {
        val localeCode = when (language) {
            "Greek" -> "el"
            "Spanish" -> "es"
            else -> "en"
        }
        val locale = Locale(localeCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainAppEntry(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    intent: Intent,
    eventsList: List<Event>,
    currentLanguage: String,
    onLanguageChanged: () -> Unit
) {
    var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

    if (isLoggedIn) {
        AppWithNavigationDrawer(
            onLogout = { isLoggedIn = false },
            isDarkTheme = isDarkTheme,
            onThemeChanged = onThemeChanged,
            intent = intent,
            eventsList = eventsList,
            currentLanguage = currentLanguage,
            onLanguageChanged = onLanguageChanged
        )
    } else {
        LoginScreen(onLoginSuccess = { isLoggedIn = true })
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppWithNavigationDrawer(
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    intent: Intent,
    eventsList: List<Event>,
    currentLanguage: String,
    onLanguageChanged: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("UnipiSettings", Context.MODE_PRIVATE)

    var curLanguage by remember {
        mutableStateOf(prefs.getString("language", "English") ?: "English")
    }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    LaunchedEffect(intent) {
        val eventIdFromNotif = intent.getStringExtra("notification_event_id")
        if (eventIdFromNotif != null) {
            intent.removeExtra("notification_event_id")
            navController.navigate("eventDetails/$eventIdFromNotif")
        }
    }

    val topBarTitle = "UnipiCityVibe"
    val bgColor = if (isDarkTheme) Color(0xFF020617) else Color(0xFFF8FAFC)
    val contentColor = if (isDarkTheme) Color.White else Color.Black
    val drawerColor = if (isDarkTheme) Color(0xFF0F172A) else Color.White

    // Messages for Location Toast
    val locDisabledMsg = stringResource(R.string.location_disabled)
    val checkLocMsg = stringResource(R.string.check_location)
    val loadingEventsMsg = stringResource(R.string.search_placeholder) // Or "Loading..."

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = drawerColor, modifier = Modifier.width(300.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(ShadcnColors.BrandGradient).padding(16.dp), contentAlignment = Alignment.BottomStart) {
                    Column {
                        Icon(Icons.Default.Event, null, tint = Color.White, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("UnipiCityVibe", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Menu Items
                val menuItems = listOf(
                    Triple(stringResource(R.string.menu_home), "home", Icons.Default.Home),
                    Triple(stringResource(R.string.menu_map), "map", Icons.Outlined.Map),
                    Triple(stringResource(R.string.menu_bookings), "bookings", Icons.Outlined.ConfirmationNumber),
                    Triple(stringResource(R.string.menu_settings), "settings", Icons.Outlined.Settings),
                    Triple(stringResource(R.string.menu_voice), "voice", Icons.Default.Mic)
                )

                menuItems.forEach { (label, route, icon) ->
                    NavigationDrawerItem(
                        label = { Text(label) },
                        icon = { Icon(icon, null) },
                        selected = currentRoute == route,
                        onClick = { navController.navigate(route); scope.launch { drawerState.close() } },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_logout), color = Color.Red) },
                    icon = { Icon(Icons.Default.ExitToApp, null, tint = Color.Red) },
                    selected = false,
                    onClick = { FirebaseAuth.getInstance().signOut(); scope.launch { drawerState.close() }; onLogout() },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = bgColor,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(topBarTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (currentRoute.startsWith("eventDetails")) {
                                navController.popBackStack()
                            } else {
                                scope.launch { drawerState.open() }
                            }
                        }) {
                            Icon(if (currentRoute.startsWith("eventDetails")) Icons.Default.ArrowBack else Icons.Default.Menu, null)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = bgColor, titleContentColor = contentColor, navigationIconContentColor = contentColor)
                )
            },
            floatingActionButton = {
                if (currentRoute != "voice") {
                    FloatingActionButton(
                        onClick = {
                            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                            val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                                    locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

                            if (!isGpsEnabled) {
                                Toast.makeText(context, locDisabledMsg, Toast.LENGTH_LONG).show()
                                context.startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            } else {
                                if (eventsList.isEmpty()) {
                                    Toast.makeText(context, loadingEventsMsg, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, checkLocMsg, Toast.LENGTH_SHORT).show()
                                    val manager = LocationNotificationManager(context)
                                    manager.startLocationUpdates(eventsList)
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Check Location Now")
                    }
                }
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = "home", Modifier.padding(innerPadding)) {
                composable("home") {
                    HomeScreen(
                        isDarkTheme = isDarkTheme,
                        onEventClick = { navController.navigate("eventDetails/$it") }
                    )
                }
                composable("map") {
                    MapScreen(isDarkTheme = isDarkTheme, onEventClick = { navController.navigate("eventDetails/$it") })
                }
                composable("bookings") {
                    BookingsScreen(isDarkTheme = isDarkTheme, language = curLanguage)
                }
                composable("settings") {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        language = curLanguage,
                        onThemeChanged = onThemeChanged,
                        onLanguageChanged = { _ ->
                            onLanguageChanged()
                        }
                    )
                }
                composable("voice") {
                    VoiceCommandScreen(onNavigate = { route -> navController.navigate(route) { popUpTo("home") } })
                }
                composable(
                    route = "eventDetails/{eventId}",
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                    EventDetailsScreen(
                        isDarkTheme = isDarkTheme,
                        language = curLanguage,
                        eventId = eventId,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}