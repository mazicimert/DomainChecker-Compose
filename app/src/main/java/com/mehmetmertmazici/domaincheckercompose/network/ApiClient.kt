package com.mehmetmertmazici.domaincheckercompose.network

import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://devik.eurovdc.eu/"
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    // Session/Cookie yönetimi için in-memory cookie store
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val host = url.host
            if (cookieStore[host] == null) {
                cookieStore[host] = mutableListOf()
            }

            // Mevcut cookie'leri güncelle veya yeni eklemek için
            cookies.forEach { newCookie ->
                cookieStore[host]?.removeAll { it.name == newCookie.name }
                cookieStore[host]?.add(newCookie)
                Log.d("COOKIE_DEBUG", "📥 GELEN COOKIE ($host): ${newCookie.name} = ${newCookie.value}")
            }



            Log.d("ApiClient", "Saved ${cookies.size} cookies for $host")
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val host = url.host
            val cookies = cookieStore[host] ?: emptyList()

            // Süresi dolmuş cookie'leri temizle
            val validCookies = cookies.filter { !it.expiresAt.let { exp -> exp < System.currentTimeMillis() } }

            // --- YENİ EKLENECEK KISIM (Giden Cookie'leri Gör) ---
            if (validCookies.isNotEmpty()) {
                validCookies.forEach { cookie ->
                    Log.d("COOKIE_DEBUG", "📤 GİDEN COOKIE ($host): ${cookie.name} = ${cookie.value}")
                }
            } else {
                Log.d("COOKIE_DEBUG", "❌ GİDEN COOKIE YOK ($host) - Liste boş")
            }


            Log.d("ApiClient", "Loading ${validCookies.size} cookies for $host")
            return validCookies
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Session header interceptor
    private val sessionInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()

        // Session token varsa header'a ekle
        val token = SessionHolder.token
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        if (originalRequest.body?.contentType()?.toString()?.contains("json") == true) {
            requestBuilder.addHeader("Content-Type", "application/json")
        }

        requestBuilder.addHeader("Accept", "application/json")

        chain.proceed(requestBuilder.build())
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .cookieJar(cookieJar)
        .addInterceptor(sessionInterceptor)
        .addInterceptor(NetworkErrorInterceptor())
        .addInterceptor(loggingInterceptor)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(ScalarsConverterFactory.create()) // String yanıtlar için
        .addConverterFactory(GsonConverterFactory.create()) // JSON için
        .addConverterFactory(PhpWarningCleanupConverter.create()) // PHP uyarıları temizleme
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    // Session temizleme (logout sırasında kullanılır)
    fun clearSession() {
        cookieStore.clear()
        SessionHolder.clear()
        Log.d("ApiClient", "Session cleared")
    }

    // Oturum durumunu kontrol et
    fun isLoggedIn(): Boolean {
        return !SessionHolder.token.isNullOrEmpty() || cookieStore.isNotEmpty()
    }
}

// Basit in-memory session holder
object SessionHolder {
    var token: String? = null
    var userId: Int? = null
    var userEmail: String? = null
    var userName: String? = null

    fun clear() {
        token = null
        userId = null
        userEmail = null
        userName = null
    }

    /**
     * Login response'undan session bilgilerini set eder
     * Backend message içinde veya user objesinde kullanıcı bilgilerini gönderebilir
     */
    fun setFromLogin(response: com.mehmetmertmazici.domaincheckercompose.model.LoginResponse) {
        token = response.token
        // Yeni helper property'leri kullan - message veya user'dan otomatik alır
        userId = response.userId
        userEmail = response.user?.email
        userName = buildString {
            append(response.userName ?: "")
            if (response.userSurname != null) {
                if (isNotEmpty()) append(" ")
                append(response.userSurname)
            }
        }.trim().ifEmpty { null }
    }

    /**
     * Register response'undan session bilgilerini set eder
     */
    fun setFromRegister(response: com.mehmetmertmazici.domaincheckercompose.model.RegisterResponse) {
        token = response.token
        userId = response.user?.id
        userEmail = response.user?.email
        userName = response.user?.let { user ->
            "${user.name ?: ""} ${user.surname ?: ""}".trim().ifEmpty { null }
        }
    }
}