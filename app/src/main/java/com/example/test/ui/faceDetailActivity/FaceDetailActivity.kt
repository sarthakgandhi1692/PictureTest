package com.example.test.ui.faceDetailActivity

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.scale
import com.example.test.R
import com.example.test.composeCommon.shimmerBrush
import com.example.test.model.local.FaceInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FaceDetailActivity : ComponentActivity() {

    private val viewModel: FaceDetailActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageUri = intent.getStringExtra("imageUri")
        viewModel.getProcessedImage(imageUri!!)

        setContent {
            Box(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .background(Color.Companion.White)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().height(56.dp)) {
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
                            text = "Image details",
                            modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterVertically),
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }

                    FaceDetailScreen(
                        onNameUpdated = { index, name ->
                            viewModel.saveName(index, name)
                        })
                }

            }
        }
    }

    @Composable
    fun FaceDetailScreen(
        onNameUpdated: (FaceInfo, String) -> Unit
    ) {
        val originalBitmap = viewModel.imageState.value?.bitmap
        var scaledBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var selectedFace by remember { mutableStateOf<FaceInfo?>(null) }
        var nameInput by remember { mutableStateOf("") }
        val density = LocalDensity.current

        val brush = shimmerBrush()
        if (originalBitmap == null) {
            Box(
                modifier = Modifier.Companion
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            return
        }

        BoxWithConstraints(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .fillMaxHeight()

        )
        {
            val maxWidth = this.maxWidth

            val widthPx = with(LocalDensity.current) { maxWidth.toPx() }

            LaunchedEffect(originalBitmap, maxWidth) {
                withContext(Dispatchers.IO) {
                    val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height
                    val targetHeightPx = (widthPx / aspectRatio).toInt()
                    val downscaled = originalBitmap.scale(widthPx.toInt(), targetHeightPx)
                    scaledBitmap = downscaled
                }
            }

            scaledBitmap?.let { bitmap ->
                val originalWidth = originalBitmap.width
                val originalHeight = originalBitmap.height
                val scaledWidth = bitmap.width
                val scaledHeight = bitmap.height

                val scaleX = scaledWidth.toFloat() / originalWidth
                val scaleY = scaledHeight.toFloat() / originalHeight

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "",
                    contentScale = ContentScale.Companion.Crop
                )

                viewModel.imageState.value?.faces?.forEachIndexed { index, face ->
                    val rect = face.boundingBox
                    val scaledLeft = rect.left * scaleX
                    val scaledTop = rect.top * scaleY
                    val scaledWidthRect = rect.width() * scaleX
                    val scaledHeightRect = rect.height() * scaleY

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(scaledLeft.toInt(), scaledTop.toInt())
                            }
                            .size(
                                with(density) { scaledWidthRect.toDp() },
                                with(density) { scaledHeightRect.toDp() }
                            )
                            .background(Color.Transparent)
                            .clickable {
                                selectedFace = face
                                nameInput = face.name ?: ""
                                Log.e("Face Clicked", "Face Clicked: $face")
                            }
                    ) {
                        face.name?.let { name ->
                            Text(
                                text = name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(2.dp)
                                    .background(Color(0x88000000), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }


            selectedFace?.let { face ->
                AlertDialog(
                    onDismissRequest = { selectedFace = null },
                    title = { Text("Enter Name") },
                    text = {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Name") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onNameUpdated(selectedFace!!, nameInput)
                            selectedFace = null
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedFace = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

        }
    }

}
