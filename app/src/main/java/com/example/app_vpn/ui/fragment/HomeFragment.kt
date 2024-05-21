package com.example.app_vpn.ui.fragment

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.data.preferences.PreferenceManager
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.databinding.FragmentHomeBinding
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.pay.GetPremiumActivity
import com.example.app_vpn.ui.viewmodel.ButtonViewModel
import com.example.app_vpn.util.JwtUtils
import com.example.app_vpn.util.MyVpnStateReceiver
import com.example.app_vpn.util.VpnStateListener
import com.example.app_vpn.util.getMyPublicIpAsync
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import de.blinkt.openvpn.api.IOpenVPNAPIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject

const val AD_UNIT_ID = "ca-app-pub-6756127155027324/8332435836"
//const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"


@AndroidEntryPoint
class HomeFragment : Fragment(), VpnStateListener {
    @Inject
    lateinit var userPreference: UserPreference

    private lateinit var binding: FragmentHomeBinding
    private lateinit var preferenceManager: PreferenceManager

    private var jwtUtils = JwtUtils()
    private val buttonViewModel: ButtonViewModel by activityViewModels()
    private var mService: IOpenVPNAPIService? = null

    // Create an instance of MyVpnStateReceiver
    private val vpnStateReceiver = MyVpnStateReceiver(this)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Gán giá trị đầu cho vpn
        bindService()
        val filter = IntentFilter("de.blinkt.openvpn.VPN_STATUS")
        requireContext().registerReceiver(vpnStateReceiver, filter)

        // Gán giá trị cho 2 cái text ơ trang home
        CoroutineScope(Dispatchers.Main).launch {
            val ipAddress = getMyPublicIpAsync().await()
            binding.ipaddress.text = ipAddress
        }

        binding.testad.setOnClickListener {
            showInterstitial()
        }

        binding.button.setOnClickListener {
            if (buttonViewModel.isRunning) {
                stopPulse()
                stopVpn()
                binding.button.setText(R.string.start)
            } else {
                startPulse()
                startVpn()
                binding.button.setText(R.string.stop)
            }
            buttonViewModel.isRunning = !buttonViewModel.isRunning
        }

        // Khôi phục trạng thái của nút khi Fragment được hiển thị lại
        if (buttonViewModel.isRunning) {
            startPulse()
            binding.button.setText(R.string.stop)
        }
        else {
            stopPulse()
            binding.button.setText(R.string.start)
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

            // Mở DrawerLayout để hiển thị NavigationView
            drawerLayout.openDrawer(GravityCompat.START)

            // Xử lí sự kiện nhấn vào Rate Us
        }

        // khai báo preference
        preferenceManager = PreferenceManager(requireContext())

        return view
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

    private fun stopPulse() {
        handlerAnimation.removeCallbacks(runnable)
    }

    // firebase file
    fun downloadFileFromFirebase(
        fileUrl: String,
        localFile: File,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storage = Firebase.storage
        val fileRef = storage.getReferenceFromUrl(fileUrl)

        fileRef.getFile(localFile).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    private fun signIn(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    task.exception?.let { onFailure(it) }
                }
            }
    }

    fun isUserLoggedIn(): Boolean {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser != null
    }

    // cập nhật state
    override fun onVpnStateChanged(state: String) {
        // Update the TextView with the new state
        binding.state.text = state
    }

    override fun updateIpAddress() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.ipaddress.text = getMyPublicIpAsync().await()
        }
    }

    override fun showInterstitial() {
        userPreference.premiumKey.asLiveData().observe(viewLifecycleOwner) {token ->
            if (activity != null && activity is MainActivity && jwtUtils.extractPremiumType(token!!) == "F") {
                (activity as MainActivity).showInterstitial()
            }
        }
    }

    // xử lí vpn
    private fun startVpn() {
        lifecycleScope.launch {
            val subDir = File(requireContext().filesDir, "com/example/app_vpn/util")
            if (!subDir.exists()) {
                subDir.mkdirs()  // Tạo thư mục và các thư mục cha nếu chưa tồn tại
            }
            val localFile = File(subDir, "config.ovpn")

            val country = preferenceManager.getCountry()
            if (country == null) {
                Toast.makeText(requireContext(), "Hãy chọn vpn", Toast.LENGTH_LONG).show()
                return@launch
            }

            // Thiết lập chữ
            binding.countryName.text = country.name

            val downloadAndConnect = {
                downloadFileFromFirebase(
                    fileUrl = country.config,
                    localFile = localFile,
                    onSuccess = {
                        println("File downloaded successfully")
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

                                val profile = mService!!.addNewVPNProfile("america", false, config)
                                mService!!.startProfile(profile.mUUID)
                                mService!!.startVPN(config)
                                binding.button.text = "Stop"
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

            if (isUserLoggedIn()) {
                downloadAndConnect()
            } else {
                signIn("giakhuu18112004@gmail.com", "gia18112004", {
                    downloadAndConnect()
                }, { exception ->
                    println("Sign-in failed: ${exception.message}")
                })
            }
        }
    }

    private fun stopVpn() {
        if (mService != null) {
            try {
                mService!!.disconnect()
                binding.button.text = "Connect"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Handle when mService is null
        }
    }

    private fun bindService() {
        val icsopenvpnService = Intent(IOpenVPNAPIService::class.java.name)

        icsopenvpnService.setPackage("com.example.app_vpn")
        requireActivity().bindService(icsopenvpnService, mConnection, AppCompatActivity.BIND_AUTO_CREATE)
    }

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = IOpenVPNAPIService.Stub.asInterface(service)
            // Perform other operations related to service connection here


            try {
                // Request permission to use the API
                val i = (mService as? IOpenVPNAPIService)?.prepare(activity!!.packageName)
                if (i != null) {
                    startActivityForResult(i, 7)
                } else {
                    onActivityResult(7, Activity.RESULT_OK, null)
                }
            } catch (e: RemoteException) {
                Log.d("testconectreq","openvpn service connection failed: " + e.message)
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null // Set mService to null when service is disconnected
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unbindService(mConnection)
    }
}