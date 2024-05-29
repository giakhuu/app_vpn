package com.example.app_vpn.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.app_vpn.R
import com.example.app_vpn.data.entities.Country
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.ui.custom.CustomArrayCountryAdapter
import com.example.app_vpn.ui.viewmodel.CountryViewModel
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.visible
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CountryFragment : Fragment() {
    private val countryViewModel by viewModels<CountryViewModel>()

    private lateinit var shimmerStandard: ShimmerFrameLayout
    private lateinit var shimmerPremium: ShimmerFrameLayout
    private lateinit var lvPremium: ListView
    private lateinit var lvStandard: ListView

    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_country, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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
}
