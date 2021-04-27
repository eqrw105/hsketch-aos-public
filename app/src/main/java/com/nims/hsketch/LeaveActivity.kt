package com.nims.hsketch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

class LeaveActivity : AppCompatActivity() {
    private lateinit var mLeave_Imageview_Back : ImageView
    private lateinit var mLeave_Button_Agree   : Button
    private lateinit var mLeave_Checkbox_Agree : CheckBox
    private lateinit var mFirebaseAuth         : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave)
        init()
    }

    private fun init(){
        //bind
        mLeave_Imageview_Back = findViewById(R.id.leave_imageview_back)
        mLeave_Button_Agree   = findViewById(R.id.leave_button_agree)
        mLeave_Checkbox_Agree = findViewById(R.id.leave_checkbox_agree)
        mFirebaseAuth         = FirebaseAuth.getInstance()

        onActivityFinish()
        onLeave()
    }

    private fun onLeave(){
        mLeave_Button_Agree.setOnClickListener {
            val cheked = mLeave_Checkbox_Agree.isChecked
            if(!cheked) {
                DM.getInstance().showToast(this, getString(R.string.leave_notcheck))
                return@setOnClickListener
            }
            onHttpLeave()
        }
    }

    override fun finish() {
        super.finish()
        DM.getInstance().finishActivity(this)
    }

    private fun onActivityFinish(){
        mLeave_Imageview_Back.setOnClickListener {
            finish()
        }
    }

    private fun onHttpLeave(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "user_leave"))
        params.add(MultipartBody.Part.createFormData("user_id", mFirebaseAuth.currentUser.uid))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpLeaveResult)

    }

    private fun onHttpLeaveResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject             = JSONObject(responseStringFromJson)
            val success                = jsonObject.getBoolean("success")
            Log.d("leave_response =>", jsonObject.toString())
            if(!success) return
            mFirebaseAuth.currentUser.delete().addOnCompleteListener {
                if(it.isSuccessful){
                    finishAffinity()
                    DM.getInstance().startActivity(this, IntroActivity())
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}