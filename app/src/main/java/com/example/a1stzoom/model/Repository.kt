package com.example.a1stzoom.model

import com.google.gson.annotations.SerializedName

data class Repository(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("html_url") var htmlUrl: String? = null,
    @SerializedName("description") var description: String? = null,
)