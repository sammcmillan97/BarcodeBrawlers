package com.example.barcodebrawlers

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.example.barcodebrawlers.database.AppDatabase
import com.example.barcodebrawlers.entities.BrawlerEntity


@Composable
fun Collection(navController: NavController, db: AppDatabase) {
    val context = LocalContext.current
    val brawlers = remember {
        mutableStateListOf<BrawlerEntity>()
    }
    var openDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        brawlers.clear()
        brawlers.addAll(db.brawlerDao().getAllBrawlers())
    }
    var selectedBrawler by remember { mutableStateOf(brawlers.firstOrNull()) }
    BrawlerList(brawlers, onBrawlerClick = {BrawlerEntity ->
        selectedBrawler = BrawlerEntity
        openDialog = true
    })
    if (openDialog && selectedBrawler != null) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = {
                Text(
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    text = selectedBrawler!!.name
                )
            },
            text = {
                Column {
                    Text("${stringResource(id = R.string.Description)}: ${selectedBrawler!!.description}")
                    Spacer(modifier = Modifier.height(15.dp))
                    Text("${stringResource(id = R.string.Strength)}: ${selectedBrawler!!.strength}")
                    Text("${stringResource(id = R.string.Agility)}: ${selectedBrawler!!.agility}")
                    Text("${stringResource(id = R.string.Intelligence)}: ${selectedBrawler!!.intelligence}")
                }
            },
            buttons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { shareBrawler(context, selectedBrawler!!)},
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Share")
                    }
                    TextButton(
                        onClick = { openDialog = false },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        )
    }
}

private fun shareBrawler(context: Context, brawler: BrawlerEntity) {
    val shareText = "Check out this Brawler: ${brawler.name}. Strength: ${brawler.strength}, Agility: ${brawler.agility}, Intelligence: ${brawler.intelligence}"

    // Convert the resource ID of the image into a URI
    val imageUri = Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/${brawler.imageResId}")

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_STREAM, imageUri)
        type = "image/*"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share using"))
}

@Composable
fun BrawlerList(brawlers: List<BrawlerEntity>, onBrawlerClick: (BrawlerEntity) -> Unit) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(brawlers) { brawler ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        onBrawlerClick(brawler)
                    },
                elevation = 4.dp
            ) {
                if (isLandscape) {
                    BrawlerListItemLandscape(brawler)
                } else {
                    BrawlerListItemPortrait(brawler)
                }
            }
        }
    }
}

@Composable
fun BrawlerListItemPortrait(brawler: BrawlerEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LoadImageFromResources(resId = brawler.imageResId, imageSize = 100)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = brawler.name,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun BrawlerListItemLandscape(brawler: BrawlerEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image Section
        LoadImageFromResources(resId = brawler.imageResId, imageSize = 150)
        Spacer(modifier = Modifier.width(16.dp))

        // Title Section
        Box(
            modifier = Modifier
                .width(150.dp) // Define a fixed width for the title
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = brawler.name,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Description Section
        Box(
            modifier = Modifier
                .weight(1f) // Takes up remaining space
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = brawler.description,
                style = TextStyle(
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun LoadImageFromResources(resId: Int, imageSize: Int ) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = null,
        modifier = Modifier
            .size(imageSize.dp)
            .aspectRatio(1f) // This enforces a 1:1 aspect ratio (square)
            .clip(shape = MaterialTheme.shapes.medium), // Optional: to clip the image as per a shape
        contentScale = ContentScale.Crop // This crops the image to fit within the bounds defined by the Modifier
    )
}
