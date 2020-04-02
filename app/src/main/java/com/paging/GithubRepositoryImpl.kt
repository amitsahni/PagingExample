package com.paging

import retrofit2.Response


/**
 * Created by Amit Singh on 28/03/20.
 * Tila
 * asingh@tila.com
 */

class GithubRepositoryImpl(private val githubService: GithubService) : GithubRepository {
    override suspend fun searchRepos(map: Map<String, String>): Response<RepoModel> {
        return githubService.searchRepos(map)
    }
}