package com.nims.hsketch

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.TooltipCompat
import com.squareup.picasso.Picasso
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

class NoticeDetailActivity : AppCompatActivity() {
    private lateinit var mNotice_Detail_Imageview_Back      : ImageView
    private lateinit var mNotice_Detail_Textview_Title      : TextView
    private lateinit var mNotice_Detail_Textview_Content    : TextView
    private lateinit var mNotice_Detail_Textview_Date       : TextView
    private lateinit var mNotice_Detail_Textview_Importance : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_detail)
        init()
    }

    private fun init(){
        mNotice_Detail_Imageview_Back      = findViewById(R.id.notice_detail_imageview_back)
        mNotice_Detail_Textview_Title      = findViewById(R.id.notice_detail_textview_title)
        mNotice_Detail_Textview_Content    = findViewById(R.id.notice_detail_textview_content)
        mNotice_Detail_Textview_Date       = findViewById(R.id.notice_detail_textview_date)
        mNotice_Detail_Textview_Importance = findViewById(R.id.notice_detail_textview_importance)
        val notice_id                      = intent.getIntExtra(DM.mIntentkey_NoticeId, -1).toString()
        onHttpNoticeDetail(notice_id)
        onActivityFinish()
    }

    //그림 상세정보
    private fun onHttpNoticeDetail(notice_id: String){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "notice_detail"))
        params.add(MultipartBody.Part.createFormData("notice_id", notice_id))
        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpNoticeDetailResult)
    }

    private fun onHttpNoticeDetailResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject             = JSONObject(responseStringFromJson)
            val success                = jsonObject.getBoolean("success")
            if(!success){
                DM.getInstance().showToast(this, getString(R.string.notice_detail_result_null))
                finish()
                return
            }

            val notice_title      = jsonObject.getString("notice_title")
            val notice_content    = jsonObject.getString("notice_content")
            val notice_date       = jsonObject.getString("notice_date")
            val notice_importance = jsonObject.getInt("notice_importance")

            mNotice_Detail_Textview_Title.text      = notice_title
            mNotice_Detail_Textview_Content.text    = notice_content
            mNotice_Detail_Textview_Date.text       = notice_date
            var importance_text                     = ""
            when(notice_importance){
                DM.mNoticeImportanceNormal -> {
                    importance_text = getString(R.string.notice_importance_normal)
                }
                DM.mNoticeImportanceImportant -> {
                    importance_text = getString(R.string.notice_importance_important)
                }
                DM.mNoticeImportanceUrgent -> {
                    importance_text = getString(R.string.notice_importance_ungent)
                }
            }
            mNotice_Detail_Textview_Importance.text = importance_text


            Log.d("noticedetail =>", jsonObject.toString())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun finish() {
        super.finish()
        DM.getInstance().finishActivity(this)
    }

    private fun onActivityFinish(){
        mNotice_Detail_Imageview_Back.setOnClickListener {
            finish()
        }
    }
}