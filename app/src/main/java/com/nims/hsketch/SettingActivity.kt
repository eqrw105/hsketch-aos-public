package com.nims.hsketch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MultipartBody

class SettingActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mSetting_Settingitemview_Notice         : SettingItemView
    private lateinit var mSetting_Settingitemview_Version        : SettingItemView
    private lateinit var mSetting_Settingitemview_Shared         : SettingItemView
    private lateinit var mSetting_Settingitemview_Mypicture      : SettingItemView
    private lateinit var mSetting_Settingitemview_Policy         : SettingItemView
    private lateinit var mSetting_Settingitemview_Termsofservice : SettingItemView
    private lateinit var mSetting_Settingitemview_Inquiry        : SettingItemView
    private lateinit var mSetting_Settingitemview_Logout         : SettingItemView
    private lateinit var mSetting_Imageview_Back                 : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        init()
    }

    private fun init(){
        mSetting_Settingitemview_Notice         = findViewById(R.id.setting_settingitemview_notice)
        mSetting_Settingitemview_Version        = findViewById(R.id.setting_settingitemview_version)
        mSetting_Settingitemview_Shared         = findViewById(R.id.setting_settingitemview_shared)
        mSetting_Settingitemview_Mypicture      = findViewById(R.id.setting_settingitemview_mypicture)
        mSetting_Settingitemview_Policy         = findViewById(R.id.setting_settingitemview_policy)
        mSetting_Settingitemview_Termsofservice = findViewById(R.id.setting_settingitemview_termsofservice)
        mSetting_Settingitemview_Inquiry        = findViewById(R.id.setting_settingitemview_inquiry)
        mSetting_Settingitemview_Logout         = findViewById(R.id.setting_settingitemview_logout)
        mSetting_Imageview_Back                 = findViewById(R.id.setting_imageview_back)

        mSetting_Settingitemview_Notice        .setOnClickListener(this)
        mSetting_Settingitemview_Version       .setOnClickListener(this)
        mSetting_Settingitemview_Shared        .setOnClickListener(this)
        mSetting_Settingitemview_Mypicture     .setOnClickListener(this)
        mSetting_Settingitemview_Policy        .setOnClickListener(this)
        mSetting_Settingitemview_Termsofservice.setOnClickListener(this)
        mSetting_Settingitemview_Inquiry       .setOnClickListener(this)
        mSetting_Settingitemview_Logout        .setOnClickListener(this)
        mSetting_Imageview_Back                .setOnClickListener(this)
    }

    override fun finish() {
        super.finish()
        DM.getInstance().finishActivity(this)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.setting_settingitemview_notice ->{
                DM.getInstance().startActivity(this, NoticeActivity())
            }
            R.id.setting_settingitemview_version ->{
                DM.getInstance().showToast(this, BuildConfig.VERSION_NAME)
            }
            R.id.setting_settingitemview_shared ->{
                val message = getString(R.string.app_name) + " 같이할 사람!"
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, message)

                val sharing = Intent.createChooser(intent, getString(R.string.setting_shared))
                startActivity(sharing)
            }
            R.id.setting_settingitemview_mypicture ->{
                DM.getInstance().startActivity(this, MypictureActivity())
            }
            R.id.setting_settingitemview_policy ->{
                val intent = Intent(this, WebActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(DM.mIntentkey_Web, DM.mIntentKey_Policy)
                DM.getInstance().startActivity(this, intent)
            }
            R.id.setting_settingitemview_termsofservice ->{
                val intent = Intent(this, WebActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(DM.mIntentkey_Web, DM.mIntentKey_Termsofservice)
                DM.getInstance().startActivity(this, intent)
            }
            R.id.setting_settingitemview_inquiry ->{
                DM.getInstance().showToast(this, getString(R.string.admin_email))
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "plain/text"
                intent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.admin_email))
                startActivity(intent)
            }
            R.id.setting_settingitemview_logout ->{
                onLogout()
            }
            R.id.setting_imageview_back ->{
                finish()
            }
        }
    }

    private fun onLogout(){
        val firebaseAuth = FirebaseAuth.getInstance()
        //로그인시 마지막 접속일 업데이트
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "user_logout"))
        params.add(MultipartBody.Part.createFormData("user_id", firebaseAuth.currentUser.uid))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, null)

      //  firebaseAuth.signOut()
        finishAffinity()
        DM.getInstance().startActivity(this, IntroActivity())
    }
}