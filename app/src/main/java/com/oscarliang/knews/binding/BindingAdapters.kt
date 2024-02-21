package com.oscarliang.knews.binding

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

object BindingAdapters {

    @JvmStatic
    @BindingAdapter("showHide")
    fun showHide(view: View, show: Boolean) {
        view.isVisible = show
    }

    @JvmStatic
    @BindingAdapter("refreshHide")
    fun refreshHide(view: SwipeRefreshLayout, isRefreshing: Boolean) {
        if (view.isRefreshing) {
            view.isRefreshing = isRefreshing
        }
    }

}
