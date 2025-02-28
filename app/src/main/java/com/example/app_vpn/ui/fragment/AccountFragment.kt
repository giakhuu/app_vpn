package com.example.app_vpn.ui.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.data.entities.PremiumStatus
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.preferences.PreferenceManager
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.databinding.FragmentAccountBinding
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.auth.login.TAG
import com.example.app_vpn.ui.auth.resetpw.ResetPasswordSuccessActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import com.example.app_vpn.ui.viewmodel.UserViewModel
import com.example.app_vpn.util.*
import com.github.razir.progressbutton.bindProgressButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    @Inject lateinit var supabaseClient: SupabaseClient
    @Inject lateinit var preferenceManager: PreferenceManager
    @Inject lateinit var userPreference: UserPreference

    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        applySystemInsets(binding.root)
    }

    /**
     * Thiết lập giao diện và sự kiện của Fragment.
     * Mỗi phần được tách thành các hàm riêng để đảm bảo Single Responsibility.
     */
    private fun setupUi() {
        setupSwipeRefresh()
        updateEmail()
        binding.btnChangePassword.setOnClickListener { showChangePasswordDialog() }
        binding.btnLogout.setOnClickListener { showLogoutDialog() }
        updatePremiumStatus()
    }

    private fun setupSwipeRefresh() {
        binding.swiperefresh.setOnRefreshListener {
            Log.d("_premium", "Swiped down to refresh")
            binding.swiperefresh.isRefreshing = false
            // Bạn có thể thêm logic refresh dữ liệu nếu cần.
        }
    }

    private fun updateEmail() {
        binding.txtEmail.text = supabaseClient.auth.currentUserOrNull()?.email
    }

    /**
     * Áp dụng padding dựa trên insets của hệ thống (status bar, navigation bar)
     */
    private fun applySystemInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }
    }

    /**
     * Hiển thị dialog đổi mật khẩu và thiết lập sự kiện của các thành phần bên trong.
     */
    private fun showChangePasswordDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.show()

        handleChangePasswordBtnClick(dialogView)
        observeUpdatePasswordResponse(dialogView)
    }

    /**
     * Xử lý sự kiện click nút đổi mật khẩu bên trong dialog.
     */
    private fun handleChangePasswordBtnClick(view: View) {
        val btnChangePw = view.findViewById<Button>(R.id.btnChangePw)
        bindProgressButton(btnChangePw)
        btnChangePw.setUp()

        val txtNewPassword = view.findViewById<TextInputEditText>(R.id.txtNewPassword)
        val txtNewPasswordConfirm = view.findViewById<TextInputEditText>(R.id.txtNewPasswordConfirm)
        val iplyNewPassword = view.findViewById<TextInputLayout>(R.id.iplyNewPassword)
        val iplyNewPasswordConfirm = view.findViewById<TextInputLayout>(R.id.iplyNewPasswordConfirm)

        iplyNewPassword.isValid(txtNewPassword, btnChangePw, getString(R.string.passwordValidateError)) {
            this.isValidPassword()
        }
        iplyNewPasswordConfirm.isValid(
            txtNewPasswordConfirm, btnChangePw, getString(R.string.passwordConfirmValidateError)
        ) { txtNewPasswordConfirm.text.toString() == txtNewPassword.text.toString() }

        btnChangePw.setOnClickListener {
            lifecycleScope.launch {
                btnChangePw.onLoad()
                authViewModel.updatePassword(
                    supabaseClient.auth.currentUserOrNull()?.email,
                    txtNewPassword.text.toString(),
                    supabaseClient.auth.currentAccessTokenOrNull() ?: ""
                )
            }
        }
    }

    /**
     * Quan sát phản hồi từ cập nhật mật khẩu.
     */
    private fun observeUpdatePasswordResponse(view: View) {
        val btnChangePw = view.findViewById<Button>(R.id.btnChangePw)
        lifecycleScope.launch {
            authViewModel.updatePasswordResponse.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Log.d(TAG, "resetpassword: thành công")
                        requireContext().showToast(getString(R.string.reset_password_success))
                        startActivity(Intent(requireContext(), ResetPasswordSuccessActivity::class.java))
                    }
                    is Resource.Error -> {
                        btnChangePw.onDone(getString(R.string.reset_password))
                        requireContext().showToast(resource.error.message.toString())
                    }
                    is Resource.Loading -> {
                        // Loading đã được xử lý qua onLoad() trong btnChangePw
                    }
                }
            }
        }
    }

    /**
     * Hiển thị dialog logout.
     */
    private fun showLogoutDialog() {
        val dialog = Dialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)
        dialog.show()

        dialogView.findViewById<Button>(R.id.btnLogoutNo).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnLogoutYes).setOnClickListener {
            logout()
            dialog.dismiss()
        }
    }

    /**
     * Cập nhật giao diện hiển thị trạng thái Premium dựa trên dữ liệu từ UserPreference.
     */
    private fun updatePremiumStatus() {
        lifecycleScope.launch {
            if (userPreference.getPremiumStatus()) {
                binding.txtPremiumType.text = getString(R.string.premium)
                binding.txtExpireDate.text = convertDateTime(userPreference.getPremiumExpiredDate())
            } else {
                binding.txtPremiumType.text = getString(R.string.non_premium)
                binding.txtExpireDate.text = getString(R.string.unlimited)
            }
        }
    }
}