package com.example.app_vpn.ui.fragment

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.ui.custom.CustomArrayCountryAdapter
import com.example.app_vpn.ui.viewmodel.CountryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CountryFragment : Fragment() {
    private lateinit var customArrayCountryAdapter: CustomArrayCountryAdapter
    private val countryViewModel by viewModels<CountryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Truy cập AssetManager từ context
        val assetManager: AssetManager = requireContext().assets

        // Tạo một danh sách rỗng để chứa các đối tượng quốc gia
//        val list = mutableListOf<Country>()

        // Lấy danh sách tên tất cả các tệp trong thư mục assets/Flags
//        val flagFiles = assetManager.list("Flags") ?: emptyArray()

        // Duyệt qua danh sách các tệp và thêm chúng vào danh sách quốc gia
//        for (fileName in flagFiles) {
//            // Lấy hình ảnh từ thư mục assets/Flags
//            val flagInputStream = assetManager.open("Flags/$fileName")
//            val flagDrawable = Drawable.createFromStream(flagInputStream, null)
//
//            // Lấy tên quốc gia từ tên tệp (giả sử tên tệp có dạng "country_flag.jpg")
//            val countryName = fileName.substringBefore("_flag")
//                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
//
//            // Thêm quốc gia vào danh sách
//            list.add(Country(flagDrawable, countryName, "50ms")) // Thêm thời gian ping mặc định
//
//            // Đóng luồng đầu vào
//            flagInputStream.close()
//        }
        val view = inflater.inflate(R.layout.fragment_country, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        countryViewModel.getAllCountry()

        countryViewModel.allCountry.observe(viewLifecycleOwner, Observer { response ->
            when(response) {
                is Resource.Success -> {
                    val allCountry = response.value
                    Log.d("allCountry", allCountry.data.toString())
                    customArrayCountryAdapter = CustomArrayCountryAdapter(requireContext(), allCountry.data)

                    // Inflate layout cho fragment này

                    // Gắn adapter với ListView trong layout
                    val listView = view.findViewById<ListView>(R.id.list)
                    listView.adapter = customArrayCountryAdapter
                }
                is Resource.Failure -> {
                    Log.d("errorBool", response.isNetworkError.toString())

                    Log.d("errorCode", response.errorCode.toString())

                    Log.d("errorBody", response.errorBody.toString())
                    Log.d("allCountry", "failed")
                }
                is Resource.Loading -> {

                }
            }
        })
        return view
    }
}