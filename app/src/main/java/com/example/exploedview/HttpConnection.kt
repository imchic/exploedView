package com.example.exploedview

import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object HttpConnection {

    private val _client = OkHttpClient()
    private val _request = Request.Builder()

    val JSON ="application/json; charset=utf-8".toMediaTypeOrNull()
    val IMAGE ="image/jpeg".toMediaTypeOrNull()
    val FILE ="multipart/form-data".toMediaTypeOrNull()

    /**
     * Http Post (JSON)
     * @return Json
     */
    fun post(map: Map<String, String>, httpUrl: String, callback: Callback) {

        try {

            val jsonObject = JSONObject()

            if(map.isNotEmpty()) {
                map.forEach { data ->
                    jsonObject.put(data.key, data.value)
                }

                _request
                    .url(httpUrl)
                    .post(jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
                    .build()

                _client.newCall(_request.build()).enqueue(callback)
            }
        } catch (e: Exception) {
            throw IllegalAccessException (e.toString())
        }

    }

}