package com.mehmetmertmazici.domaincheckercompose.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.regex.Pattern

class PhpWarningCleanupConverter private constructor(
    private val gson: Gson
) : Converter.Factory() {

    companion object {
        private const val TAG = "PhpWarningCleanup"

        private val PHP_WARNING_PATTERNS = listOf(
            Pattern.compile("<br\\s*/?>\\s*<b>Warning</b>:.*?<br\\s*/?>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL),
            Pattern.compile("<br\\s*/?>\\s*<b>Notice</b>:.*?<br\\s*/?>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL),
            Pattern.compile("<br\\s*/?>\\s*<b>Error</b>:.*?<br\\s*/?>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL),
            Pattern.compile("<br\\s*/?>\\s*<b>Fatal error</b>:.*?<br\\s*/?>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL),
            Pattern.compile("Warning:.*?\\n", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Notice:.*?\\n", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Error:.*?\\n", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Fatal error:.*?\\n", Pattern.CASE_INSENSITIVE)
        )

        fun create(): PhpWarningCleanupConverter {
            return PhpWarningCleanupConverter(
                GsonBuilder()
                    .create()
            )
        }
    }



    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        val delegate = GsonConverterFactory.create(gson)
            .responseBodyConverter(type, annotations, retrofit)
            ?: return null

        return Converter<ResponseBody, Any> { responseBody ->
            val originalContent = responseBody.string()
            Log.d(TAG, "Original response: $originalContent")

            // Clean PHP warnings from response
            val cleanedContent = cleanPhpWarnings(originalContent)
            Log.d(TAG, "Cleaned response: $cleanedContent")

            if (!cleanedContent.trim().startsWith("{") && !cleanedContent.trim().startsWith("[")) {
                Log.d(TAG, "Response is plain text, creating error response")

                val errorJson = """
                    {
                        "code": 0,
                        "status": "error",
                        "message": "$cleanedContent"
                    }
                """.trimIndent()

                val errorResponseBody = errorJson
                    .toResponseBody(responseBody.contentType())

                return@Converter delegate.convert(errorResponseBody)
            }

            val cleanedResponseBody = cleanedContent
                .toResponseBody(responseBody.contentType())

            try {
                delegate.convert(cleanedResponseBody)
            } catch (e: Exception) {
                Log.e(TAG, "JSON parsing failed for cleaned content: $cleanedContent", e)

                val jsonStart = cleanedContent.indexOf('{')
                val jsonEnd = cleanedContent.lastIndexOf('}')

                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    val jsonOnly = cleanedContent.substring(jsonStart, jsonEnd + 1)
                    Log.d(TAG, "Extracted JSON: $jsonOnly")

                    val jsonResponseBody = jsonOnly
                        .toResponseBody(responseBody.contentType())

                    try {
                        delegate.convert(jsonResponseBody)
                    } catch (e2: Exception) {
                        Log.e(TAG, "Manual JSON also failed", e2)

                        val fallbackJson = """
                            {
                                "code": 0,
                                "status": "error",
                                "message": "Response parsing failed: $cleanedContent"
                            }
                        """.trimIndent()

                        val fallbackResponseBody = fallbackJson
                            .toResponseBody(responseBody.contentType())

                        delegate.convert(fallbackResponseBody)
                    }
                } else {
                    val errorJson = """
                        {
                            "code": 0,
                            "status": "error", 
                            "message": "$cleanedContent"
                        }
                    """.trimIndent()

                    val errorResponseBody = ResponseBody.create(
                        responseBody.contentType(),
                        errorJson
                    )

                    delegate.convert(errorResponseBody)
                }
            }
        }
    }

    private fun cleanPhpWarnings(content: String): String {
        var cleaned = content.trim()

        PHP_WARNING_PATTERNS.forEach { pattern ->
            cleaned = pattern.matcher(cleaned).replaceAll("")
        }

        cleaned = cleaned.replace(Regex("<[^>]*>"), "")

        cleaned = cleaned.replaceFirst(Regex("^[\\s\\r\\n]*"), "")

        return cleaned.trim()
    }
}


class NetworkErrorInterceptor : Interceptor {
    companion object {
        private const val TAG = "NetworkError"
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        Log.d(TAG, "Making request to: ${request.url}")

        return try {
            val response = chain.proceed(request)
            Log.d(TAG, "Response code: ${response.code}")

            if (!response.isSuccessful) { // 200 -299 ?
                Log.w(TAG, "HTTP error: ${response.code} ${response.message}")
            }

            response
        } catch (e: Exception) {
            Log.e(TAG, "Network request failed", e)
            throw e
        }
    }
}
