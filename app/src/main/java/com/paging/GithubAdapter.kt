package com.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PageKeyedDataSource
import androidx.recyclerview.widget.DiffUtil
import kotlinx.android.synthetic.main.item_loading.view.*
import kotlinx.android.synthetic.main.item_repo.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


/**
 * Created by Amit Singh on 28/03/20.
 * Tila
 * asingh@tila.com
 */

class GithubAdapter :
    BaseFooterPagedListAdapter<GithubAdapter.MyItemViewHolder, GithubAdapter.MyFooterViewHolder, Items>() {

    inner class MyItemViewHolder(val v: View) : HolderContainer<Items>(v) {
        override fun bind(position: Int, item: Items) {
            v.name.text = item.name
            v.desc.text = item.description
            v.setOnClickListener {
                notifyDataSetChanged()
            }
        }
    }

    inner class MyFooterViewHolder(val v: View) : HolderContainer<Items>(v) {

        override fun bind(position: Int, item: Items) {
            if (useFooter) {
                v.progressBar.visibility = View.VISIBLE
            } else {
                v.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onCreateFooterViewHolder(viewGroup: ViewGroup, viewType: Int): MyFooterViewHolder {
        return MyFooterViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_loading, viewGroup, false)
        )
    }

    override fun onBindFooterViewHolder(holder: MyFooterViewHolder, position: Int) {
        if (useFooter) {
            holder.itemView.progressBar.visibility = View.VISIBLE
        } else {
            holder.itemView.progressBar.visibility = View.GONE
        }
    }

    override fun onCreateItemViewHolder(viewGroup: ViewGroup, viewType: Int): MyItemViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_repo, viewGroup, false)
        return MyItemViewHolder(view)
    }

    override fun onBindItemViewHolder(holder: MyItemViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(position, it)
        }
    }


}

class DiffUtilCallBack : DiffUtil.ItemCallback<Items>() {
    override fun areItemsTheSame(oldItem: Items, newItem: Items): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Items, newItem: Items): Boolean {
        return oldItem.id == newItem.id
    }

}

class GithubPagedDataSource(private val githubRepository: GithubRepository, val error: () -> Unit) :
    PageKeyedDataSource<Int, Items>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Items>
    ) {
        val map = mapOf(
            "q" to "apple",
            "page" to "0",
            "per_page" to "${params.requestedLoadSize}"
        )
        Timber.d(map.toString())
        CoroutineScope(Dispatchers.IO).launch {
            val result = githubRepository.searchRepos(map)
            result.run {
                if (isSuccessful) {
                    this.body()?.let {
                        callback.onResult(it.items, 0, it.total, 0, 1)
                    }
                } else {
                    this.errorBody()?.let {
                        Timber.d(it.toString())
                        CoroutineScope(Dispatchers.Main).launch {
                            error()
                        }
                    }
                }
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Items>) {
        val map = mapOf(
            "q" to "apple",
            "page" to params.key.toString(),
            "per_page" to "${params.requestedLoadSize}"
        )
        Timber.d(map.toString())
        CoroutineScope(Dispatchers.IO).launch {
            val result = githubRepository.searchRepos(map)
            result.run {
                if (isSuccessful) {
                    this.body()?.let {
                        callback.onResult(it.items, params.key + 1)
                    }
                } else {
                    this.errorBody()?.let {
                        Timber.d(it.toString())
                        CoroutineScope(Dispatchers.Main).launch {
                            error()
                        }
                    }
                }
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Items>) {
        /*val map = mapOf(
            "q" to "apple",
            "page" to params.key,
            "per_page" to "${params.requestedLoadSize}"
        )
        Timber.d(map.toString())
        CoroutineScope(Dispatchers.IO).launch {
            val result = githubRepository.searchRepos(map)
            result.run {
                if (isSuccessful) {
                    this.body()?.let {
                        Timber.d(it.toString())
                        callback.onResult(it.items, params.key + 1)
                    }
                } else {
                    this.errorBody()?.let {
                        Timber.d(it.toString())
                    }
                }
            }
        }*/
    }
}