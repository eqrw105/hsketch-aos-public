package com.nims.hsketch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_picture.*
import kotlinx.android.synthetic.main.activity_picture_detail.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class PictureActivity : AppCompatActivity() {
    private lateinit var mPicture_Floatingactionbutton  : FloatingActionButton
    private lateinit var mPicture_Toolbar               : androidx.appcompat.widget.Toolbar

    private lateinit var mPictureList_1                 : ArrayList<PictureData>
    private lateinit var mPictureAdapter_1              : PictuerAdapter
    private lateinit var mPicture_Recyclerview_1        : RecyclerView

    private lateinit var mPictureList_2                 : ArrayList<PictureData>
    private lateinit var mPictureAdapter_2              : PictuerAdapter
    private lateinit var mPicture_Recyclerview_2        : RecyclerView

    private lateinit var mPictureList_3                 : ArrayList<PictureData>
    private lateinit var mPictureAdapter_3              : PictuerAdapter
    private lateinit var mPicture_Recyclerview_3        : RecyclerView

    private lateinit var mPicture_Imageview_Mainimage   : ImageView

    private lateinit var mPicture_Adview                : AdView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        init()

    }

    private fun init() {
        mPicture_Floatingactionbutton         = findViewById(R.id.picture_floatingactionbutton)
        mPicture_Toolbar                      = findViewById(R.id.picture_toolbar)
        mPicture_Recyclerview_1               = findViewById(R.id.picture_recyclerview_1)
        mPicture_Recyclerview_2               = findViewById(R.id.picture_recyclerview_2)
        mPicture_Recyclerview_3               = findViewById(R.id.picture_recyclerview_3)
        mPicture_Imageview_Mainimage          = findViewById(R.id.picture_imageview_mainimage)
        mPicture_Adview                       = findViewById(R.id.picture_adview)

        mPictureList_1                        = ArrayList()
        mPictureList_2                        = ArrayList()
        mPictureList_3                        = ArrayList()

        val recyclerlayoutManager_1           = LinearLayoutManager(this)
        recyclerlayoutManager_1.orientation   = LinearLayoutManager.HORIZONTAL
        
        val recyclerlayoutManager_2           = LinearLayoutManager(this)
        recyclerlayoutManager_2.orientation   = LinearLayoutManager.HORIZONTAL

        val recyclerlayoutManager_3           = LinearLayoutManager(this)
        recyclerlayoutManager_3.orientation   = LinearLayoutManager.HORIZONTAL

        mPicture_Recyclerview_1.layoutManager = recyclerlayoutManager_1
        mPictureAdapter_1                     = PictuerAdapter(this, mPictureList_1, this)
        mPicture_Recyclerview_1.adapter       = mPictureAdapter_1
        mPicture_Recyclerview_1.setHasFixedSize(true)

        mPicture_Recyclerview_2.layoutManager = recyclerlayoutManager_2
        mPictureAdapter_2                     = PictuerAdapter(this, mPictureList_2, this)
        mPicture_Recyclerview_2.adapter       = mPictureAdapter_2
        mPicture_Recyclerview_2.setHasFixedSize(true)

        mPicture_Recyclerview_3.layoutManager = recyclerlayoutManager_3
        mPictureAdapter_3                     = PictuerAdapter(this, mPictureList_3, this)
        mPicture_Recyclerview_3.adapter       = mPictureAdapter_3
        mPicture_Recyclerview_3.setHasFixedSize(true)

        Picasso.get().load(BuildConfig.BASE_URL + BuildConfig.MAIN_IMAGE_PATH).into(mPicture_Imageview_Mainimage)

        getAdmob()
        onDrawing()
        onSelectTopMenu()
        onHttpPicture1FromServer()
        onHttpPicture2FromServer()
        onHttpPicture3FromServer()
    }

    private fun getAdmob(){
        //배너
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        mPicture_Adview.loadAd(adRequest)
    }

    private fun onDrawing(){
        mPicture_Floatingactionbutton.setOnClickListener {
            DM.getInstance().startActivity(this, DrawingActivity())
        }
    }

    private fun onSelectTopMenu(){
        mPicture_Toolbar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.action_search ->{
                    DM.getInstance().startActivity(this, SearchActivity())
                }
                R.id.action_setting ->{
                    DM.getInstance().startActivity(this, SettingActivity())
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    //좋아요수 순서
    private fun onHttpPicture1FromServer(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_download_1"))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpPicture1FromServerResult)
    }

    private fun onHttpPicture1FromServerResult(response: Response<ResponseBody>){
        onUpdatePicture(response, mPictureList_1, mPictureAdapter_1)
    }

    //신규작품순서
    private fun onHttpPicture2FromServer(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_download_2"))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpPicture2FromServerResult)
    }

    private fun onHttpPicture2FromServerResult(response: Response<ResponseBody>){
        onUpdatePicture(response, mPictureList_2, mPictureAdapter_2)
    }

    //신규작품순서
    private fun onHttpPicture3FromServer(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_download_3"))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpPicture3FromServerResult)
    }

    private fun onHttpPicture3FromServerResult(response: Response<ResponseBody>){
        onUpdatePicture(response, mPictureList_3, mPictureAdapter_3)
    }

    //각각 조건별로 가져온 작품들을 UI에 적용
    private fun onUpdatePicture(response: Response<ResponseBody>, arrayList: ArrayList<PictureData>, adapter: PictuerAdapter){
        try{
            //JSON 형태의 문자열 타입
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject             = JSONObject(responseStringFromJson)
            val success                = jsonObject.getBoolean("success")
            if(!success) return
            val items                  = jsonObject.getString("items")
            val jsonArray              = JSONArray(items)

            for (i in 0 until jsonArray.length()){
                val item         = jsonArray.getJSONObject(i)
                val picture_id         = item.getInt("picture_id")
                val picture_user       = item.getString("picture_user")
                val picture_title      = item.getString("picture_title")
                val picture_like       = item.getInt("picture_like")
                val pictureData        = PictureData(picture_id, picture_user, picture_title, picture_like)

                arrayList.add(pictureData)
                adapter  .notifyItemInserted(arrayList.size-1)

            }
            Log.d("picture_response", jsonObject.toString())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    class PictuerAdapter(private val context: Context, private val arrayList: ArrayList<PictureData>,private val activity: Activity) :
        RecyclerView.Adapter<PictuerAdapter.Holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_main_picture, parent, false)
            return Holder(view)
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = arrayList[position]
            val uri  = Uri.parse(BuildConfig.BASE_URL + BuildConfig.BASE_PATH + item.getPictureUser() +"/" + item.getPictureTitle() + DM.mFileExtension)

            holder.mLayout_Main_Picture_Textview_Title.text = item.getPictureTitle()
            holder.mLayout_Main_Picture_Textview_Like.text  = DM.getInstance().compressInt(context, item.getPictureLike())

            Picasso.get().load(uri).into(holder.mLayout_Main_Picture_Imageview)

            onPictureClick(holder, item)
        }

        private fun onPictureClick(holder: Holder, item: PictureData){
            holder.mLayout_Main_Picture_Cardview.setOnClickListener {
                val intent = Intent(context, PictureDetailActivity::class.java)
                intent.putExtra(DM.mIntentkey_PictureId, item.getPictureId())
                DM.getInstance().startActivityTransition(context, it, activity, intent)
            }
        }

        open inner class Holder(itemview: View) : RecyclerView.ViewHolder(itemview) {
            var mLayout_Main_Picture_Imageview      = itemview.findViewById<ImageView>(R.id.layout_main_picture_imageview)
            var mLayout_Main_Picture_Cardview       = itemview.findViewById<CardView>(R.id.layout_main_picture_cardview)
            var mLayout_Main_Picture_Textview_Like  = itemview.findViewById<TextView>(R.id.layout_main_picture_textview_like)
            var mLayout_Main_Picture_Textview_Title = itemview.findViewById<TextView>(R.id.layout_main_picture_textview_title)
        }
    }

    open class PictureData(
        private val mPictureId    : Int,
        private val mPictureUser  : String,
        private val mPictureTitle : String,
        private val mPictureLike  : Int
    ){
        fun getPictureId(): Int {
            return this.mPictureId
        }

        fun getPictureUser(): String {
            return this.mPictureUser
        }

        fun getPictureTitle(): String {
            return this.mPictureTitle
        }

        fun getPictureLike(): Int {
            return this.mPictureLike
        }
    }
}