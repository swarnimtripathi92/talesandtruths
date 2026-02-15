package com.kidverse.app

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

fun ImageView.loadFast(url: String?, placeholder: Int = R.drawable.placeholder_story, centerCrop: Boolean = true) {
    val request = Glide.with(context)
        .load(url)
        .thumbnail(0.2f)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .placeholder(placeholder)
        .error(placeholder)
        .transition(DrawableTransitionOptions.withCrossFade(120))

    if (centerCrop) {
        request.centerCrop().into(this)
    } else {
        request.into(this)
    }
}
