package com.paging

//import retrofit2.converter.scalars.ScalarsConverterFactory
//import okhttp3.logging.HttpLoggingInterceptor
import android.annotation.SuppressLint
import android.os.Build
import androidx.lifecycle.MutableLiveData
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import timber.log.Timber
import java.net.URLDecoder
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitManager {

    val success = MutableLiveData<String>()
    val error = MutableLiveData<String>()
    val failure = MutableLiveData<String>()

    //val httpCodeLiveData = MutableLiveData<Event<Int>>()
    private val LINE_SEPARATOR = System.getProperty("line.separator")!!

    private val isDebug = BuildConfig.DEBUG

    /*private val gson = GsonBuilder()
        .enableComplexMapKeySerialization()
        .serializeNulls()
        .setDateFormat(DateFormat.LONG)
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .setPrettyPrinting()
        .create()

    private val factory = GsonConverterFactory.create(gson)


    private val okHttpInterceptor = run {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.apply {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }
    }*/

    //private val refreshToken = RefreshToken()
    private val loggerInterceptor = LoggerInterceptor()
    private val globalHandlerInterceptor = GlobalHandler()
    private val ioInterceptor = IO()

    private val contentType = "application/json".toMediaType()
    private val okHttpClient =
        (if (BuildConfig.DEBUG) getUnsafeOkHttpClient() else OkHttpClient().newBuilder())
            .connectTimeout(10.toLong(), TimeUnit.SECONDS)
            .readTimeout(30.toLong(), TimeUnit.SECONDS)
            .writeTimeout(30.toLong(), TimeUnit.SECONDS)
            //.addInterceptor(refreshToken)
            .addInterceptor(UserAgentInterceptor())
            .addInterceptor(globalHandlerInterceptor)
            .addInterceptor(loggerInterceptor)
            .addInterceptor(ioInterceptor)
            .build()

    fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(baseUrl)
        //.addConverterFactory(factory)
        //.addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(
            Json(
                JsonConfiguration(ignoreUnknownKeys = true, prettyPrint = true, isLenient = true)
            ).asConverterFactory(contentType)
        )
        .build()

    fun OkHttpClient.Builder.headers(headers: Map<String, String> = emptyMap()): OkHttpClient.Builder {
        if (headers.isNotEmpty()) {
            addInterceptor {
                val builder = it.request().newBuilder()
                headers.forEach { (key, value) ->
                    builder.addHeader(key, value)
                }
                it.proceed(builder.build())
            }
        }
        return this
    }

    private class UserAgentInterceptor : Interceptor {

        private val userAgent: String by lazy {
            buildUserAgent()
        }

        override fun intercept(chain: Interceptor.Chain): Response {
            val builder = chain.request().newBuilder()
            builder.header("User-Agent", userAgent)
            return chain.proceed(builder.build())
        }

        private fun buildUserAgent(): String {
            val versionName = BuildConfig.VERSION_NAME
            val versionCode = BuildConfig.VERSION_CODE
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val version = Build.VERSION.SDK_INT
            val versionRelease = Build.VERSION.RELEASE
            val installerName = BuildConfig.APPLICATION_ID
            return "$installerName / $versionName($versionCode); ($manufacturer; $model; SDK $version; Android $versionRelease)"
        }
    }

    private class LoggerInterceptor : Interceptor {

        @SuppressLint("TimberArgCount")
        override fun intercept(chain: Interceptor.Chain): Response {
            if (!isDebug) {
                return chain.proceed(chain.request())
            }
            Timber.d(

                "╔════ Request ════════════════════════════════════════════════════════════════════════════"
            )
            chain.request().let {
                Timber.d("║ URL: ${it.url}")
                if (it.url.querySize > 0) {
                    Timber.d("║ URL Decode:")
                    URLDecoder.decode(it.url.toString(), "UTF-8").split(LINE_SEPARATOR.toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                        .forEach {
                            Timber.d("║ $it")
                        }
                }
                Timber.d("║ ")
                Timber.d("║ Method: @${it.method}")
                if (it.headers.size > 0) {
                    Timber.d("║ ")
                    Timber.d("║ Headers: ")
                }
                for (i in 0 until it.headers.size) {
                    Timber.d("║   ─ ${it.headers.name(i)} : ${it.headers.value(i)}")
                }
                if (it.url.querySize > 0) {
                    Timber.d("║ ")
                    Timber.d("║ Query: ")
                    for (i in 0 until it.url.querySize) {
                        Timber.d(
                            "║   ─ ${it.url.queryParameterName(i)} : ${it.url.queryParameterValue(
                                i
                            )}"
                        )
                    }
                }
                it.body?.let { body ->
                    if (body !is MultipartBody) {
                        Timber.d("║ ")
                        if (body.contentLength() > 0)
                            Timber.d("║ Body:")
                        val buffer = okio.Buffer()
                        body.writeTo(buffer)
                        buffer.readUtf8().split(LINE_SEPARATOR.toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                            .forEach {
                                try {
                                    Timber.d("║ ${URLDecoder.decode(it, "UTF-8")}")
                                } catch (e: java.lang.Exception) {
                                    Timber.e(e)
                                }
                            }
                    }

                }
            }
            Timber.d(

                "╚═══════════════════════════════════════════════════════════════════════════════════════"
            )
            val startNs = System.nanoTime()
            val response = chain.proceed(chain.request())
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            val source = response.body?.source()
            source?.request(Long.MAX_VALUE)
            source?.buffer?.clone()?.readUtf8()?.apply {
                Timber.d(

                    "╔════ Response ═══════════════════════════════════════════════════════════════════════════"
                )
                Timber.d("║ URL: ${response.request.url}")
                Timber.d("║ ")
                Timber.d(

                    "║ Status Code: ${response.code} / ${response.message} - $tookMs ms"
                )
                val size = response.headers.size
                if (size > 0) {
                    Timber.d("║ ")
                    Timber.d("║ Headers: ")
                }
                val headers = response.headers
                for (i in 0 until size) {
                    Timber.d("║   ─ ${headers.name(i)} : ${headers.value(i)}")
                }
                Timber.d("║ ")
                Timber.d("║ ")
                try {
                    if (this?.startsWith("{")) {
                        JSONObject(this).toString(1)?.let {
                            val lines =
                                it.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            for (line in lines) {
                                Timber.d("║ $line")
                            }
                            // Log.d("", it)

                        }
                    } else {
                        JSONArray(this).toString(1)?.let {
                            val lines =
                                it.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            lines.forEach {
                                Timber.d("║ $it")
                            }
                            //Log.d("", it)
                        }
                    }
                    Timber.d("║ $this")
                } catch (e: Exception) {
                    Timber.e("║ ${e.message}")
                }
                Timber.d(

                    "╚═══════════════════════════════════════════════════════════════════════════════════════"
                )
            }
            return response
        }

    }

    private class GlobalHandler : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder().build()
            val response = chain.proceed(request)
            //httpCodeLiveData.postValue(Event(response.code))
            if (response.isSuccessful) {
                val source = response.body?.source()
                source?.request(java.lang.Long.MAX_VALUE)
                source?.buffer?.clone()?.readUtf8()?.apply {
                    success.postValue(this)
                }
            } else {
                val source = response.body?.source()
                source?.request(Long.MAX_VALUE)
                source?.buffer?.clone()?.readUtf8()?.apply {
                    error.postValue(this)
                }
            }
            return response
        }

    }

    private class IO : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            try {
                val request = chain.request().newBuilder().build()
                return chain.proceed(request)
            } catch (e: Exception) {
                failure.postValue(e.toString())
            }
            return chain.proceed(chain.request())
        }

    }


    /*private class RefreshToken : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder().build()
            val response = chain.proceed(request)
            when (response.code) {
                401 -> {
                    val map = mutableMapOf<String, String>()
                    map["refresh_token"] = TilaSharedPreference.refreshToken ?: ""
                    val call = retrofit(AppConstants.API_GATEWAY_BASE_URL).create(AuthService::class.java).makeRefreshCall(map)
                    val body = call.execute().body()
                    body?.let {
                        TilaSharedPreference.setValue(TilaSharedPreference.PREF_KEY_ACCESS_TOKEN, body.access_token
                                ?: "", isGlobalPref = false)
                        val requestBuilder = chain.request().newBuilder().header(TilaWebServiceConstants.KEY_X_ACCESS_TOKEN, TilaSharedPreference.accessToken
                                ?: "")
                        return chain.proceed(requestBuilder.build())
                    }
                }
                403 -> {

                }
            }
            return response
        }

    }*/

    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
            return builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}