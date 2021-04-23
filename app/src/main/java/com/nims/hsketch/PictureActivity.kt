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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_picture.*
import kotlinx.android.synthetic.main.activity_picture_detail.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONArray
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

        onDrawing()
        onSelectTopMenu()
        onReceivePicture1FromServer()
        onReceivePicture2FromServer()
        onReceivePicture3FromServer()
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
    private fun onReceivePicture1FromServer(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_download_1"))

        //HTTP 통신
        DM.getInstance().onHTTP_POST_Connect(this, params, ::onReceivePicture1FromServerResult)
    }

    private fun onReceivePicture1FromServerResult(response: Response<ResponseBody>){
        try{
            //JSON 형태의 문자열 타입
            val responseStringFromJson = response.body()!!.string() as String
            val jsonArray              = JSONArray(responseStringFromJson)

            for (i in 0 until jsonArray.length()){
                val jsonObject         = jsonArray.getJSONObject(i)
                val picture_id         = jsonObject.get("picture_id")   .toString().toInt()
                val picture_user       = jsonObject.get("picture_user") .toString()
                val picture_title      = jsonObject.get("picture_title").toString()
                val picture_like       = jsonObject.get("picture_like") .toString().toInt()
                val pictureData        = PictureData(picture_id, picture_user, picture_title, picture_like)

                mPictureList_1   .add(pictureData)
                mPictureAdapter_1.notifyItemInserted(mPictureList_1.size-1)

                Log.d("picture1_response", jsonObject.toString())
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //신규작품순서
    private fun onReceivePicture2FromServer(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_download_2"))

        //HTTP 통신
        DM.getInstance().onHTTP_POST_Connect(this, params, ::onReceivePicture2FromServerResult)
    }

    private fun onReceivePicture2FromServerResult(response: Response<ResponseBody>){
        try{
            //JSON 형태의 문자열 타입
            val responseStringFromJson = response.body()!!.string() as String
            val jsonArray              = JSONArray(responseStringFromJson)

            for (i in 0 until jsonArray.length()){
                val jsonObject         = jsonArray.getJSONObject(i)
                val picture_id         = jsonObject.get("picture_id")   .toString().toInt()
                val picture_user       = jsonObject.get("picture_user") .toString()
                val picture_title      = jsonObject.get("picture_title").toString()
                val picture_like       = jsonObject.get("picture_like") .toString().toInt()
                val pictureData        = PictureData(picture_id, picture_user, picture_title, picture_like)

                mPictureList_2   .add(pictureData)
                mPictureAdapter_2.notifyItemInserted(mPictureList_2.size-1)

                Log.d("picture2_response", jsonObject.toString())
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //신규작품순서
    private fun onReceivePicture3FromServer(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_download_3"))

        //HTTP 통신
        DM.getInstance().onHTTP_POST_Connect(this, params, ::onReceivePicture3FromServerResult)
    }

    private fun onReceivePicture3FromServerResult(response: Response<ResponseBody>){
        try{
            //JSON 형태의 문자열 타입
            val responseStringFromJson = response.body()!!.string() as String
            val jsonArray              = JSONArray(responseStringFromJson)

            for (i in 0 until jsonArray.length()){
                val jsonObject         = jsonArray.getJSONObject(i)
                val picture_id         = jsonObject.get("picture_id")   .toString().toInt()
                val picture_user       = jsonObject.get("picture_user") .toString()
                val picture_title      = jsonObject.get("picture_title").toString()
                val picture_like       = jsonObject.get("picture_like") .toString().toInt()
                val pictureData        = PictureData(picture_id, picture_user, picture_title, picture_like)

                mPictureList_3   .add(pictureData)
                mPictureAdapter_3.notifyItemInserted(mPictureList_3.size-1)

                Log.d("picture3_response", jsonObject.toString())
            }
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
            holder.mLayout_Main_Picture_Textview_Like.text = DM.getInstance().compressInt(context, item.getPictureLike())

            Picasso.get().load(uri).into(holder.mLayout_Main_Picture_Imageview)

            onPictureClick(holder, item)
        }

        private fun onPictureClick(holder: Holder, item: PictureData){
            holder.mLayout_Main_Picture_Cardview.setOnClickListener {
                val intent = Intent(context, PictureDetailActivity::class.java)
                intent.putExtra(DM.mItentkey_PictureId, item.getPictureId())
                DM.getInstance().startActivityTransition(context, it, activity, intent)
            }
        }

        open inner class Holder(itemview: View?) : RecyclerView.ViewHolder(itemview!!) {
            var mLayout_Main_Picture_Imageview      = itemview!!.findViewById<ImageView>(R.id.layout_main_picture_imageview)
            var mLayout_Main_Picture_Cardview       = itemview!!.findViewById<CardView>(R.id.layout_main_picture_cardview)
            var mLayout_Main_Picture_Textview_Like  = itemview!!.findViewById<TextView>(R.id.layout_main_picture_textview_like)
            var mLayout_Main_Picture_Textview_Title = itemview!!.findViewById<TextView>(R.id.layout_main_picture_textview_title)
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