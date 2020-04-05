package com.paging

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer


/**
 * Created by Amit Singh on 28/03/20.
 * Tila
 * asingh@tila.com
 */

abstract class BasePagedListAdapter<VH : RecyclerView.ViewHolder, T> :
    PagedListAdapter<T, RecyclerView.ViewHolder>(DiffCallback()) {
    var clickListener: ((Int, T) -> Unit?)? = null
    var longClickListener: ((Int, T) -> Unit?)? = null
    internal val clickPosition = MutableLiveData<Item<T>>()

    fun click(f: (Int, T) -> Unit) {
        clickListener = f
    }

    fun longClick(f: (Int, T) -> Unit) {
        longClickListener = f
    }
}

data class Item<T>(
    val position: Int,
    val model: T
)

abstract class HolderContainer<T>(val view: View) : RecyclerView.ViewHolder(view), LayoutContainer {

    override val containerView: View?
        get() = view

    abstract fun bind(position: Int, item: T)
}

private class DiffCallback<T> :
    DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem === newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

}