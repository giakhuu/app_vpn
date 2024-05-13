package com.example.app_vpn.ui.fragment

import android.app.Activity
import android.app.Notification
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.databinding.FragmentHomeBinding
import com.example.app_vpn.ui.auth.AuthActivity
import com.example.app_vpn.ui.pay.PaymentVipActivity
import de.blinkt.openvpn.api.IOpenVPNAPIService
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val buttonViewModel: ButtonViewModel by activityViewModels()
    private var mService: IOpenVPNAPIService? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        bindService()
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
            startVpn()
            binding.button.setText(R.string.stop)
        }
        else {
            stopPulse()
            stopVpn()
            binding.button.setText(R.string.start)
        }


        // Xử lí sự kiện click vào vương miện
        binding.crownVip.setOnClickListener {
            val intent = Intent(requireContext(), PaymentVipActivity::class.java)
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


    private fun startVpn() {
        lifecycleScope.launch {
            try {
                requireActivity().assets.open("vpn/america.ovpn").use { inputStream ->
                    Log.d("testvpn", "startVpn: ")
                    val isr = InputStreamReader(inputStream)
                    val br = BufferedReader(isr)
                    var config = ""
                    var line: String?

                    while (true) {
                        line = br.readLine()
                        if (line == null) break
                        config += line + "\n"
                    }

                    val profile = mService!!.addNewVPNProfile("america", false, config)
                    mService!!.startProfile(profile.mUUID)
                    mService!!.startVPN(config)
                    binding.button.text = "Disconnect"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(activity, "VPN service error", Toast.LENGTH_SHORT).show()
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