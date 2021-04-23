package com.nims.hsketch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response

class IntroActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mIntro_Sigininbutton_Google: SignInButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        init()
    }

    private fun init(){
        mIntro_Sigininbutton_Google = findViewById(R.id.intro_sigininbutton_google)

        mFirebaseAuth               = FirebaseAuth.getInstance()

        val googleSignInOptions     = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleApiClient            = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
            .build()

        onAppVersion()
        onLogin()
    }

    //버전 체크
    private fun onAppVersion(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "config_version"))

        //HTTP 통신
        DM.getInstance().onHTTP_POST_Connect(this, params, ::onAppVersionResult)
    }

    private fun onAppVersionResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        val responseStringFromJson = response.body()!!.string() as String

        try{
            val jsonArray = JSONArray(responseStringFromJson)
            for (i in 0 until jsonArray.length()){
                val jsonObject     = jsonArray.getJSONObject(i)
                val config_version = jsonObject.get("config_version").toString()
                val currentUser    = mFirebaseAuth.currentUser

                Log.d("version => ", jsonObject.toString())
                //버전 일치하면 다음 엑티비티
                if(BuildConfig.VERSION_NAME != config_version) {
                    //버전 미일치
                    return
                }

                //이미 로그인중인가?
                if(currentUser == null){
                    //아니면 로그인하는 버튼 보여주기
                    val animation                          = AnimationUtils.loadAnimation(this, R.anim.activity_slide_in)
                    mIntro_Sigininbutton_Google.visibility = View.VISIBLE
                    mIntro_Sigininbutton_Google.animation  = animation
                    return
                }
                //맞으면 메인화면으로 이동
                DM.getInstance().startActivity(this, PictureActivity())
                finish()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun onLogin(){
        mIntro_Sigininbutton_Google.setOnClickListener {
            val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(intent, DM.mGoogleSignRequestCode)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == DM.mGoogleSignRequestCode){
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if(result!!.isSuccess){
                val account    = result.signInAccount
                val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)

                mFirebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener {
                        if(it.isSuccessful){//성공
                            Log.d("googleLogin", "success")

                            val currentUser = mFirebaseAuth.currentUser
                            onRegister(currentUser.uid, currentUser.email)
                            DM.getInstance().startActivity(this, PictureActivity())
                            finish()
                        }
                    }
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    //버전 체크
    private fun onRegister(user_id: String, user_email: String){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "user_register"))
        params.add(MultipartBody.Part.createFormData("user_id", user_id))
        params.add(MultipartBody.Part.createFormData("user_email", user_email))

        //HTTP 통신
        DM.getInstance().onHTTP_POST_Connect(this, params, ::onRegisterResult)
    }

    private fun onRegisterResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject             = JSONObject(responseStringFromJson)

            Log.d("register_response =>", jsonObject.toString())
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

}