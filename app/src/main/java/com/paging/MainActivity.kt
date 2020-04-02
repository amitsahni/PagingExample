package com.paging

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val githubRepository: GithubRepository by inject()
    private val adapter = GithubAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.adapter = adapter
        val config = PagedList.Config.Builder()
            .setPageSize(30)
            .setInitialLoadSizeHint(30)
            .setEnablePlaceholders(false)
            .build()
        initializePagedListBuilder(
            config,
            GithubPagedDataSource(githubRepository) {
                adapter.hideLoader()
            }
        ).observe(this,
            Observer {
                adapter.submitList(it)
            })
    }
}