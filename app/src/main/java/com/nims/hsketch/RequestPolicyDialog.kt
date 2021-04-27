package com.nims.hsketch

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.webkit.WebView
import android.widget.Button
import android.widget.CheckBox

class RequestPolicyDialog(context: Context): Dialog(context) {
    private lateinit var mRequest_Privacy_Button_OK               : Button
    private lateinit var mRequest_Privacy_Button_Cancel           : Button
    private lateinit var mRequest_Privacy_Checkbox_Privacy        : CheckBox
    private lateinit var mRequest_Privacy_Checkbox_Termsofservice : CheckBox
    private lateinit var mRequest_Privacy_Button_Privacy          : Button
    private lateinit var mRequest_Privacy_Button_Termsofservice   : Button
    private lateinit var mRequest_Privacy_Webview                 : WebView
    private lateinit var mRequestPrivacyDialogListener            : RequestPrivacyDialogListener

    fun showDialog() {
        init()
        //처음에 개인정보 처리방침 띄우기
        val url = BuildConfig.BASE_URL + BuildConfig.PRIVACY_END_POINT
        DM.getInstance().getWebView(mRequest_Privacy_Webview, url)
        getPrivacy()
        getTermsOfService()
        onOkClick()
        onCancelClick()
        show()
    }

    private fun init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        setContentView(R.layout.layout_request_policy_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴
        setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        //bind
        mRequest_Privacy_Button_OK               = findViewById(R.id.request_policy_button_ok)
        mRequest_Privacy_Button_Cancel           = findViewById(R.id.request_policy_button_cancel)
        mRequest_Privacy_Checkbox_Privacy        = findViewById(R.id.request_policy_checkbox_policy)
        mRequest_Privacy_Checkbox_Termsofservice = findViewById(R.id.request_policy_checkbox_termsofservice)
        mRequest_Privacy_Webview                 = findViewById(R.id.request_policy_webView)
        mRequest_Privacy_Button_Privacy          = findViewById(R.id.request_policy_button_policy)
        mRequest_Privacy_Button_Termsofservice   = findViewById(R.id.request_policy_button_termsofservice)
    }

    private fun getPrivacy(){
        mRequest_Privacy_Button_Privacy.setOnClickListener{
            val url = BuildConfig.BASE_URL + BuildConfig.PRIVACY_END_POINT
            DM.getInstance().getWebView(mRequest_Privacy_Webview, url)
        }
    }

    private fun getTermsOfService(){
        mRequest_Privacy_Button_Termsofservice.setOnClickListener{
            val url = BuildConfig.BASE_URL + BuildConfig.TERMSOFSERVICE_END_POINT
            DM.getInstance().getWebView(mRequest_Privacy_Webview, url)
        }
    }

    override fun onBackPressed() {
        //웹사이트에서 뒤로 갈 페이지 존재시
        if(mRequest_Privacy_Webview.canGoBack()) {
            mRequest_Privacy_Webview.goBack() // 웹사이트 뒤로가기
        }
        else {
            super.onBackPressed()
        }
    }

    private fun onOkClick(){
        mRequest_Privacy_Button_OK.setOnClickListener {
            if(!DM.getInstance().getInternetCheck(context)) return@setOnClickListener
            if(!mRequest_Privacy_Checkbox_Privacy.isChecked || !mRequest_Privacy_Checkbox_Termsofservice.isChecked){
                DM.getInstance().showToast(context, context.getString(R.string.request_policy_notcheck))
                return@setOnClickListener
            }
            mRequestPrivacyDialogListener.onClicked(true)
            dismiss()
        }
    }

    private fun onCancelClick(){
        mRequest_Privacy_Button_Cancel.setOnClickListener {
            mRequestPrivacyDialogListener.onClicked(false)
            dismiss()
        }
    }

    fun setOnClickedListener(requestPrivacyDialogListenerener: (Boolean) -> Unit) {
        this.mRequestPrivacyDialogListener = object: RequestPrivacyDialogListener {
            override fun onClicked(result: Boolean) {
                requestPrivacyDialogListenerener(result)
            }
        }
    }

    interface RequestPrivacyDialogListener {
        fun onClicked(result : Boolean)
    }
}