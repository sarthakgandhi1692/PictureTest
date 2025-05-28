package com.example.test.ui.mainActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.composeCommon.rememberScaledBitmap
import com.example.test.composeCommon.shimmerBrush
import com.example.test.model.local.ProcessedImage
import com.example.test.ui.faceDetailActivity.FaceDetailActivity
import com.example.test.utils.PermissionUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity for the app.
 */
@AndroidEntryPoint
@SuppressLint("UnusedBoxWithConstraintsScope")
class ImageListingActivity : ComponentActivity() {

    private val viewModel: ImageListingActivityViewModel by viewModels()
    private var navigatedToSettingsForPermission = false

    @Inject
    lateinit var permissionUtil: PermissionUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request necessary permissions when the activity is created.
        if (permissionUtil.isMediaPermissionGranted(this)) {
            viewModel.updatePermissionState(true)
            viewModel.processImages()
        }
        setContent {
            MainContent()
        }
    }

    /**
     * Requests necessary permissions for the app.
     */
    private fun requestPermission() {
        permissionUtil.requestMediaPermission(
            this@ImageListingActivity, onGranted = {
                viewModel.updatePermissionState(true)
                viewModel.processImages()
            }, onSettingsOpened = {
                navigatedToSettingsForPermission = true
            })
    }

    @Composable
    fun MainContent() {
        // Observe permission status from ViewModel.
        val hasPermission by viewModel.hasPermission
        val faceImages by viewModel.allFacesFlow.collectAsState()

        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .background(Color.Companion.White)
        ) {
            Column {
                // Top app bar with back button and title.
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
                        text = stringResource(R.string.image_list),
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically),
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }

                // Display gallery grid if permission is granted.
                if (hasPermission) {
                    GalleryFaceGrid(faceImages = faceImages)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Blue)
                                .clickable { requestPermission() }
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.get_started),
                                color = Color.White
                            )
                        }
                    }
                }

            }
        }

    }

    // Composable function to display the grid of face images.
    @Composable
    fun GalleryFaceGrid(
        faceImages: List<ProcessedImage>
    ) {
        // Show shimmer loading effect if images are not yet loaded.
        if (faceImages.isEmpty()) {
            CustomShimmerGrid()
        } else {
            // Display the grid of images once loaded.
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

    /**
     * Composable function to display a grid of shimmer items.
     * @param itemCount Number of items to display in the grid.
     * @param columns Number of columns in the grid.
     */
    @Composable
    fun CustomShimmerGrid(
        itemCount: Int = 20,
        columns: Int = 3
    ) {
        val brush = shimmerBrush()

        // LazyVerticalGrid for efficient display of shimmer items.
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

    /**
     * Composable function to display an image with face overlays.
     * @param processedImage Processed image data.
     */
    @Composable
    fun FaceOverlayCanvas(processedImage: ProcessedImage) {

        // BoxWithConstraints to get the available width for scaling the image.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .clickable {
                    startActivity(
                        Intent(
                            this,
                            FaceDetailActivity::class.java
                        ).apply {
                            putExtra(ARG_IMAGE_URI, processedImage.uri)
                        })
                }
        ) {
            // Calculate width in pixels.
            val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val scaledBitmap = rememberScaledBitmap(processedImage.bitmap, widthPx)

            // Display the scaled bitmap.
            scaledBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }
    }


    // Called when the activity is becoming visible to the user.
    override fun onStart() {
        super.onStart()
        if (navigatedToSettingsForPermission) {
            // If navigated back from settings, re-request permission.
            navigatedToSettingsForPermission = false
            requestPermission()
        }
    }


    companion object {
        private const val ARG_IMAGE_URI = "ARG_IMAGE_URI"
    }

}