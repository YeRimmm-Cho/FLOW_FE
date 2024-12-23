package com.example.martfia.service

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object MartfiaRetrofitClient {
    private const val BASE_URL = ""

    // OkHttpClient 설정 추가
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃 설정
        .readTimeout(60, TimeUnit.SECONDS)    // 읽기 타임아웃 설정
        .writeTimeout(60, TimeUnit.SECONDS)   // 쓰기 타임아웃 설정
        .build()

    // Retrofit 인스턴스를 하나만 생성하여 모든 서비스에서 재사용하도록 설정
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // OkHttpClient 사용
            .build()
    }

    // 다양한 서비스 인터페이스 제공 함수
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
