package com.example.test.ui.mainActivity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import com.example.test.R
import com.example.test.composeCommon.shimmerBrush
import com.example.test.model.local.ProcessedImage
import com.example.test.ui.faceDetailActivity.FaceDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    private var navigatedToSettings = false

    private val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.processImages()
        } else {
            // Check if we should show a rationale or send user to settings
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showRationaleDialog()
            } else {
                // User checked "Don't ask again", redirect to settings
                showSettingsDialog()
            }
        }
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("We need access to your storage to load and process images.")
            .setPositiveButton("Try Again") { _, _ ->
                requestPermissionLauncher.launch(permission)
            }
            .setNegativeButton("Cancel") { _, _ ->
                requestPermissionLauncher.launch(permission)
            }
            .show()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Permission was denied permanently. Please go to Settings to enable it.")
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                navigatedToSettings = true
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.processImages()
        } else {
            requestPermissionLauncher.launch(permission)
        }
        setContent {
            MainContent(
                viewModel.state
            )
        }
    }

    @Preview
    @Composable
    fun MainContent(
        state: MainActivityState? = MainActivityState()
    ) {
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .background(Color.Companion.White)
        ) {

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable {
                                onBackPressed()
                            }
                    ) {
                        Image(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center),
                            painter = painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = ""
                        )
                    }

                    Text(
                        text = "Image List",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically),
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }

                val hasPermission by viewModel.hasPermission.collectAsState()

                if (hasPermission) {
                    GalleryFaceGrid()
                }

            }
        }

    }

    @Composable
    fun GalleryFaceGrid() {
        val faceImages by viewModel.allFacesFlow.collectAsState()

        if (faceImages.isEmpty()) {
            CustomShimmerGrid()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                items(faceImages) { processedImage ->
                    FaceOverlayCanvas(processedImage)
                }
            }
        }
    }

    @Composable
    fun CustomShimmerGrid(
        itemCount: Int = 20,
        columns: Int = 3
    ) {
        val brush = shimmerBrush()

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.Companion.fillMaxSize()
        ) {
            items(itemCount) {
                Box(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .height(128.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )
            }
        }
    }

    @Composable
    fun FaceOverlayCanvas(processedImage: ProcessedImage) {
        val originalBitmap = processedImage.bitmap
        var scaledBitmap by remember { mutableStateOf<Bitmap?>(null) }

        BoxWithConstraints(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .height(128.dp)
                .clickable {
                    startActivity(Intent(this@MainActivity, FaceDetailActivity::class.java).apply {
                        putExtra("imageUri", processedImage.uri)
                    })
                }
        ) {
            val maxWidth = this.maxWidth
            val widthPx = with(LocalDensity.current) { maxWidth.toPx() }

            // Downscale bitmap off the main thread
            LaunchedEffect(originalBitmap, maxWidth) {
                withContext(Dispatchers.IO) {
                    val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height
                    val targetHeightPx = (widthPx / aspectRatio).toInt()
                    val downscaled = originalBitmap.scale(widthPx.toInt(), targetHeightPx)
                    scaledBitmap = downscaled
                }
            }

        }

        scaledBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "",
                contentScale = ContentScale.Companion.Crop
            )
        }
    }

    override fun onStart() {
        super.onStart()
        if (navigatedToSettings) {
            navigatedToSettings = false
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.processImages()
            } else {
                requestPermissionLauncher.launch(permission)
            }
        }
    }


}