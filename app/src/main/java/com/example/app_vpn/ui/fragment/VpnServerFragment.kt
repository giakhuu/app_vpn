package com.example.app_vpn.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.app_vpn.data.entities.VpnServer
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.preferences.PreferenceManager
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.databinding.FragmentCountryBinding
import com.example.app_vpn.ui.custom.CustomArrayVpnServerAdapter
import com.example.app_vpn.ui.viewmodel.VpnServerViewModel
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class VpnServerFragment(
) : Fragment() {
    @Inject
    lateinit var preferenceManager: PreferenceManager
    @Inject
    lateinit var userPreference: UserPreference

    private val vpnServerViewModel by viewModels<VpnServerViewModel>()
    private lateinit var binding: FragmentCountryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCountryBinding.inflate(inflater, container, false)
        setupInsets(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingStart, systemBars.top, v.paddingEnd, v.paddingBottom)
            insets
        }
    }

    private fun setupUI() {
        binding.importFileBtn.apply {
            enable(false)
            setOnClickListener { showFilePicker() }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vpnServerViewModel.allCountry.collect {handleResponse(it)} }
            }
        }
    }

    private suspend fun handleResponse(resource: Resource<List<VpnServer>>) {
        when (resource) {
            is Resource.Success -> {
                handleSuccess(resource.data!!)
                endLoading()
            }
            is Resource.Error -> {
                endLoading()
            }
            is Resource.Loading -> showLoading()
        }
    }

    private suspend fun handleSuccess(allCountry: List<VpnServer>) {
        binding.importFileBtn.enable(true)
        updateCountryUI(allCountry)
    }


    private fun showLoading() {
        binding.shimmerPremiumCountry.startShimmer()
    }

    private fun endLoading() {
        binding.shimmerPremiumCountry.apply {
            stopShimmer()
            visible(false)
        }
    }

    private suspend fun updateCountryUI(allCountry: List<VpnServer>) {
        binding.lvVpnServer.apply {
            adapter = CustomArrayVpnServerAdapter(requireContext(), allCountry, userPreference.getPremiumStatus())
            visible(true)
        }
    }

    private fun showFilePicker() {
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }.also { startActivityForResult(it, FILE_PICKER_REQUEST_CODE) }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { readFileContent(it) }
        }
    }

    private fun readFileContent(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val content = inputStream.bufferedReader().use { it.readText() }
                Log.d("ovpnconfig", content)
                saveVpnConfig(content)
            }
        } catch (e: Exception) {
            Log.e("FileReadError", "Failed to read file: ${e.message}")
        }
    }

    private fun saveVpnConfig(content: String) {
        preferenceManager.saveVpnServer(
            VpnServer(
                id = 0,
                name = "Your country",
                flag = "None",
                config = content,
                premium = false,
                vpnName = "Your VPN",
                vpnPassword = "None"
            )
        )
    }

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 100
    }
}
