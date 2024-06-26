package com.example.app_vpn.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.databinding.ActivityMainBinding
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.ui.fragment.AD_UNIT_ID
import com.example.app_vpn.ui.fragment.AccountFragment
import com.example.app_vpn.ui.fragment.CountryFragment
import com.example.app_vpn.ui.fragment.HomeFragment
import com.example.app_vpn.ui.viewmodel.UserViewModel
import com.example.app_vpn.util.GoogleMobileAdsConsentManager
import com.example.app_vpn.util.startNewActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val TAG = "my_tag"

    @Inject
    lateinit var userPreference: UserPreference

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    val userViewModel by viewModels<UserViewModel>()
    private var interstitialAd: InterstitialAd? = null
    private var adIsLoading: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

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

        setUpAds(true)

        val homeFragment = HomeFragment()
        val countryFragment = CountryFragment()
        val accountFragment = AccountFragment()

        makeCurrentFragment(homeFragment)

        // Click bottom navigation di chuyển fragment
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> makeCurrentFragment(homeFragment)
                R.id.nav_country -> makeCurrentFragment(countryFragment)
                R.id.nav_account -> makeCurrentFragment(accountFragment)
            }
            true
        }
    }

    // Thay đổi fragment
    private fun makeCurrentFragment(fragment: Fragment) {
        Log.d("my_fragment", fragment.toString())
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }

    fun performLogout() = lifecycleScope.launch {
        userPreference.clear()
        startNewActivity(LoginActivity::class.java)
    }

    private fun setUpAds(showAds: Boolean) {
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)
        googleMobileAdsConsentManager.gatherConsent(this) { consentError ->
            if (consentError != null) {
                Log.w(
                    TAG + "consent error",
                    "consent error: ${consentError.errorCode}: ${consentError.message}"
                )
            }

            if (googleMobileAdsConsentManager.canRequestAds && showAds) {
                initializeMobileAdsSdk()
            }
            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                // Regenerate the options menu to include a privacy setting.
                invalidateOptionsMenu()
            }
        }

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds && showAds) {
            initializeMobileAdsSdk()
        }

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
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
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                interstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "Ad failed to show.")
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                interstitialAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                // Called when ad is dismissed.
            }
        }

        interstitialAd?.show(this)
    }

    private fun loadAd() {
        // Request a new ad if one isn't already loaded.
        if (adIsLoading || interstitialAd != null) {
            return
        }
        adIsLoading = true

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    interstitialAd = null
                    adIsLoading = false
                    val error =
                        "domain: ${adError.domain}, code: ${adError.code}, " + "message: ${adError.message}"
                    Toast.makeText(
                        this@MainActivity,
                        "onAdFailedToLoad() with error $error",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG + "error", error)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    interstitialAd = ad
                    adIsLoading = false
                    Log.d(TAG, "onAdLoaded()")
                }
            }
        )
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this@MainActivity) {
            // Load an ad.
            loadAd()
        }
    }

    fun fetchData() {
        CoroutineScope(Dispatchers.Main).launch {
            val accessToken = userPreference.getAccessTokenAsString()
            userViewModel.fetchData(accessToken!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
    }
}