package com.example.app_vpn.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.databinding.ActivityMainBinding
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.ui.fragment.AccountFragment
import com.example.app_vpn.ui.fragment.VpnServerFragment
import com.example.app_vpn.ui.fragment.HomeFragment
import com.example.app_vpn.ui.viewmodel.UserViewModel
import com.example.app_vpn.util.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val TAG = "my_tag"

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var supabase: SupabaseClient

    private lateinit var binding: ActivityMainBinding
    val userViewModel by viewModels<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        setupInsets()
        setupNavigation()
        fetchData()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.navigation_view)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupNavigation() {
        val homeFragment = HomeFragment()
        val vpnServerFragment = VpnServerFragment()
        val accountFragment = AccountFragment()

        makeCurrentFragment(homeFragment)

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> makeCurrentFragment(homeFragment)
                R.id.nav_country -> makeCurrentFragment(vpnServerFragment)
                R.id.nav_account -> makeCurrentFragment(accountFragment)
            }
            true
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        Log.d("my_fragment", fragment.toString())
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }

    fun performLogout() = lifecycleScope.launch {
        userPreference.clear()
        supabase.auth.signOut()
        startNewActivity(LoginActivity::class.java)
    }

    private fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            userViewModel.fetchPremiumData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
    }

    // Ads setup and management (commented out section moved here)
    /*
    private fun setUpAds(showAds: Boolean) {
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)
        googleMobileAdsConsentManager.gatherConsent(this) { consentError ->
            if (consentError != null) {
                Log.w(TAG + "consent error", "consent error: ${consentError.errorCode}: ${consentError.message}")
            }

            if (googleMobileAdsConsentManager.canRequestAds && showAds) {
                initializeMobileAdsSdk()
            }
            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                invalidateOptionsMenu()
            }
        }

        if (googleMobileAdsConsentManager.canRequestAds && showAds) {
            initializeMobileAdsSdk()
        }

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR)).build()
        )
    }

    fun showInterstitial() {
        if (interstitialAd == null) {
            if (googleMobileAdsConsentManager.canRequestAds) {
                loadAd()
            }
        }

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.")
                interstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "Ad failed to show.")
                interstitialAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }

        interstitialAd?.show(this)
    }

    private fun loadAd() {
        if (adIsLoading || interstitialAd != null) return
        adIsLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.message)
                interstitialAd = null
                adIsLoading = false
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                interstitialAd = ad
                adIsLoading = false
            }
        })
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) return
        MobileAds.initialize(this@MainActivity) { loadAd() }
    }
    */
}
