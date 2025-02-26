package com.example.englishgptapplication.logic.network

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * RetrofitClient 单例对象通过创建信任所有证书的 TrustManager、SSLContext 及忽略主机名验证的 HostnameVerifier，
 * 构建带请求路径区分添加 Cookie 拦截器的 OkHttpClient，
 * 进而创建基于 https://test.shiqu.zhilehuo.com/ 且使用 Gson 转换器的 Retrofit 实例，提供泛型方法来创建服务接口实例。
 */

object RetrofitClient {
    private const val BASE_URL = "https://test.shiqu.zhilehuo.com/"
    private const val COOKIE1 = "sid=OFvEbpyl4PyKkc/cSjl2tW3g5Ga/z5DPSQRGQn8mJBs="
    private const val COOKIE2 = "sid=i5VMMK2c7EEm5qK597kJeDqrel7NKCRqSQRGQn8mJBs="

    // 创建信任所有证书的 TrustManager
    private val trustAllCerts = arrayOf<TrustManager>(
        @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    )

    // 创建 SSLContext 并初始化
    private val sslContext: SSLContext = try {
        SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
    } catch (e: Exception) {
        throw RuntimeException(e)
    }

    // 创建忽略主机名验证的 HostnameVerifier
    private val allHostsValid = HostnameVerifier { _, _ -> true }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            // 根据请求路径选择对应的Cookie
            val cookie = if (originalRequest.url.encodedPath == "/knowledge/article/getArticleDetail") {
                COOKIE2
            } else {
                COOKIE1
            }
            val newRequest = originalRequest.newBuilder()
                .addHeader("Cookie", cookie)
                .build()
            chain.proceed(newRequest)
        }
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier(allHostsValid)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 接收一个 Class 对象作为参数，通过 Retrofit 来创建对应的服务接口实例。
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    // 内联函数，使用了具体化类型参数 reified，可以直接通过泛型来创建服务接口实例，无需显式传递 Class 对象。
    inline fun <reified T> create(): T = create(T::class.java)
}