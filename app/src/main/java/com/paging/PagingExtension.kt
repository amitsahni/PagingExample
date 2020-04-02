/**
 * Created by Amit Singh on 28/03/20.
 * Tila
 * asingh@tila.com
 */

package com.paging

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

fun <K, V> initializePagedListBuilder(
    config: PagedList.Config,
    body: DataSource<K, V>
): LiveData<PagedList<V>> {
    val dataSourceFactory = object : DataSource.Factory<K, V>() {
        override fun create(): DataSource<K, V> {
            return body
        }
    }
    return LivePagedListBuilder(dataSourceFactory, config).build()
}