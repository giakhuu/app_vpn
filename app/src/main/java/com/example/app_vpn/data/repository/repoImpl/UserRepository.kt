package com.example.app_vpn.data.repository.repoImpl

import android.util.Log
import com.example.app_vpn.data.entities.PremiumStatus
import com.example.app_vpn.data.entities.Subscription
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.network.api.UserApi
import com.example.app_vpn.data.network.api.UserServiceApi
import com.example.app_vpn.data.network.safeCall
import com.example.app_vpn.util.API_KEY
import com.example.app_vpn.util.getNewExpiredDate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.log

class UserRepository @Inject constructor(
    private val userApi: UserApi,
    private val supabase: SupabaseClient,
    private val userServiceApi: UserServiceApi
)  {

    suspend fun fetchPremiumData() = safeCall {
        userApi.getPremiumUserByUuid("Bearer $API_KEY", "eq.${supabase.auth.currentUserOrNull()?.id}")
    }

    suspend fun insertPremiumData() = safeCall {
        userServiceApi.addPremiumUser(PremiumStatus(supabase.auth.currentUserOrNull()!!.id, OffsetDateTime.now().toString()))
    }

    suspend fun updatePremiumData(subscription: Subscription){
        when(val checkData = fetchPremiumData()) {
            is Resource.Error -> {
                Log.d("updatePremiumData", ": ${checkData.error}")
            }
            Resource.Loading -> {}
            is Resource.Success -> {
                if (!checkData.data.isNullOrEmpty()) {
                    val oldPremiumStatus = checkData.data.first()
                    val newPremiumUser = PremiumStatus(
                        uuid = oldPremiumStatus.uuid,
                        expiredDate = getNewExpiredDate(oldPremiumStatus.expiredDate, subscription.duration)
                    )
                    userServiceApi.updatePremiumUser(
                        userPremiumStatus = newPremiumUser,
                        "eq.${supabase.auth.currentUserOrNull()?.id}"
                    )
                }
                else {
                    val newPremiumUser = PremiumStatus(
                        uuid = supabase.auth.currentUserOrNull()!!.id,
                        expiredDate = getNewExpiredDate(null, subscription.duration)
                    )

                    userServiceApi.addPremiumUser(
                        newPremiumUser
                    )
                }
            }
        }
    }
}