/**
 * Created by Amit Singh on 28/03/20.
 * Tila
 * asingh@tila.com
 */

package com.paging

import org.koin.dsl.module

val module = module {
    single { RetrofitManager.retrofit("https://api.github.com/") }
    single { GithubService.create(get()) }
    single { GithubRepositoryImpl(get()) as GithubRepository }
}