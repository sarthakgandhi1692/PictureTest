package com.example.test.utils

import android.graphics.Bitmap
import com.example.test.model.local.FaceInfo

/**
 * Utility class for image manipulation.
 */
class ImageUtils {

    /**
     * Draws rectangles around detected faces on a bitmap.
     * @param bitmap The original bitmap.
     * @param faces A list of FaceInfo objects representing the detected faces and their bounding boxes.
     * @return A new bitmap with rectangles drawn around the faces.
     */
    fun drawFacesOnBitmap(bitmap: Bitmap, faces: List<FaceInfo>): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(mutableBitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.RED
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 5f
        }

        for (face in faces) {
            canvas.drawRect(face.boundingBox, paint)
        }

        return mutableBitmap
    }
}