package com.example.test.ui.faceDetailActivity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.composeCommon.rememberScaledBitmap
import com.example.test.composeCommon.shimmerBrush
import com.example.test.model.local.FaceInfo
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for displaying face details.
 */
@AndroidEntryPoint
@SuppressLint("UnusedBoxWithConstraintsScope")
class FaceDetailActivity : ComponentActivity() {

    // ViewModel for this activity
    private val viewModel: FaceDetailActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get image URI from intent and load the image
        val imageUri = intent.getStringExtra(ARG_IMAGE_URI)
        viewModel.getProcessedImage(imageUri!!)

        setContent {
            MainContent(
                onNameUpdated = { index, name ->
                    viewModel.saveName(index, name)
                }
            )
        }

        initObservers()
    }

    private fun initObservers() {
        viewModel.uiEventsLiveData.observe(this) {
            when (it) {
                FaceDetailActivityViewModel.UIEvents.SHOW_ERROR_TOAST -> {
                    Toast.makeText(
                        this@FaceDetailActivity,
                        getString(R.string.name_saved_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                FaceDetailActivityViewModel.UIEvents.SHOW_SUCCESS_TOAST -> {
                    Toast.makeText(
                        this@FaceDetailActivity,
                        getString(R.string.name_saved_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Main content of the activity.
     */
    @Composable
    fun MainContent(
        onNameUpdated: (FaceInfo, String) -> Unit
    ) {
        val imageState = viewModel.imageState.value
        val bitmap = imageState?.bitmap
        val faces = imageState?.faces.orEmpty()
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .background(Color.Companion.White)
        ) {
            Column {
                // Top app bar with back button and title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    // Back button
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

                    // Title text
                    Text(
                        text = stringResource(R.string.image_details),
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically),
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }

                // Screen for displaying face details
                FaceDetailScreen(
                    originalBitmap = bitmap,
                    faces = faces,
                    onNameUpdated = { index, name ->
                        onNameUpdated(index, name)
                    })
            }

        }
    }

    /**
     * Screen for displaying face details.
     *
     * @param onNameUpdated Callback for when a face name is updated.
     */
    @Composable
    fun FaceDetailScreen(
        originalBitmap: Bitmap?,
        faces: List<FaceInfo>?,
        onNameUpdated: (FaceInfo, String) -> Unit
    ) {
        if (originalBitmap == null || faces == null) return

        // State for selected face and name input
        var selectedFace by remember { mutableStateOf<FaceInfo?>(null) }
        var nameInput by remember { mutableStateOf("") }

        // Use BoxWithConstraints to get the maximum width available
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val maxWidth = this.maxWidth
            val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
            // Scale the bitmap to fit the width
            val scaledBitmap = rememberScaledBitmap(originalBitmap, widthPx)
            if (scaledBitmap == null) {
                // Show loading shimmer if bitmap is not loaded
                LoadingShimmerPlaceholder()
                return@BoxWithConstraints
            }

            FaceImageWithOverlays(
                originalBitmap = originalBitmap,
                scaledBitmap = scaledBitmap,
                faces = faces,
                onFaceClick = { face ->
                    selectedFace = face
                    nameInput = face.name ?: ""
                }
            )

            // Show name input dialog if a face is selected
            selectedFace?.let { face ->
                NameInputDialog(
                    nameInput = nameInput,
                    onNameChange = { nameInput = it },
                    onConfirm = {
                        onNameUpdated(face, nameInput)
                        selectedFace = null
                        nameInput = ""
                    },
                    onDismiss = {
                        selectedFace = null
                        nameInput = ""
                    }
                )
            }
        }
    }

    /**
     * Composable for displaying the face image with overlays for detected faces.
     *
     * @param originalBitmap The original bitmap of the image.
     * @param scaledBitmap The scaled bitmap of the image.
     * @param faces List of detected faces.
     * @param onFaceClick Callback for when a face is clicked.
     */
    @Composable
    fun FaceImageWithOverlays(
        originalBitmap: Bitmap,
        scaledBitmap: Bitmap,
        faces: List<FaceInfo>,
        onFaceClick: (FaceInfo) -> Unit
    ) {
        // Calculate scaling factors for face bounding boxes
        val scaleX = scaledBitmap.width.toFloat() / originalBitmap.width
        val scaleY = scaledBitmap.height.toFloat() / originalBitmap.height

        // Display the scaled image
        Image(
            bitmap = scaledBitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        // Iterate over detected faces and draw a box for each
        faces.forEach { face ->
            val rect = face.boundingBox
            val left = rect.left * scaleX
            val top = rect.top * scaleY
            val width = rect.width() * scaleX
            val height = rect.height() * scaleY

            FaceBox(
                name = face.name,
                left = left,
                top = top,
                width = width,
                height = height,
                onClick = { onFaceClick(face) }
            )
        }
    }

    /**
     * Composable for displaying a face box.
     *
     * @param name Name of the face.
     * @param left Left coordinate of the face box.
     * @param top Top coordinate of the face box.
     * @param width Width of the face box.
     * @param height Height of the face box.
     * @param onClick Callback for when the face box is clicked.
     */

    @Composable
    fun FaceBox(
        name: String?,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        onClick: () -> Unit
    ) {
        val density = LocalDensity.current
        Box(
            modifier = Modifier
                .offset { IntOffset(left.toInt(), top.toInt()) }
                .size(
                    width = with(density) { width.toDp() },
                    height = with(density) { height.toDp() }
                )
                .background(Color.Transparent)
                .clickable { onClick() }
        ) {
            // Display name if available
            if (TextUtils.isEmpty(name).not()) {
                Text(
                    text = name!!,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                        .background(Color(0x88000000), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }

    /**
     * Composable for displaying a name input dialog.
     *
     * @param nameInput Current name input.
     * @param onNameChange Callback for when the name input changes.
     * @param onConfirm Callback for when the confirm button is clicked.
     * @param onDismiss Callback for when the dialog is dismissed.
     */
    @Composable
    fun NameInputDialog(
        nameInput: String,
        onNameChange: (String) -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.enter_name)) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    /**
     * Composable for displaying a loading shimmer placeholder.
     */
    @Composable
    fun LoadingShimmerPlaceholder() {
        val brush = shimmerBrush()
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
        )
    }

    companion object {
        private const val ARG_IMAGE_URI = "ARG_IMAGE_URI"
    }

}
