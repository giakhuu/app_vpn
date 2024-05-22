package com.example.app_vpn.ui.pay

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.databinding.ActivityPaymentVipBinding
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.ui.viewmodel.PaymentViewModel
import com.example.app_vpn.util.getMyPublicIpAsync
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.startNewActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOException

@AndroidEntryPoint
class PaymentVipActivity : AppCompatActivity() {

    private val paymentViewModel by viewModels<PaymentViewModel>()
    private lateinit var bundle: Bundle

    private lateinit var userPreference: UserPreference

    private lateinit var binding: ActivityPaymentVipBinding

    private var ipAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaymentVipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference(this)

        bundle = intent.extras!!

        lifecycleScope.launch {
            createPayment(bundle.getString("amount")!!)
        }

        binding.imgLogo.setOnClickListener {
            lifecycleScope.launch {
                logout()
            }
        }

        // Tiếp tục với việc ánh xạ các view và thiết lập các sự kiện ở đây

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Lưu hình ảnh
        binding.btnSaveImg.setOnClickListener {
            val bitmap: Bitmap = (binding.imgQrCode.drawable as BitmapDrawable).bitmap
            saveImage(bitmap, "qrpay.jpg")
        }

        paymentViewModel.createPaymentResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    val paymentUrl = response.value.data.paymentUrl
                    generateQRCode(paymentUrl)
                }

                is Resource.Failure -> {
                    handleApiError(response)
                }

                else -> {
                    Log.d("mytag", "on Loading payment")
                }
            }
        }
    }

    private suspend fun logout() {
        userPreference.clear()
        startNewActivity(LoginActivity::class.java)
    }

    private fun saveImage(bitmap: Bitmap, imageName: String) {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //this one
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        try {
            resolver.openOutputStream(uri ?: return)?.use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            Toast.makeText(this@PaymentVipActivity, "Save image success", Toast.LENGTH_LONG).show()
        } catch (e: FileNotFoundException) {
            Log.d("TAG", "saveImage: ${e.message}")
        } catch (e: IOException) {
            Log.d("TAG", "saveImage: ${e.message}")
        }
    }

    private suspend fun createPayment(amount: String) {
        val accessToken = userPreference.getAccessTokenAsString()
        ipAddress = getMyPublicIpAsync().await()
        paymentViewModel.createPayment(accessToken!!, ipAddress!!, amount)
    }

    private fun generateQRCode(text: String) {
        val multiFormatWriter = MultiFormatWriter()

        try {
            val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 400, 400)
            val barcodeEncoder = BarcodeEncoder()
            binding.imgQrCode.setImageBitmap(barcodeEncoder.createBitmap(bitMatrix))
        } catch (e: WriterException) {
            Log.d("mytag", e.message!!)
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("my_tag", "payment on destroy")
    }

    override fun onPause() {
        super.onPause()
        Log.d("my_tag", "payment on pause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("my_tag", "payment on stop")
    }
}