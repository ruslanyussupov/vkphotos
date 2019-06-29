package dev.iusupov.vkphotos.ext

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory

fun Bitmap.toRoundedDrawable(resources: Resources): Drawable {
    return RoundedBitmapDrawableFactory.create(resources, this).apply {
        isCircular = true
    }
}