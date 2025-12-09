package com.mehmetmertmazici.domaincheckercompose.utils

import android.content.Context
import com.google.gson.Gson

object DomainExtensions {
    private var extensionsData: ExtensionsData? = null

    fun loadExtensions(context: Context): ExtensionsData {
        if (extensionsData == null) {
            val jsonString = context.assets.open("domain_extensions.json").bufferedReader().use {
                it.readText()
            }
            extensionsData = Gson().fromJson(jsonString, ExtensionsData::class.java)
        }
        return extensionsData!!
    }

    fun getAllExtensions(context: Context): Set<String> {
        val data = loadExtensions(context)
        return (data.popular + data.international + data.country + data.turkey).toSet()
    }

    fun getTwoPartExtensions(context: Context): Set<String> {
        val data = loadExtensions(context)
        return data.turkey.toSet() // .com.tr, .net.tr gibi
    }
}

data class ExtensionsData(
    val popular: List<String>,
    val international: List<String>,
    val country: List<String>,
    val turkey: List<String>
)