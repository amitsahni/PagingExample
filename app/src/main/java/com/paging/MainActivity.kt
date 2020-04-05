package com.paging

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val githubRepository: GithubRepository by inject()
    private val adapter = GithubAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dataSourceFactory = ConcertTimeDataSourceFactory()
        recyclerView.adapter = adapter
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(10)
            .setEnablePlaceholders(false)
            .build()
        /*initializePagedListBuilder(
            config,
            GithubPagedDataSource(githubRepository) {
                adapter.hideLoader()
            }
        ).observe(this, Observer {
            adapter.submitList(it)
        })*/

        val listBuilder = LivePagedListBuilder(dataSourceFactory, config).build()
        listBuilder.observe(this, Observer {

            adapter.submitList(it)
        })
        swipeRefreshLayout.setOnRefreshListener {
            /*listBuilder.value?.dataSource?.addInvalidatedCallback {
                //adapter.submitList(null)
                adapter.notifyDataSetChanged()
            }
            listBuilder.value?.dataSource?.invalidate()*/
            adapter.currentList?.dataSource?.invalidate()
            adapter.submitList(null)
            adapter.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        }

        adapter.click { i, items ->
            adapter.currentList?.dataSource?.invalidate()
            adapter.submitList(null)
            adapter.notifyDataSetChanged()
        }
    }

    inner class ConcertTimeDataSourceFactory :
        DataSource.Factory<Int, Items>() {
        //val sourceLiveData = MutableLiveData<GithubPagedDataSource>()
        override fun create(): DataSource<Int, Items> {
            val source = GithubPagedDataSource(githubRepository) {
                adapter.hideLoader()
            }
            //sourceLiveData.postValue(source)
            return source
        }


    }

}