package com.example.appgithubsearch.domain


data class Repository(
    val name: String,
    @SerializedName("html_url")
    val htmlUrl: String

) {
    annotation class SerializedName(val value: String)
}
