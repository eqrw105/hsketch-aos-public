package com.nims.hsketch

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.webkit.WebView
import android.widget.Button
import android.widget.CheckBox

class RequestPolicyDialog(context: Context): Dialog(context) {
    private lateinit var mRequest_Policy_Button_OK               : Button
    private lateinit var mRequest_Policy_Button_Cancel           : Button
    private lateinit var mRequest_Policy_Checkbox_Policy         : CheckBox
    private lateinit var mRequest_Policy_Checkbox_Termsofservice : CheckBox
    private lateinit var mRequest_Policy_Button_Policy           : Button
    private lateinit var mRequest_Policy_Button_Termsofservice   : Button
    private lateinit var mRequest_Policy_Webview                 : WebView
    private lateinit var mRequestPolicyDialogListener            : RequestPolicyDialogListener

    fun showDialog() {
        init()
        //처음에 개인정보 처리방침 띄우기
        val url = BuildConfig.BASE_URL + BuildConfig.POLICY_END_POINT
        DM.getInstance().getWebView(mRequest_Policy_Webview, url)
        getPolicy()
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
        mRequest_Policy_Button_OK               = findViewById(R.id.request_policy_button_ok)
        mRequest_Policy_Button_Cancel           = findViewById(R.id.request_policy_button_cancel)
        mRequest_Policy_Checkbox_Policy         = findViewById(R.id.request_policy_checkbox_policy)
        mRequest_Policy_Checkbox_Termsofservice = findViewById(R.id.request_policy_checkbox_termsofservice)
        mRequest_Policy_Webview                 = findViewById(R.id.request_policy_webView)
        mRequest_Policy_Button_Policy           = findViewById(R.id.request_policy_button_policy)
        mRequest_Policy_Button_Termsofservice   = findViewById(R.id.request_policy_button_termsofservice)
    }

    private fun getPolicy(){
        mRequest_Policy_Button_Policy.setOnClickListener{
            val url = BuildConfig.BASE_URL + BuildConfig.POLICY_END_POINT
            DM.getInstance().getWebView(mRequest_Policy_Webview, url)
        }
    }

    private fun getTermsOfService(){
        mRequest_Policy_Button_Termsofservice.setOnClickListener{
            val url = BuildConfig.BASE_URL + BuildConfig.TERMSOFSERVICE_END_POINT
            DM.getInstance().getWebView(mRequest_Policy_Webview, url)
        }
    }

    override fun onBackPressed() {
        //웹사이트에서 뒤로 갈 페이지 존재시
        if(mRequest_Policy_Webview.canGoBack()) {
            mRequest_Policy_Webview.goBack() // 웹사이트 뒤로가기
            return
        }
        super.onBackPressed()
    }


    private fun onOkClick(){
        mRequest_Policy_Button_OK.setOnClickListener {
            if(!DM.getInstance().getInternetCheck(context)) return@setOnClickListener
            if(!mRequest_Policy_Checkbox_Policy.isChecked || !mRequest_Policy_Checkbox_Termsofservice.isChecked){
                DM.getInstance().showToast(context, context.getString(R.string.request_policy_notcheck))
                return@setOnClickListener
            }
            mRequestPolicyDialogListener.onClicked(true)
            dismiss()
        }
    }

    private fun onCancelClick(){
        mRequest_Policy_Button_Cancel.setOnClickListener {
            mRequestPolicyDialogListener.onClicked(false)
            dismiss()
        }
    }

    fun setOnClickedListener(requestPolicyDialogListenerener: (Boolean) -> Unit) {
        this.mRequestPolicyDialogListener = object: RequestPolicyDialogListener {
            override fun onClicked(result: Boolean) {
                requestPolicyDialogListenerener(result)
            }
        }
    }

    interface RequestPolicyDialogListener {
        fun onClicked(result : Boolean)
    }
}