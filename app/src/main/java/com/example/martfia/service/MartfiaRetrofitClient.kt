package com.example.martfia.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MartfiaRetrofitClient {
    private const val BASE_URL = "https://api.martfia.com/"  // 실제 API의 기본 URL로 변경하세요

    // Retrofit 인스턴스를 하나만 생성하여 모든 서비스에서 재사용하도록 설정
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 다양한 서비스 인터페이스 제공 함수
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
