package com.paging

import retrofit2.Response


/**
 * Created by Amit Singh on 28/03/20.
 * Tila
 * asingh@tila.com
 */

interface GithubRepository {

    suspend fun searchRepos(map: Map<String, String>): Response<RepoModel>
}