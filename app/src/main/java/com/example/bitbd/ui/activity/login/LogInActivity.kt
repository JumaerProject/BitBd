package com.example.bitbd.ui.activity.login


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.emrekotun.toast.CpmToast.Companion.toastError
import com.emrekotun.toast.CpmToast.Companion.toastInfo
import com.emrekotun.toast.CpmToast.Companion.toastSuccess
import com.emrekotun.toast.CpmToast.Companion.toastWarning
import com.example.bitbd.R
import com.example.bitbd.animation.LoadingProgress
import com.example.bitbd.constant.SUCCESS
import com.example.bitbd.constant.WARNING
import com.example.bitbd.util.RunTimeValue
import com.example.bitbd.databinding.ActivityLogInBinding
import com.example.bitbd.sharedPref.BitBDPreferences
import com.example.bitbd.ui.activity.BaseActivity
import com.example.bitbd.ui.activity.main.MainActivity
import com.example.bitbd.ui.activity.login.model.UserLogIn
import com.example.bitbd.ui.activity.otp_verify.OtpVerifyActivity
import com.example.bitbd.util.BitBDUtil
import com.example.bitbd.util.UserToastCommunicator
import kotlinx.coroutines.launch

class LogInActivity : BaseActivity()  {
    private lateinit var loginViewModel: LogInViewModel
    private lateinit var binding: ActivityLogInBinding
    private lateinit var preference: BitBDPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginViewModel = ViewModelProvider(this)[LogInViewModel::class.java]
        preference = BitBDPreferences(this@LogInActivity)
        dataObserver()

        binding.loginButton.setOnClickListener {
            logInSubmit()
        }

        if(preference.getAuthToken()?.isEmpty() == false && preference.getPhoneVerifyStatus() == 1){
            moveToNextMainPage()
        }


    }



    private fun logInSubmit() {
        try {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: java.lang.Exception) {

        }
        val username = binding.usernameLayout.editText?.text.toString()
        val password = binding.passwordLayout.editText?.text.toString()

        if (username.isEmpty()) {
            binding.usernameLayout.error = getString(R.string.this_field_is_required)
            return
        }
        binding.usernameLayout.error = null

        if (password.isEmpty()) {
            binding.passwordLayout.error = getString(R.string.this_field_is_required)
            return
        }

        binding.passwordLayout.error = null
        lifecycleScope.launch {
            loginViewModel.login(UserLogIn(username, password),this@LogInActivity)
        }

    }


    private fun dataObserver(){

        var loading : LoadingProgress? = null

        loginViewModel.progress.observe(this){
            if(it != null){
                if(it) {
                   loading = BitBDUtil.showProgress(this@LogInActivity)
                }
                else{
                    loading?.dismiss()

                }
            }
        }

        loginViewModel.userLogin.observe(this){
            if(it != null){
                it.user?.mobileStatus?.let { it1 -> preference.putPhoneVerifyStatus(it1) }
                if(it.user?.mobileStatus !=1){
                    preference.putAuthToken(it.authorisation?.token)
                    startActivity(Intent(this@LogInActivity, OtpVerifyActivity::class.java))
                    finish()
                    return@observe
                }
                RunTimeValue.logInResponse = it
                preference.putAuthToken(it.authorisation?.token)
                it.user?.name?.let { it1 -> preference.putName(it1.toString()) }
                it.user?.mobile?.let { it1 -> preference.putMobileNumber(it1.toString()) }
                it.user?.email?.let { it1 -> preference.putEmail(it1.toString()) }
                it.user?.image?.let { it1 -> preference.putImageUrl(it1.toString()) }
                it.user?.affiliateStatus?.let { it1 -> preference.putAffiliate(it1.toInt()) }
                it.user?.affiliateCode?.let { it1 -> preference.putAffiliateCode(it1.toString()) }
                it.user?.username?.let { it1 -> preference.putUserName(it1.toString()) }
                it.user?.slug?.let { it1 -> preference.putSlug(it1.toString()) }
                it.user?.balance?.let { it1-> preference.putAvailableBalance(it1.toString()) }
                loading?.dismiss()
                moveToNextMainPage()
            }
        }
    }

    private fun moveToNextMainPage() {
        startActivity(Intent(this@LogInActivity, MainActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        finishAffinity()
    }

}