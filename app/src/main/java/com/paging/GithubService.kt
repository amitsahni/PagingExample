package com.paging

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.QueryMap


/**
 * Created by Amit Singh on 28/03/20.
 * Tila
 * asingh@tila.com
 */

interface GithubService {

    @GET("search/repositories?sort=stars")
    suspend fun searchRepos(
        @QueryMap map: Map<String, String>
//        @Query("q") query: String,
//        @Query("page") page: Int,
//        @Query("per_page") itemsPerPage: Int
    ): Response<RepoModel>


    companion object {
        //private const val BASE_URL = "https://api.github.com/"

        fun create(retrofit: Retrofit): GithubService {
            return retrofit.create(GithubService::class.java)
        }
    }
}