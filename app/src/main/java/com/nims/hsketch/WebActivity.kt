package com.nims.hsketch

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class WebActivity : AppCompatActivity() {
    private lateinit var mWeb_Webview        : WebView
    private lateinit var mWeb_Imageview_Back : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        init()
    }

    private fun init(){
        //bind
        mWeb_Webview        = findViewById(R.id.web_webview)
        mWeb_Imageview_Back = findViewById(R.id.web_imageview_back)

        when(intent.getStringExtra(DM.mIntentkey_Web)){
            DM.mIntentKey_Policy         -> {
                val url = BuildConfig.BASE_URL + BuildConfig.POLICY_END_POINT
                DM.getInstance().getWebView(mWeb_Webview, url)
            }
            DM.mIntentKey_Termsofservice -> {
                val url = BuildConfig.BASE_URL + BuildConfig.TERMSOFSERVICE_END_POINT
                DM.getInstance().getWebView(mWeb_Webview, url)
            }
        }

        onActivityFinish()
    }

    override fun onBackPressed() {
        //웹사이트에서 뒤로 갈 페이지 존재시
        if(mWeb_Webview.canGoBack()) {
            mWeb_Webview.goBack() // 웹사이트 뒤로가기
            return
        }
        super.onBackPressed()
    }

    private fun onActivityFinish(){
        mWeb_Imageview_Back.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        DM.getInstance().finishActivity(this)
    }
}