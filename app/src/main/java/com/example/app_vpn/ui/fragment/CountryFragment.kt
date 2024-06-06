package com.example.app_vpn.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.app_vpn.R
import com.example.app_vpn.data.entities.Country
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.preferences.PreferenceManager
import com.example.app_vpn.databinding.FragmentCountryBinding
import com.example.app_vpn.ui.custom.CustomArrayCountryAdapter
import com.example.app_vpn.ui.viewmodel.CountryViewModel
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.visible
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint
import java.io.BufferedReader
import java.io.InputStreamReader

@AndroidEntryPoint
class CountryFragment : Fragment() {
    private val countryViewModel by viewModels<CountryViewModel>()

    private lateinit var shimmerStandard: ShimmerFrameLayout
    private lateinit var shimmerPremium: ShimmerFrameLayout
    private lateinit var lvPremium: ListView
    private lateinit var lvStandard: ListView

    private var isDataLoaded = false

    private lateinit var binding: FragmentCountryBinding

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCountryBinding.inflate(inflater, container, false)
        val view = binding.root
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val currentPaddingLeft = ViewCompat.getPaddingStart(v)
            val currentPaddingRight = ViewCompat.getPaddingEnd(v)
            val currentPaddingBottom = v.paddingBottom
            v.setPadding(
                currentPaddingLeft,
                systemBars.top,
                currentPaddingRight,
                currentPaddingBottom
            )
            insets
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shimmerStandard = view.findViewById(R.id.shimmerStandardCountry)
        shimmerPremium = view.findViewById(R.id.shimmerPremiumCountry)
        lvPremium = view.findViewById(R.id.lvPremiumCountry)
        lvStandard = view.findViewById(R.id.lvStandardCountry)
        binding.importFileBtn.enable(false)

        if (!isDataLoaded) {
            countryViewModel.getAllCountry()
        }

        countryViewModel.allCountry.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    shimmerStandard.apply {
                        stopShimmer()
                        visible(false)
                    }
                    shimmerPremium.apply {
                        stopShimmer()
                        visible(false)
                    }
                    binding.importFileBtn.enable(true)
                    val allCountry = response.value.data
                    updateCountryUI(allCountry)
                    isDataLoaded = true
                }

                is Resource.Failure -> {
                    shimmerStandard.apply {
                        stopShimmer()
                        visible(false)
                    }
                    shimmerPremium.apply {
                        stopShimmer()
                        visible(false)
                    }
                    requireActivity().handleApiError(response)
                }

                is Resource.Loading -> {
                    if (!isDataLoaded) {
                        shimmerStandard.startShimmer()
                        shimmerPremium.startShimmer()
                    }
                }
            }
        }


        // Import file
        // set preferenceManager
        preferenceManager = PreferenceManager(requireContext())
        binding.importFileBtn.setOnClickListener { showFilePicker() }
    }

    private fun updateCountryUI(allCountry: List<Country>) {
        val listPremiumCountry = mutableListOf<Country>()
        val listStandardCountry = mutableListOf<Country>()
        for (i in allCountry.indices) {
            if (allCountry[i].premium) {
                listPremiumCountry.add(allCountry[i])
            } else {
                listStandardCountry.add(allCountry[i])
            }
        }
        lvPremium.adapter = CustomArrayCountryAdapter(requireContext(), listPremiumCountry, this)
        lvStandard.adapter = CustomArrayCountryAdapter(requireContext(), listStandardCountry, this)
        lvPremium.visible(true)
        lvStandard.visible(true)
    }

    private fun showFilePicker(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try{
            startActivityForResult(intent, 100)
        }
        catch (ex: Exception) {
            Toast.makeText(requireContext(), "Please insstall a file mangaer", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in java", ReplaceWith(
        "super.onActivityResult(requestCode, resultCode, data)",
        "androidx.fragment.app.Fragment"
    )
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            if (uri != null) {
                try {
                    // Sử dụng ContentResolver để mở InputStream
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val inputStreamReader = InputStreamReader(inputStream)
                        val bufferedReader = BufferedReader(inputStreamReader)
                        val stringBuilder = StringBuilder()
                        var line: String?
                        while (bufferedReader.readLine().also { line = it } != null) {
                            stringBuilder.append(line).append('\n')
                        }
                        inputStream.close()
                        val fileContent = stringBuilder.toString()
                        Log.d("ovpnconfig", fileContent)
                        preferenceManager.saveCountry(
                            Country(
                                id = 0, // Thay đổi giá trị id theo nhu cầu
                                name = "Your country",
                                flag = "None",
                                config = fileContent,
                                premium = false, // Hoặc false
                                vpnName = "Your VPN",
                                vpnPassword = "None"
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("FileReadError", "Failed to read file: ${e.message}")
                }
            }
        }
    }



}
