package com.example.app_vpn.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.data.UserPreferences
import com.example.app_vpn.databinding.ActivityMainBinding
import com.example.app_vpn.ui.auth.AuthActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.ui.fragment.AccountFragment
import com.example.app_vpn.ui.fragment.CountryFragment
import com.example.app_vpn.ui.fragment.HomeFragment
import com.example.app_vpn.util.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var userPreference: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userPreference = UserPreferences(this)

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
    private fun makeCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }

    fun performLogout() = lifecycleScope.launch {
//        authViewModel.logout()
        userPreference.clear()
        startNewActivity(AuthActivity::class.java)
    }

}