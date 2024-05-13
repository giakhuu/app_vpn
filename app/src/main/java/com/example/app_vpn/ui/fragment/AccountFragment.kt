package com.example.app_vpn.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.app_vpn.R
import com.example.app_vpn.data.UserPreferences
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.repsonses.User
import com.example.app_vpn.databinding.FragmentAccountBinding
import com.example.app_vpn.ui.viewmodel.UserViewModel
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.logout
import com.example.app_vpn.util.snackBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding

    @Inject
    lateinit var userPreferences: UserPreferences

    private val userViewModel by viewModels<UserViewModel>()

    private lateinit var auth : FirebaseAuth

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = binding.swiperefresh
        auth = Firebase.auth

        lifecycleScope.launch {
            fetchData()
        }

        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        userViewModel.user.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    updateUI(it.value.data)
                }
                is Resource.Failure -> {
                    requireActivity().handleApiError(it)
                }
                is Resource.Loading -> {
                    //shimmer effect
                }
            }
        })

        binding.btnChangePassword.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext()) // Sử dụng requireContext() thay vì this

            val viewBottomSheetDialog = layoutInflater.inflate(R.layout.dialog_change_password, null)

            dialog.setCancelable(true)
            dialog.setContentView(viewBottomSheetDialog)
            dialog.show()

            val btnChangePassword = viewBottomSheetDialog.findViewById<Button>(R.id.btnChangePasswordDialog)

            // Xử lí sự kiện thay đổi password
            btnChangePassword.setOnClickListener {
                val oldPassword = viewBottomSheetDialog.findViewById<TextInputEditText>(R.id.txtOldPassword).text.toString()
                val newPassword = viewBottomSheetDialog.findViewById<TextInputEditText>(R.id.txtNewPassword).text.toString()
                Log.d("mytag_pw", oldPassword + newPassword)
                lifecycleScope.launch {
                    changePassword(oldPassword, newPassword)
                }
                dialog.dismiss()
                requireView().snackBar("OK")
            }
        }

        binding.btnLogout.setOnClickListener {
            val dialog = Dialog(requireContext())

            val viewLogoutDialog = layoutInflater.inflate(R.layout.dialog_logout, null)

            dialog.setCancelable(false)
            dialog.setContentView(viewLogoutDialog)
            dialog.show()

            val btnLogoutNo = viewLogoutDialog.findViewById<Button>(R.id.btnLogoutNo)
            val btnLogoutYes = viewLogoutDialog.findViewById<Button>(R.id.btnLogoutYes)

            btnLogoutNo.setOnClickListener {
                dialog.dismiss()
            }

            btnLogoutYes.setOnClickListener {
                auth.signOut()
                logout()
                dialog.dismiss()
            }
        }
    }

    private fun refreshData() {
        binding.swiperefresh.isRefreshing = false
    }

    private suspend  fun fetchData() {
        val accessToken = userPreferences.getAccessTokenAsString()
        userViewModel.fetchData(accessToken!!)
    }

    private fun updateUI(user: User) {
        binding.txtUsername.text = user.username
        binding.txtEmail.text = user.email
    }

    private suspend fun changePassword(oldPassword: String, newPassword: String) {
        val accessToken = userPreferences.getAccessTokenAsString()
        userViewModel.changePassword(accessToken!!, oldPassword, newPassword)
    }
}
