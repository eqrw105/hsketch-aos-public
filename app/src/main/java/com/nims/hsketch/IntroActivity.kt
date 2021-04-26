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
import org.json.JSONObject
import retrofit2.Response

class IntroActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    private lateinit var mFirebaseAuth              : FirebaseAuth
    private lateinit var mGoogleApiClient           : GoogleApiClient
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

        onHttpAppVersion()
        onRegister()
    }

    //버전 체크
    private fun onHttpAppVersion(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "config_version"))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpAppVersionResult)
    }

    private fun onHttpAppVersionResult(response: Response<ResponseBody>){
        try{
            //JSON 형태의 문자열 타입
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject     = JSONObject(responseStringFromJson)
            val success        = jsonObject.getBoolean("success")
            if(!success) return
            val config_version = jsonObject.getString("config_version")
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
            //마지막 접속일 업데이트
            onHttpLastDateUpdate(currentUser.uid, DM.getInstance().getNow())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun onRegister(){
        mIntro_Sigininbutton_Google.setOnClickListener {
            if(!DM.getInstance().getInternetCheck(this)) return@setOnClickListener
            val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(intent, DM.mGoogleSignRequestCode)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != DM.mGoogleSignRequestCode) return

        val result     = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
        if (!result!!.isSuccess) return

        val account    = result.signInAccount
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)

        mFirebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                //성공
                val currentUser = mFirebaseAuth.currentUser
                //첫 로그인(회원가입)일 때는 정보 DB 입력
                if (it.result!!.additionalUserInfo.isNewUser) {
                    Log.d("googleLogin", "user register")
                    onHttpRegister(
                        currentUser.uid,
                        currentUser.email,
                        currentUser.displayName,
                        DM.getInstance().getNow()
                    )
                    return@addOnCompleteListener
                }
                Log.d("googleLogin", "user login")
                //이미 가입한사람이 로그인하면 마지막 접속일 업데이트
                onHttpLastDateUpdate(currentUser.uid, DM.getInstance().getNow())
            }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    //회원가입
    private fun onHttpRegister(user_id: String, user_email: String, user_name: String, user_date: String){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "user_register"))
        params.add(MultipartBody.Part.createFormData("user_id", user_id))
        params.add(MultipartBody.Part.createFormData("user_email", user_email))
        params.add(MultipartBody.Part.createFormData("user_name", user_name))
        params.add(MultipartBody.Part.createFormData("user_date", user_date))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpRegisterResult)
    }

    //회원가입 결과
    private fun onHttpRegisterResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject             = JSONObject(responseStringFromJson)
            val success                = jsonObject.getBoolean("success")
            if (success) onLogin()

            Log.d("register_response =>", jsonObject.toString())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //로그인시 마지막 접속일 업데이트
    private fun onHttpLastDateUpdate(user_id: String, user_lastdate: String){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "user_lastdate_update"))
        params.add(MultipartBody.Part.createFormData("user_id", user_id))
        params.add(MultipartBody.Part.createFormData("user_lastdate", user_lastdate))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpLastDateUpdateResult)
    }

    //마지막 접속일 업데이트 결과
    private fun onHttpLastDateUpdateResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject             = JSONObject(responseStringFromJson)
            val success                = jsonObject.getBoolean("success")
            if (success) onLogin()

            Log.d("update_response =>", jsonObject.toString())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun onLogin(){
        DM.getInstance().startActivity(this, PictureActivity())
        finish()
    }

}