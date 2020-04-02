/**
 * Created by Amit Singh on 28/03/20.
 * Tila
 * asingh@tila.com
 */

package com.paging

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoModel(
    @SerialName("total_count") val total: Int = 0,
    @SerialName("items") val items: List<Items> = emptyList()
)

@Serializable
data class Items(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("description") val description: String?,
    @SerialName("html_url") val url: String,
    @SerialName("stargazers_count") val stars: Int,
    @SerialName("forks_count") val forks: Int,
    @SerialName("language") val language: String?
)
