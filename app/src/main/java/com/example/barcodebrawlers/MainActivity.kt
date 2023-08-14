package com.example.barcodebrawlers
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.barcodebrawlers.database.AppDatabase
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    private val db = AppDatabase.DatabaseProvider.getDatabase(this)
    private val brawlers = listOf<Brawler>(
        Brawler(
            "Space Pirate",
            1,
            "A pirate roaming the vast expanse of the universe.",
            6,
            6,
            6
        ),
        Brawler(
            "Desert Duelist",
            2,
            "A swift swordfighter trained in the scorching sands.",
            6,
            7,
            4
        ),
        Brawler(
            "Forest Phantom",
            3,
            "An elusive archer who blends with the woods.",
            5,
            8,
            5
        ),
        Brawler(
            "Swamp Sorcerer",
            4,
            "A mage drawing power from marshland mysteries.",
            4,
            5,
            9
        ),
        Brawler(
            "Island Invader",
            5,
            "A warrior adept in naval combat and amphibious ambushes.",
            7,
            6,
            3
        ),
        Brawler(
            "Tundra Tracker",
            6,
            "A survivalist skilled in arctic ambushes.",
            6,
            7,
            4
        ),
        Brawler(
            "Canyon Crusader",
            7,
            "A vigilant guardian of the rocky valleys.",
            7,
            6,
            5
        ),
        Brawler(
            "Jungle Juggernaut",
            8,
            "A mighty brawler skilled in navigating dense rainforests.",
            8,
            5,
            4
        ),
        Brawler(
            "Plains Pathfinder",
            9,
            "A nimble scout known for tracking on open terrains.",
            5,
            8,
            5
        ),
        Brawler(
            "Cave Crawler",
            10,
            "A stealthy expert of subterranean combat.",
            6,
            7,
            4
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
        integrator.initiateScan()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null) {
            if (scanResult.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                val barcodeData = scanResult.contents
                val transformedNumber = transformBarcodeToNumber(barcodeData)
                if (transformedNumber < 11) {
                    val brawler = brawlers[transformedNumber]
                    val brawlerString = brawler.toString()
                    Toast.makeText(this, "You found a $brawlerString", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "No brawlers found", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun transformBarcodeToNumber(barcode: String): Int {
        val numericValue = barcode.hashCode()
        return abs(numericValue) % 31 // This ensures the number is between 0 and 30, inclusive
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp(checkAndRequestCameraPermission = ::checkAndRequestCameraPermission)
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MyApp(checkAndRequestCameraPermission: () -> Unit) {
    val navController = rememberNavController()
    val items = listOf(BottomNavItem.Home, BottomNavItem.Scan, BottomNavItem.Collection)

    Scaffold(
        bottomBar = {
            BottomNavigation {
                val currentRoute = currentRoute(navController)
                items.forEach { item ->
                    BottomNavigationItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                // Prevent multiple copies of the same destination
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
        }
    ) {
        NavHost(navController, startDestination = BottomNavItem.Home.route) {
            composable(BottomNavItem.Home.route) { Home(navController) }
            composable(BottomNavItem.Scan.route) { Scan(navController, onScanClick = checkAndRequestCameraPermission) }
            composable(BottomNavItem.Collection.route) { Collection(navController) }
        }
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}


sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Scan : BottomNavItem("scan", "Scan", Icons.Default.Add)
    object Collection : BottomNavItem("collection", "Collection", Icons.Default.List)
}

@Composable
fun Home(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
    }
}


