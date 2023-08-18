package com.example.barcodebrawlers
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.barcodebrawlers.database.AppDatabase
import com.example.barcodebrawlers.ui.theme.BarcodeBrawlersTheme
import com.google.zxing.integration.android.IntentIntegrator

import kotlin.math.abs

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private var collectedBrawlersCount by mutableStateOf(0)
    private lateinit var successSound: MediaPlayer
    private lateinit var failureSound: MediaPlayer

    private val brawlers = listOf<Brawler>(
        Brawler(
            "Space Pirate",
            1,
            "A pirate roaming the vast expanse of the universe.",
            6,
            6,
            6,
            imageResId = R.drawable.spacepirate
        ),
        Brawler(
            "Desert Duelist",
            2,
            "A swift sword fighter trained in the scorching sands.",
            6,
            7,
            4,
            imageResId = R.drawable.desertduelist
        ),
        Brawler(
            "Forest Phantom",
            3,
            "An elusive archer who blends with the woods.",
            5,
            8,
            5,
            imageResId = R.drawable.forestphantom
        ),
        Brawler(
            "Swamp Sorcerer",
            4,
            "A mage drawing power from marshland mysteries.",
            4,
            5,
            9,
            imageResId = R.drawable.swampsorcerer
        ),
        Brawler(
            "Tribal Warrior",
            5,
            "A warrior adept in naval combat and amphibious ambushes.",
            7,
            6,
            3,
            imageResId = R.drawable.tribalwarriror
        ),
        Brawler(
            "Tundra Tracker",
            6,
            "A survivalist skilled in arctic ambushes.",
            6,
            7,
            4,
            imageResId = R.drawable.tundratracker
        ),
        Brawler(
            "Canyon Crusader",
            7,
            "A vigilant guardian of the rocky valleys.",
            7,
            6,
            5,
            imageResId = R.drawable.canyoncrusader
        ),
        Brawler(
            "Jungle Juggernaut",
            8,
            "A mighty brawler skilled in navigating dense rainforests.",
            8,
            5,
            4,
            imageResId = R.drawable.junglejuggernaut
        ),
        Brawler(
            "Plains Pathfinder",
            9,
            "A nimble scout known for tracking on open terrains.",
            5,
            8,
            5,
            imageResId = R.drawable.plainspathfinder
        ),
        Brawler(
            "Cave Crawler",
            10,
            "A stealthy expert of subterranean combat.",
            6,
            7,
            4,
            imageResId = R.drawable.cavecrawler
        )
    )

    private companion object {
        const val CAMERA_REQUEST_CODE = 1001
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }

    fun checkAndRequestCameraPermission() {
        val cameraPermission = arrayOf(Manifest.permission.CAMERA)
        if (!hasPermissions(cameraPermission)) {
            ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE)
        } else {
            startBarcodeScanning()
        }
    }

    private fun startBarcodeScanning() {
        val integrator = IntentIntegrator(this)
        integrator.setBeepEnabled(false)
        integrator.initiateScan()

    }

    private fun deleteAll() {
        collectedBrawlersCount = db.brawlerDao().getBrawlersCount()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null) {
            if (scanResult.contents == null) {
                Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_LONG).show()
            } else {
                val barcodeData = scanResult.contents
                val transformedNumber = transformBarcodeToNumber(barcodeData)
                if (transformedNumber < 10) {
                    successSound.start()
                    val brawler = brawlers[transformedNumber]
                    val brawlerString = brawler.toString()
                    Toast.makeText(this, getString(R.string.brawler_found, brawlerString), Toast.LENGTH_LONG).show()
                    db.brawlerDao().insert(brawler.toEntity())
                    collectedBrawlersCount = db.brawlerDao().getBrawlersCount()
                } else {
                    Toast.makeText(this, getString(R.string.no_brawlers_found), Toast.LENGTH_LONG).show()
                    failureSound.start()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        successSound.release()
        failureSound.release()
    }

    fun transformBarcodeToNumber(barcode: String): Int {
        val numericValue = barcode.hashCode()
        return abs(numericValue) % 31
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase.DatabaseProvider.getDatabase(this)
        successSound = MediaPlayer.create(this, R.raw.success_sound)
        failureSound = MediaPlayer.create(this, R.raw.failure_sound)
        collectedBrawlersCount = db.brawlerDao().getBrawlersCount()
        setContent {
            MyApp(
                db,
                collectedBrawlersCount,
                checkAndRequestCameraPermission = ::checkAndRequestCameraPermission,
                deleteAll = ::deleteAll
            )
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MyApp(db: AppDatabase, collectedBrawlersCount: Int, checkAndRequestCameraPermission: () -> Unit, deleteAll: () -> Unit) {
    val darkThemeEnabled = remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val items = listOf(BottomNavItem.Home, BottomNavItem.Collection, BottomNavItem.Settings)

    BarcodeBrawlersTheme(darkTheme = darkThemeEnabled.value) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(id = R.string.app_name), textAlign = TextAlign.Start)
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavigation {
                    val currentRoute = currentRoute(navController)
                    items.forEach { item ->
                        BottomNavigationItem(
                            icon = { Icon(item.icon, contentDescription = stringResource(id = item.labelResource)) },
                            label = { Text(stringResource(id = item.labelResource)) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            },
            content = {
                Box(modifier = Modifier.padding(bottom = 56.dp)) {
                    NavHost(navController, startDestination = BottomNavItem.Home.route) {
                        composable(BottomNavItem.Home.route) {
                            Home(
                                navController,
                                collectedBrawlersCount,
                                onScanClick = checkAndRequestCameraPermission
                            )
                        }
                        composable(BottomNavItem.Collection.route) { Collection(navController, db) }
                        composable(BottomNavItem.Settings.route) {
                            SettingsScreen(
                                darkThemeEnabled,
                                db,
                                deleteAll
                            )
                        }
                    }
                }
            })
    }
}
@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}


sealed class BottomNavItem(val route: String, val labelResource: Int, val icon: ImageVector) {
    object Home : BottomNavItem("home", R.string.home, Icons.Default.Home)
    object Collection : BottomNavItem("collection", R.string.collection, Icons.Default.List)
    object Settings : BottomNavItem("setting", R.string.settings, Icons.Default.Settings)
}

@Composable
fun Home(navController: NavController, collectedBrawlersCount: Int, onScanClick: () -> Unit) {
    val totalBrawlers = 10  // Total number of brawlers that can be collected
    val targetProgress = collectedBrawlersCount.toFloat() / totalBrawlers

    // State for controlling the start of the animation
    val startAnimation = remember { mutableStateOf(false) }


    LaunchedEffect(key1 = Unit) {
        startAnimation.value = true
    }

    // Animate progress only if startAnimation is true
    val animatedProgress by animateFloatAsState(
        targetValue = if (startAnimation.value) targetProgress else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onScanClick,
        ) {
            Text(
                text = stringResource(id = R.string.scan)
            )
        }
        // ... Other components

        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = stringResource(
                id = R.string.collected_brawlers,
                collectedBrawlersCount,
                totalBrawlers
            )
        )
    }
}

@Composable
fun SettingsScreen(
    darkThemeEnabled: MutableState<Boolean>,
    db: AppDatabase,
    deleteAll: () -> Unit,
) {
    var inputText by remember { mutableStateOf("") }
    var isDialogVisible by remember { mutableStateOf(false) }
    val deleteString = stringResource(id = R.string.delete )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.settings),
            style = TextStyle(fontSize = 30.sp)
        )

        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ThemeToggleSwitch(
            isDarkTheme = darkThemeEnabled.value,
            onThemeChange = { darkThemeEnabled.value = it }
        )

        // Delete All button to show the dialog
        Button(
            onClick = { isDialogVisible = true }
        ) {
            Text(text = stringResource(id = R.string.delete_all))
        }

        // Dialog to confirm the deletion
        if (isDialogVisible) {
            AlertDialog(
                onDismissRequest = { isDialogVisible = false },
                title = { Text(text = stringResource(id = R.string.delete_all_confirmation)) },
                text = {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text(text = stringResource(id = R.string.enter_delete)) }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputText == deleteString) {
                                db.brawlerDao().deleteAll()
                                deleteAll()
                                inputText = ""
                                isDialogVisible = false
                            }
                        },
                        enabled = inputText == deleteString
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            isDialogVisible = false
                            inputText = ""
                        }
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun ThemeToggleSwitch(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(id = R.string.dark_mode))
        Switch(
            checked = isDarkTheme,
            onCheckedChange = onThemeChange
        )
    }
}
