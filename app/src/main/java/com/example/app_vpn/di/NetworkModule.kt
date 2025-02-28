package com.example.app_vpn.di

import android.content.Context
import com.example.app_vpn.data.entities.Payment
import com.example.app_vpn.data.network.RemoteDataSource
import com.example.app_vpn.data.network.api.PaymentApi
import com.example.app_vpn.data.network.api.VpnServerApi
import com.example.app_vpn.data.network.api.PaypalPaymentApi
import com.example.app_vpn.data.network.api.SubscriptionApi
import com.example.app_vpn.data.network.api.UserApi
import com.example.app_vpn.data.network.api.UserServiceApi
import com.example.app_vpn.util.API_KEY
import com.example.app_vpn.util.SUPABASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun provideSupabaseClient() : SupabaseClient {
        val supabaseClient = createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = API_KEY,
        ) {
            install(Auth) {
                autoLoadFromStorage = true // 🔥 Tự động load session từ storage
                alwaysAutoRefresh = true
            }
            install(Storage) {
                // settings
            }
        }

        return supabaseClient
    }

    @Singleton
    @Provides
    fun provideUserApi(
        remoteDataSource: RemoteDataSource,
        @ApplicationContext context: Context
    ): UserApi {
        return remoteDataSource.buildApi(UserApi::class.java, )
    }

    @Singleton
    @Provides
    fun provideCountryApi(
        remoteDataSource: RemoteDataSource,
        @ApplicationContext context: Context
    ) : VpnServerApi {
        return remoteDataSource.buildApi(VpnServerApi::class.java,)
    }

    @Singleton
    @Provides
    fun providePaypalPaymentApi(
        remoteDataSource: RemoteDataSource,
    ) : PaypalPaymentApi {
        return remoteDataSource.buildPaypalApi(PaypalPaymentApi::class.java)
    }

    @Singleton
    @Provides
    fun provideSubscriptionApi(
        remoteDataSource: RemoteDataSource,
    ) : SubscriptionApi {
        return remoteDataSource.buildApi(SubscriptionApi::class.java, )
    }

    @Singleton
    @Provides
    fun providePaymentApi(
        remoteDataSource: RemoteDataSource,
    ) : PaymentApi{
        return remoteDataSource.buildApiSecret(PaymentApi::class.java)
    }

    @Singleton
    @Provides
    fun provideUserServiceApi(
        remoteDataSource: RemoteDataSource
    ) : UserServiceApi {
        return remoteDataSource.buildApiSecret(UserServiceApi::class.java)
    }
}