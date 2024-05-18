package com.example.app_vpn.ui.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.app_vpn.R
import com.example.app_vpn.data.entities.User
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.databinding.FragmentAccountBinding
import com.example.app_vpn.ui.viewmodel.UserViewModel
import com.example.app_vpn.util.JwtUtils
import com.example.app_vpn.util.enable
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.logout
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding

    @Inject
    lateinit var userPreference: UserPreference

    private val userViewModel by viewModels<UserViewModel>()

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val jwtUtils = JwtUtils()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("my_tag", "on view create account fragemnt")
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = binding.swiperefresh

        lifecycleScope.launch {
            fetchData()
        }

        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        userViewModel.user.observe(viewLifecycleOwner) {
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
        }

        binding.btnChangePassword.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext()) // Sử dụng requireContext() thay vì this
            val viewBottomSheetDialog = layoutInflater.inflate(R.layout.dialog_change_password, null)
            dialog.setCancelable(true)
            dialog.setContentView(viewBottomSheetDialog)
            dialog.show()

            val btnChangePw = viewBottomSheetDialog.findViewById<Button>(R.id.btnChangePw)
            bindProgressButton(btnChangePw)
            btnChangePw.apply {
                attachTextChangeAnimator()
                enable(false)
            }

            val txtCurrentPassword = viewBottomSheetDialog.findViewById<TextInputEditText>(R.id.txtCurrentPassword)
            val txtNewPassword = viewBottomSheetDialog.findViewById<TextInputEditText>(R.id.txtNewPassword)

            txtNewPassword.addTextChangedListener {
                btnChangePw.enable(
                    txtCurrentPassword.text.toString().isNotEmpty() &&
                            txtNewPassword.text.toString().isNotEmpty()
                )
            }

            // Xử lí sự kiện thay đổi password
            btnChangePw.setOnClickListener {
                val currentPassword = txtCurrentPassword.text.toString()
                val newPassword = txtNewPassword.text.toString()
                lifecycleScope.launch {
                    changePassword(currentPassword, newPassword)
                }
            }

            userViewModel.changePwResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is Resource.Success -> {
                        btnChangePw.hideProgress(R.string.change_password)
                        val responseValue = it.value
                        when (responseValue.isSuccessful) {
                            true -> {
                                dialog.dismiss()
                                showChangePwSuccessDialog(requireContext())
                            }
                            false -> {
                                viewBottomSheetDialog.findViewById<TextInputLayout>(R.id.iplyCurrentPassword).apply {
                                    helperText = responseValue.message
                                    setHelperTextColor(ColorStateList.valueOf(resources.getColor(R.color.red)))
                                }
                            }
                        }
                    }
                    is Resource.Failure -> {
                        requireActivity().handleApiError(it)
                        dialog.dismiss()
                    }
                    is Resource.Loading -> {
                        btnChangePw.showProgress() {
                            buttonTextRes = R.string.loading
                            progressColor = Color.WHITE
                        }
                    }
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
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
            logout()
            dialog.dismiss()
        }
    }

    private fun showChangePwSuccessDialog(context: Context) {
        val dialog = Dialog(context)
        val viewDialog = layoutInflater.inflate(R.layout.dialog_success, null)
        dialog.setCancelable(false)
        dialog.setContentView(viewDialog)
        dialog.show()

        val btnDone = viewDialog.findViewById<Button>(R.id.btnDone)
        val txtSuccessText = viewDialog.findViewById<TextView>(R.id.txtSuccessText)
        txtSuccessText.text = "Change password successful"
        btnDone.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun refreshData() {
        binding.swiperefresh.isRefreshing = false
    }

    private suspend  fun fetchData() {
        val accessToken = userPreference.getAccessTokenAsString()
        userViewModel.fetchData(accessToken!!)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(user: User) {
        val premiumType = jwtUtils.extractPremiumType(user.premiumKey)
        when (premiumType) {
            "F" -> binding.txtPremiumType.text = "Free"
            else -> binding.txtPremiumType.text =
                "Premium. Expire Date: ${jwtUtils.extractExpirationDate(user.premiumKey)}"
        }
        binding.txtUsername.text = user.username
        binding.txtEmail.text = user.email

    }

    private suspend fun changePassword(oldPassword: String, newPassword: String) {
        val accessToken = userPreference.getAccessTokenAsString()
        userViewModel.changePassword(accessToken!!, oldPassword, newPassword)
    }
}
