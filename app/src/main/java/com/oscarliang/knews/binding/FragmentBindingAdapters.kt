package com.oscarliang.knews.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.oscarliang.knews.R
import com.oscarliang.knews.testing.OpenForTesting

@OpenForTesting
class FragmentBindingAdapters {

    @BindingAdapter(value = ["imageUrl"])
    fun bindImage(imageView: ImageView, url: String?) {
        Glide.with(imageView.context)
            .load(url)
            .placeholder(R.drawable.ic_knews_gray)
            .error(R.drawable.ic_error)
            .into(imageView)
    }

}
