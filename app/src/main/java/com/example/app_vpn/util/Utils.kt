package com.example.app_vpn.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

fun <A : Activity> Activity.startNewActivity(activity: Class<A>,  bundle: Bundle? = null) {
    Intent(this, activity).also {
        bundle?.let { intent.putExtras(it) }
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}

fun View.visible(isVisivble: Boolean) {
    visibility = if (isVisivble) View.VISIBLE else View.GONE
}

fun View.enable(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.5f
}

fun Button.onLoad() {
    this.apply {
        showProgress {
            buttonTextRes = R.string.loading
            progressColor = Color.WHITE
        }
        enable(false)
    }
}

fun Button.setUp() {
    this.apply {
        attachTextChangeAnimator()
        enable(false)
    }
}

fun TextInputLayout.isValid(
    editText: EditText,
    submitButton: View? = null,
    invalidHelperText: String,
    validate: String.() -> Boolean
) {
    editText.addTextChangedListener {
        val inputText = it?.toString().orEmpty()
        if (inputText.validate()) {
            this.isHelperTextEnabled = false
            submitButton?.enable(true)
        } else {
            this.isHelperTextEnabled = true
            this.helperText = invalidHelperText
            setHelperTextColor(ColorStateList.valueOf(resources.getColor(R.color.red)))
            submitButton?.enable(false)
        }
    }
}

fun Button.onDone(
    content : String
) {
    this.apply {
        hideProgress(content)
        enable(true)
    }
}

fun View.snackBar(message: String, action: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)

    action?.let {
        snackbar.setAction("Retry") {
            it()
        }
    }
    snackbar.show()
}

fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.handleApiError(
    failure: Resource.Failure,
    action: (() -> Unit)? = null
) {
    val rootView = window.decorView.findViewById<View>(android.R.id.content)
    when {
        failure.isNetworkError -> {
            Log.d("my_tag", failure.toString())
            rootView?.snackBar(
                failure.errorBody.toString(),
                action
            )
        }
        failure.errorCode == 404 -> {
            if (this is LoginActivity) {
                rootView?.snackBar("Please enter correct login information")
            }
            else {
                rootView?.snackBar("404 Error !")
            }
        }
        failure.errorCode == 409 -> {
            rootView?.snackBar("Already exist username or email")
        }
        else -> {
            val error = failure.errorBody
            rootView?.snackBar(error!!)
        }
    }
}

fun Fragment.logout() = lifecycleScope.launch {
    Log.d("mytag_logout_utils", activity.toString())
    if (activity is MainActivity) {
        (activity as MainActivity).performLogout()
    }
}

fun hideKeyboard(context: Context, view: View) {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}


