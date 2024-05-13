package com.example.app_vpn.di

import android.content.Context
import com.example.app_vpn.data.network.api.AuthApi
import com.example.app_vpn.data.network.RemoteDataSource
import com.example.app_vpn.data.network.api.CountryApi
import com.example.app_vpn.data.network.api.MailApi
import com.example.app_vpn.data.network.api.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideAuthApi(
        remoteDataSource: RemoteDataSource,
        @ApplicationContext context: Context
    ): AuthApi {
        return remoteDataSource.buildApi(AuthApi::class.java, context)
    }

    @Singleton
    @Provides
    fun provideUserApi(
        remoteDataSource: RemoteDataSource,
        @ApplicationContext context: Context
    ): UserApi {
        return remoteDataSource.buildApi(UserApi::class.java, context)
    }

    @Singleton
    @Provides
    fun provideMailApi(
        remoteDataSource: RemoteDataSource,
        @ApplicationContext context: Context
    ) : MailApi {
        return remoteDataSource.buildApi(MailApi::class.java, context)
    }

    @Singleton
    @Provides
    fun provideCountryApi(
        remoteDataSource: RemoteDataSource,
        @ApplicationContext context: Context
    ) : CountryApi {
        return remoteDataSource.buildApi(CountryApi::class.java, context)
    }
}