package com.nims.hsketch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response

class NoticeActivity : AppCompatActivity() {
    private lateinit var mNoticeList                : ArrayList<NoticeData>
    private lateinit var mNoticeAdapter             : NoticeAdapter
    private lateinit var mNotice_Recyclerview       : RecyclerView
    private lateinit var mNotice_Imageview_Back     : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
        init()

    }

    private fun init(){
        mNotice_Recyclerview                   = findViewById(R.id.notice_recyclerview)
        mNotice_Imageview_Back                 = findViewById(R.id.notice_imageview_back)
        mNoticeList                            = ArrayList()

        val recyclerlayoutManager              = LinearLayoutManager(this)
        recyclerlayoutManager.orientation      = LinearLayoutManager.VERTICAL

        mNotice_Recyclerview.layoutManager     = recyclerlayoutManager
        mNoticeAdapter                         = NoticeAdapter(this, mNoticeList, this)
        mNotice_Recyclerview.adapter           = mNoticeAdapter
        mNotice_Recyclerview.setHasFixedSize(true)

        onHttpNotice()
        onActivityFinish()
    }

    override fun finish() {
        super.finish()
        DM.getInstance().finishActivity(this)
    }

    private fun onActivityFinish(){
        mNotice_Imageview_Back.setOnClickListener {
            finish()
        }
    }

    private fun onHttpNotice(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "notice"))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpNoticeResult)
    }

    private fun onHttpNoticeResult(response: Response<ResponseBody>){
        try{
            //JSON 형태의 문자열 타입
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject             = JSONObject(responseStringFromJson)
            val success                = jsonObject.getBoolean("success")
            val items                  = jsonObject.getString("items")
            if(!success) return
            val jsonArray              = JSONArray(items)
            for (i in 0 until jsonArray.length()){
                val item               = jsonArray.getJSONObject(i)

                val notice_id         = item.getInt("notice_id")
                val notice_title      = item.getString("notice_title")
                val notice_date       = item.getString("notice_date")
                val notice_importance = item.getInt("notice_importance")
                val noticeData        = NoticeData(
                    notice_id,
                    notice_title,
                    notice_date,
                    notice_importance
                )

                mNoticeList   .add(noticeData)
                mNoticeAdapter.notifyItemInserted(mNoticeList.size-1)

            }
            Log.d("notice_response => ", jsonObject.toString())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private class NoticeAdapter(private val context: Context, private val arrayList: ArrayList<NoticeData>, private val activity: Activity) :
        RecyclerView.Adapter<NoticeAdapter.Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_notice, parent, false)
            return Holder(view)
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item            = arrayList[position]
            val importance      = item.getNoticeImfortance()
            var importance_text = ""
            when(importance){
                DM.mNoticeImportanceNormal -> {
                    importance_text = context.getString(R.string.notice_importance_normal)
                }
                DM.mNoticeImportanceImportant -> {
                    importance_text = context.getString(R.string.notice_importance_important)
                }
                DM.mNoticeImportanceUrgent -> {
                    importance_text = context.getString(R.string.notice_importance_ungent)
                }
            }
            holder.mNotice_Textview_Importance.text = importance_text
            holder.mNotice_Textview_Title.text = item.getNoticeTitle()
            holder.mNotice_Textview_Date.text = item.getNoticeDate()
            onNoticeDetail(holder, item)
        }

        private fun onNoticeDetail(holder: Holder, item: NoticeData){
            holder.mNotice_Cardview.setOnClickListener {
                val intent = Intent(context, NoticeDetailActivity::class.java)
                intent.putExtra(DM.mIntentkey_NoticeId, item.getNoticeId())
                DM.getInstance().startActivity(activity, intent)
            }
        }


        open inner class Holder(itemview: View) : RecyclerView.ViewHolder(itemview) {
            open var mNotice_Cardview            = itemview.findViewById<CardView>(R.id.notice_cardview)
            open var mNotice_Textview_Importance = itemview.findViewById<TextView>(R.id.notice_textview_importance)
            open var mNotice_Textview_Title      = itemview.findViewById<TextView>(R.id.notice_textview_title)
            open var mNotice_Textview_Date       = itemview.findViewById<TextView>(R.id.notice_textview_date)
        }
    }

    open class NoticeData(
        private val mNoticeId    : Int,
        private val mNoticeTitle  : String,
        private val mNoticeDate : String,
        private val mNoticeImfortance : Int,
    ){
        fun getNoticeId(): Int {
            return this.mNoticeId
        }

        fun getNoticeTitle(): String {
            return this.mNoticeTitle
        }

        fun getNoticeDate(): String {
            return this.mNoticeDate
        }

        fun getNoticeImfortance(): Int {
            return this.mNoticeImfortance
        }

       
    }
}