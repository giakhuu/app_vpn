package com.example.app_vpn.ui.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.data.entities.Country
import com.example.app_vpn.data.network.FirebaseHandler
import com.example.app_vpn.data.preferences.PreferenceManager
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.databinding.FragmentHomeBinding
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.menu.ContactActivity
import com.example.app_vpn.ui.menu.PrivatePolicyActivity
import com.example.app_vpn.ui.pay.GetPremiumActivity
import com.example.app_vpn.ui.viewmodel.ButtonViewModel
import com.example.app_vpn.util.JwtUtils
import com.example.app_vpn.util.getMyPublicIpAsync
import com.example.app_vpn.util.toast
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import de.blinkt.openvpn.api.IOpenVPNAPIService
import de.blinkt.openvpn.api.IOpenVPNStatusCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject


const val AD_UNIT_ID = "ca-app-pub-6756127155027324/8332435836"


@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject
    lateinit var firebaseHandler: FirebaseHandler
    // Khai báo preference
    @Inject
    lateinit var userPreference: UserPreference
    @Inject
    lateinit var preferenceManager: PreferenceManager
    private var country : Country? = null

    private lateinit var binding: FragmentHomeBinding
    private var jwtUtils = JwtUtils()
    private val buttonViewModel: ButtonViewModel by activityViewModels()


    // khai báo service cho vpn
    private var mService: IOpenVPNAPIService? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        updateIpAddress()

        // Gán giá trị đầu cho vpn
        bindService()
        val stopPulseFilter = IntentFilter("com.example.app_vpn.STOP_PULSE")
        requireContext().registerReceiver(stopPulseReceiver, stopPulseFilter, RECEIVER_NOT_EXPORTED)
        // khai báo preference
        country = preferenceManager.getCountry()


        // hiện thông tin vpn trong bộ nhớ
        preferenceVPNDetail()
        Log.d("buttonViewModel", "${buttonViewModel.isRunning}  ${preferenceManager.getStatus()}")

        // xử lí bấm nút kết nối
        binding.button.setOnClickListener {
            Log.d("buttonViewModel", "${buttonViewModel.isRunning}  ${preferenceManager.getStatus()}")

            when (preferenceManager.getStatus()) {
                "CONNECTED" -> stopVpn()
                "USERPAUSE" -> resumeVpn()
                else -> {
                    if (country == null) {
                        Toast.makeText(requireContext(), "Hãy chọn VPN", Toast.LENGTH_LONG).show()
                    } else {
                        if (buttonViewModel.isRunning) {
                            stopPulse()
                            stopVpn()
                        }
                        else {
                            startVpn()
                            startPulse()
                            buttonViewModel.isRunning = true
                        }
                    }
                }
            }
        }

        // Khôi phục trạng thái của nút khi Fragment được hiển thị lại
        if (preferenceManager.getStatus() == "CONNECTED") {
            status("connected")
            binding.countryName.text = country!!.name
            startPulse()
        } else {
            stopPulse()
        }


        // Xử lí sự kiện click vào vương miện
        binding.crownVip.setOnClickListener {
            val intent = Intent(requireContext(), GetPremiumActivity::class.java)
            startActivity(intent)
        }

        // Xử lí sự kiện click hiện navigation drawer
        binding.btnNavigation.setOnClickListener {
            // Lấy ra DrawerLayout từ Activity chứa Fragment
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.main_drawer_layout)
            val navigationView = requireActivity().findViewById<NavigationView>(R.id.navigation_view)


            // Mở DrawerLayout để hiển thị NavigationView
            drawerLayout.openDrawer(GravityCompat.START)

            //xử lí sự kiện khi click vào item trong navigation view
            navigationView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.source_code -> {
                        val uri = Uri.parse("https://github.com/giakhuu/app_vpn.git")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                        true
                    }

                    R.id.policy -> {
                        startActivity(Intent(requireActivity(), PrivatePolicyActivity::class.java))
                        true
                    }
                    R.id.rate -> {
                        requireActivity().toast("5* không phải bàn ¯\\_(ツ)_/¯")
                        true
                    }
                    R.id.contact -> {
                        startActivity(Intent(requireActivity(), ContactActivity::class.java))
                        true
                    }
                    else -> {
                        requireActivity().toast("Đang phát triển")
                        true
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPulse() // Dừng Handler khi Fragment bị destroy
    }

    private var handlerAnimation = Handler()
    private var runnable = object : Runnable {
        override fun run() {
            binding.imgAnimation1.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1000)
                .withEndAction {
                    binding.imgAnimation1.scaleX = 1f
                    binding.imgAnimation1.scaleY = 1f
                    binding.imgAnimation1.alpha = 1f
                }

            binding.imgAnimation2.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(700)
                .withEndAction {
                    binding.imgAnimation2.scaleX = 1f
                    binding.imgAnimation2.scaleY = 1f
                    binding.imgAnimation2.alpha = 1f
                }

            handlerAnimation.postDelayed(this, 1500)
        }
    }

    private fun startPulse() {
        handlerAnimation.postDelayed(runnable, 0)
    }

    fun stopPulse() {
        handlerAnimation.removeCallbacks(runnable)
    }





    // xử lí vpn
    private fun preferenceVPNDetail() {
        if(country != null) {
            binding.preferenceVpnCountryName.text = country!!.name
            if(country!!.flag != "None") {
                Picasso.get().load(country!!.flag).into(binding.flagImg)
            }
            if (country!!.premium) {
                binding.preferenceVpnName.text = country!!.vpnName
                binding.preferenceVpnPassword.text = country!!.vpnPassword
            }
        }
    }

    private fun startVpn() {
        lifecycleScope.launch {
            val subDir = File(requireContext().filesDir, "com/example/app_vpn/util")
            if (!subDir.exists()) {
                subDir.mkdirs()  // Tạo thư mục và các thư mục cha nếu chưa tồn tại
            }
            val localFile = File(subDir, "config.ovpn")


            if (country == null) {
                Toast.makeText(requireContext(), "Hãy chọn vpn", Toast.LENGTH_LONG).show()
                return@launch
            }

            val downloadAndConnect = {
                firebaseHandler.downloadFileFromFirebase(
                    fileUrl = country!!.config,
                    localFile = localFile,
                    onSuccess = {
                        try {
                            localFile.inputStream().use { inputStream ->
                                val isr = InputStreamReader(inputStream)
                                val br = BufferedReader(isr)
                                val config = buildString {
                                    var line: String?
                                    while (true) {
                                        line = br.readLine()
                                        if (line == null) break
                                        append(line).append("\n")
                                    }
                                }

                                val profile = mService!!.addNewVPNProfile(country!!.name, false, config)
                                mService!!.startProfile(profile.mUUID)
                                Log.d("stop vpn", "startVpn: ")
                                mService!!.startVPN(config)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(activity, "VPN service error", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { exception ->
                        println("Failed to download file: ${exception.message}")
                    }
                )
            }


            // check country là external hay từ fire base
            if(country!!.id == 0 && country!!.name == "Your country") {
                val profile = mService!!.addNewVPNProfile(country!!.name, false, country!!.config)
                mService!!.startProfile(profile.mUUID)
                Log.d("stop vpn", "startVpn: ")
                mService!!.startVPN(country!!.config)
            }
            else {
                if (firebaseHandler.isUserLoggedIn()) {
                    downloadAndConnect()
                } else {
                    firebaseHandler.signIn("giakhuu18112004@gmail.com", "gia18112004", {
                        downloadAndConnect()
                    }, { exception ->
                        println("Sign-in failed: ${exception.message}")
                    })
                }
            }
        }
    }

    private fun stopVpn() {
        if (mService != null) {
            try {
                AlertDialog.Builder(requireContext())
                    .setTitle("Disconnect")
                    .setMessage(R.string.disconnect_alert)
                    .setPositiveButton("Ok") { _, _ ->
                        mService!!.disconnect()
                        stopPulse()
                        buttonViewModel.isRunning = false
                        binding.countryName.text = getString(R.string.your_wifi)
                        preferenceManager.saveStatus("NOPROCESS")
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                    .setCancelable(false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Handle when mService is null
        }
    }

    fun resumeVpn() {
        try {
            mService!!.resume()
            status("connected")
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun bindService() {
        val icsopenvpnService = Intent(IOpenVPNAPIService::class.java.name)

        icsopenvpnService.setPackage("com.example.app_vpn")
        requireActivity().bindService(
            icsopenvpnService,
            mConnection,
            AppCompatActivity.BIND_AUTO_CREATE
        )
    }



    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = IOpenVPNAPIService.Stub.asInterface(service)
            // Perform other operations related to service connection here


            try {
                // Request permission to use the API
                val i = mService?.prepare(activity!!.packageName)
                if (i != null) {
                    startActivityForResult(i, 7)
                } else {
                    onActivityResult(7, Activity.RESULT_OK, null)
                }
            } catch (e: RemoteException) {
                Log.d("testconectreq", "openvpn service connection failed: " + e.message)
                e.printStackTrace()
            }

        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null // Set mService to null when service is disconnected
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 7) {
            try {
                mService!!.registerStatusCallback(mCallback)
            } catch (e: RemoteException) {
                Log.d("mCallback","openvpn status callback failed: " + e.message)
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unbindService(mConnection)
        try {
            mService?.unregisterStatusCallback(mCallback)
        } catch (e: RemoteException) {
            Log.e("MainFragment", "Failed to unregister VPN status callback: ${e.message}")
            e.printStackTrace()
        }
    }

    // cập nhật state
    fun status(state: String) {
        if(state == "noconnect") {
            binding.button.text = getString(R.string.cn)
            updateIpAddress()
            stopPulse()
        }
        else if (state == "connecting") {
            startPulse()
            binding.countryName.text = country?.name
            binding.button.text = getString(R.string.on_cn)
        }
        else if (state == "retry") {
            binding.button.text = getString(R.string.retry)
            updateIpAddress()
        }
        else if (state == "connected") {
            binding.button.text = getString(R.string.disco)
            showInterstitial()
            updateIpAddress()
        }
        else if(state == "pause") {
            binding.button.text = getString(R.string.res_vpn)
            updateIpAddress()
            stopPulse()
            buttonViewModel.isRunning = false
        }
    }
    fun statusHandler(connectionState: String) {
        requireActivity().runOnUiThread {
            when (connectionState) {
                "NOPROCESS" -> {
                    binding.textView6.text = getString(R.string.not_cn)
                    status("noconnect")
                }
                "USERPAUSE" -> {
                    binding.textView6.text = getString(R.string.pause_vpn)
                    status("pause")
                }
                "CONNECTED" -> {
                    binding.textView6.text = getString(R.string.cn_suc)
                    status("connected")
                }

                "WAIT" -> {
                    binding.textView6.text = getString(R.string.wam)
                    status("connecting")
                }

                "AUTH" -> {
                    binding.textView6.text = getString(R.string.authe)
                    status("connecting")
                }

                "CONNECTRETRY" -> {
                    status("retry")
                    binding.textView6.text = "Retry to connect"
                    try {
                        mService!!.disconnect()
                    } catch (ex: RemoteException) {
                        Log.d("statusHandler", "openvpn server disconnect failed: " + ex.message)
                        ex.printStackTrace()
                    }
                }


                "AUTH_FAILED" -> {
                    binding.textView6.text = getString(R.string.au_failed)
                    status("retry")
                }

                "EXITING" -> {
                    binding.textView6.text = getString(R.string.exvpn)
                    binding.countryName.text = getString(R.string.your_wifi)
                    buttonViewModel.isRunning = false
                    stopPulse()
                    status("retry")
                }
            }
        }
    }


    fun updateIpAddress() {
        CoroutineScope(Dispatchers.Main).launch {
            var ip = getMyPublicIpAsync().await()
            binding.ipaddress.text = ip
            Log.d("ipaddress", ip)
        }
    }
    // Hiện quảng cáo
    fun showInterstitial() {
        userPreference.premiumKey.asLiveData().observe(viewLifecycleOwner) { token ->
            if (activity != null && activity is MainActivity && jwtUtils.extractPremiumType(token!!) == "F") {
                (activity as MainActivity).showInterstitial()
            }
        }
    }

// call back để lấy status
    private val mCallback: IOpenVPNStatusCallback = object : IOpenVPNStatusCallback.Stub() {
        @Throws(RemoteException::class)
        override fun newStatus(uuid: String, state: String, message: String, level: String) {
            Log.d("VPNStatus", state)
            preferenceManager.saveStatus(state)
            statusHandler(state)
        }
    }

    // xử lí bấm cancel khi nhập password
    private val stopPulseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.app_vpn.STOP_PULSE") {
                stopVpn()
                stopPulse()
                updateIpAddress()
                buttonViewModel.isRunning = false
                status("noconnect")
            }
        }
    }
}