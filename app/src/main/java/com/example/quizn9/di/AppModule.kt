package com.example.quizn9.di

import com.example.quizn9.BuildConfig
import com.example.quizn9.data.remote.service.ClothesApiService
import com.example.taskn21.data.remote.common.HandleResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    @Provides
    @Singleton
    fun provideHandleResponse(): HandleResponse = HandleResponse()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val httpClint = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            httpClint.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        return httpClint.build()
    }

    @Provides
    @Singleton
    fun provideClothesRetrofitClient(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.MOCKY_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideClothesApiService(retrofit: Retrofit): ClothesApiService {
        return retrofit.create(ClothesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideIODispatcher() = Dispatchers.IO
}