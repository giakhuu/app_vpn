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
import android.os.Looper
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
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.data.entities.VpnServer
import com.example.app_vpn.data.preferences.PreferenceManager
import com.example.app_vpn.databinding.FragmentHomeBinding
import com.example.app_vpn.ui.menu.ContactActivity
import com.example.app_vpn.ui.menu.PrivatePolicyActivity
import com.example.app_vpn.ui.pay.GetPremiumActivity
import com.example.app_vpn.ui.viewmodel.HomeViewModel
import com.example.app_vpn.util.getMyPublicIpAsync
import com.example.app_vpn.util.showToast
import com.example.app_vpn.util.toast
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import de.blinkt.openvpn.api.IOpenVPNAPIService
import de.blinkt.openvpn.api.IOpenVPNStatusCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


const val AD_UNIT_ID = "ca-app-pub-6756127155027324/8332435836"
@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject lateinit var preferenceManager: PreferenceManager

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    private var mService: IOpenVPNAPIService? = null
    private val handlerAnimation = Handler(Looper.getMainLooper())

    private var  vpnServer : VpnServer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInsets()
        setupVpn()
        setupUiActions()
        restoreUiState()
        preferenceVPNDetail()

    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupVpn() {
        updateIpAddress()
        bindService()
        if (preferenceManager.getVpnServer() != null) {
            vpnServer = preferenceManager.getVpnServer()!!
            homeViewModel.getConfig(vpnServer!!)
        }
        val stopPulseFilter = IntentFilter("com.example.app_vpn.STOP_PULSE")
        requireContext().registerReceiver(stopPulseReceiver, stopPulseFilter, RECEIVER_NOT_EXPORTED)
    }

    private fun setupUiActions() {
        binding.button.setOnClickListener { handleVpnButtonClick() }
        binding.crownVip.setOnClickListener { navigateToPremium() }
        binding.btnNavigation.setOnClickListener { openNavigationDrawer() }
    }

    private fun restoreUiState() {
        if (preferenceManager.getStatus() == "CONNECTED") {
            status("connected")
            binding.countryName.text = vpnServer?.name ?: "Unknown"
            startPulse()
        } else {
            stopPulse()
        }
    }

    private fun handleVpnButtonClick() {
        val status = preferenceManager.getStatus()
        Log.d("Status", "handleVpnButtonClick: $status")
        when (status) {
            "CONNECTED" -> stopVpn()
            "USERPAUSE" -> resumeVpn()
            else -> {
                if (vpnServer == null) {
                    requireContext().showToast("Hãy chọn VPN")
                } else {
                    if (status == "NOPROCESS" || status == null) {
                        startVpn(homeViewModel.config)
                        startPulse()
                    } else {
                        stopPulse()
                        stopVpn()
                    }
                }
            }
        }
    }

    private fun navigateToPremium() {
        startActivity(Intent(requireContext(), GetPremiumActivity::class.java))
    }

    private fun openNavigationDrawer() {
        requireActivity().findViewById<DrawerLayout>(R.id.main_drawer_layout)
            .openDrawer(GravityCompat.START)

        requireActivity().findViewById<NavigationView>(R.id.navigation_view)
            .setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.source_code -> openUrl("https://github.com/giakhuu/app_vpn.git")
                    R.id.policy -> navigateTo(PrivatePolicyActivity::class.java)
                    R.id.rate -> requireActivity().toast("5* không phải bàn ¯\\_(ツ)_/¯")
                    R.id.contact -> navigateTo(ContactActivity::class.java)
                    else -> requireActivity().toast("Đang phát triển")
                }
                true
            }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun navigateTo(activityClass: Class<*>) {
        startActivity(Intent(requireActivity(), activityClass))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPulse()
    }

    private val runnable = object : Runnable {
        override fun run() {
            animateView(binding.imgAnimation1, 1000)
            animateView(binding.imgAnimation2, 700)
            handlerAnimation.postDelayed(this, 1500)
        }
    }

    private fun animateView(view: View, duration: Long) {
        view.animate()
            .scaleX(4f).scaleY(4f).alpha(0f)
            .setDuration(duration)
            .withEndAction {
                view.scaleX = 1f
                view.scaleY = 1f
                view.alpha = 1f
            }
    }

    private fun startPulse() {
        handlerAnimation.postDelayed(runnable, 0)
    }

    private fun stopPulse() {
        handlerAnimation.removeCallbacks(runnable)
    }



    // xử lí vpn
    private fun preferenceVPNDetail() {
        if(vpnServer != null) {
            binding.preferenceVpnCountryName.text = vpnServer!!.name
            if(vpnServer!!.flag != "None") {
                Picasso.get().load(vpnServer!!.flag).into(binding.flagImg)
            }
            if (vpnServer!!.premium) {
                binding.preferenceVpnName.text = vpnServer!!.vpnName
                binding.preferenceVpnPassword.text = vpnServer!!.vpnPassword
            }
        }
    }

    private fun startVpn(config: String) {
        lifecycleScope.launch {
            if (vpnServer == null) {
                Toast.makeText(requireContext(), "Hãy chọn VPN", Toast.LENGTH_LONG).show()
                return@launch
            }
            if (config == "") {
                Toast.makeText(requireContext(), "Chưa tải config xong", Toast.LENGTH_LONG).show()
                return@launch
            }
            try {
                val profile = mService!!.addNewVPNProfile(vpnServer!!.name, false, config)
                mService!!.startProfile(profile.mUUID)
                Log.d("VPN", "VPN Started")
            } catch (e: Exception) {
                Log.e("VPN", "Lỗi khởi động VPN: ${e.message}")
                Toast.makeText(requireContext(), "VPN service error", Toast.LENGTH_SHORT).show()
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

    private fun resumeVpn() {
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
            e.printStackTrace()
        }
    }

    fun status(state: String) {
        binding.apply {
            when (state) {
                "noconnect" -> {
                    button.text = getString(R.string.cn)
                    updateIpAddress()
                    stopPulse()
                }
                "connecting" -> {
                    startPulse()
                    countryName.text = vpnServer?.name
                    button.text = getString(R.string.on_cn)
                }
                "retry" -> {
                    button.text = getString(R.string.retry)
                    updateIpAddress()
                }
                "connected" -> {
                    button.text = getString(R.string.disco)
                    updateIpAddress()
                }
                "pause" -> {
                    button.text = getString(R.string.res_vpn)
                    updateIpAddress()
                    stopPulse()
                }
            }
        }
    }

    fun statusHandler(connectionState: String) {
        requireActivity().runOnUiThread {
            Log.d("StatusHandler", "Updating UI for state: $connectionState")

            val stateMapping = mapOf(
                "NOPROCESS" to Pair(getString(R.string.not_cn), "noconnect"),
                "USERPAUSE" to Pair(getString(R.string.pause_vpn), "pause"),
                "CONNECTED" to Pair(getString(R.string.cn_suc), "connected"),
                "WAIT" to Pair(getString(R.string.wam), "connecting"),
                "AUTH" to Pair(getString(R.string.authe), "connecting"),
                "TCP_CONNECT" to Pair(getString(de.blinkt.openvpn.R.string.state_tcp_connect), "connecting"),
                "CONNECTRETRY" to Pair("Retry to connect", "retry"),
                "AUTH_FAILED" to Pair(getString(R.string.au_failed), "retry"),
                "EXITING" to Pair(getString(R.string.exvpn), "retry")
            )

            stateMapping[connectionState]?.let { (text, state) ->
                binding.textView6.text = text
                status(state)
            }

            if (connectionState == "CONNECTRETRY") {
                try {
                    mService?.disconnect()
                } catch (ex: RemoteException) {
                    ex.printStackTrace()
                }
            }

            if (connectionState == "EXITING") {
                binding.countryName.text = getString(R.string.your_wifi)
                stopPulse()
            }
        }
    }

    fun updateIpAddress() {
        CoroutineScope(Dispatchers.Main).launch {
            val ip = getMyPublicIpAsync().await()
            binding.ipaddress.text = ip
        }
    }
    // Hiện quảng cáo
//    fun showInterstitial() {
//        userPreference.premiumKey.asLiveData().observe(viewLifecycleOwner) { token ->
//            if (activity != null && activity is MainActivity && "" == "F") {
//                (activity as MainActivity).showInterstitial()
//            }
//        }
//    }

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
                status("noconnect")
            }
        }
    }
}