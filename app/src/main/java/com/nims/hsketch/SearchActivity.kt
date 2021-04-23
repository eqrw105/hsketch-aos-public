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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    private lateinit var mSearch_Imageview_Back : ImageView
    private lateinit var mSearch_Searchview     : androidx.appcompat.widget.SearchView

    //리사이클러뷰
    private lateinit var mSearchList            : ArrayList<SearchData>
    private lateinit var mSearchAdapter         : SearchAdapter
    private lateinit var mSearchRecyclerview    : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        init()
    }

    private fun init(){
        mSearch_Imageview_Back            = findViewById(R.id.search_imageview_back)
        mSearch_Searchview                = findViewById(R.id.search_searchview)
        mSearchRecyclerview               = findViewById(R.id.search_recyclerview)

        mSearchList                       = ArrayList()

        val recyclerlayoutManager         = LinearLayoutManager(this)
        recyclerlayoutManager.orientation = LinearLayoutManager.VERTICAL

        mSearchRecyclerview.layoutManager = recyclerlayoutManager
        mSearchAdapter                    = SearchAdapter(this, mSearchList, this)
        mSearchRecyclerview.adapter       = mSearchAdapter
        mSearchRecyclerview.setHasFixedSize(true)

        onActivityFinish()
        onSearch()
    }

    private fun onSearch(){
        mSearch_Searchview.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                onSearchPicture(p0!!)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })
    }

    private fun onActivityFinish(){
        mSearch_Imageview_Back.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        DM.getInstance().finishActivity(this)
    }

    private fun onSearchPicture(search_keyword: String){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_download_search"))
        params.add(MultipartBody.Part.createFormData("search_keyword", search_keyword))

        //HTTP 통신
        DM.getInstance().onHTTP_POST_Connect(this, params, ::onSearchPictureResult)
    }

    private fun onSearchPictureResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            mSearchList.clear()
            mSearchAdapter.notifyDataSetChanged()

            val responseStringFromJson  = response.body()!!.string() as String
            val jsonArray               = JSONArray(responseStringFromJson)

            if(jsonArray.length() == 0) DM.getInstance().showToast(this, getString(R.string.search_result_null))

            for (i in 0 until jsonArray.length()){
                val jsonObject          = jsonArray.getJSONObject(i)
                val picture_id          = jsonObject.get("picture_id").toString().toInt()
                val picture_user        = jsonObject.get("picture_user").toString()
                val picture_title       = jsonObject.get("picture_title").toString()
                val picture_description = jsonObject.get("picture_description").toString()
                val picture_like        = jsonObject.get("picture_like").toString().toInt()
                val pictureData = SearchData(
                    picture_id,
                    picture_user,
                    picture_title,
                    picture_description,
                    picture_like
                )
                mSearchList.add(pictureData)
                mSearchAdapter.notifyItemInserted(mSearchList.size-1)

                Log.d("search_response =>", jsonObject.toString())
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    class SearchAdapter(private val context: Context, private val arrayList: ArrayList<SearchData>,private val activity: Activity) :
        RecyclerView.Adapter<SearchAdapter.Holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_search_picture, parent, false)
            return Holder(view)
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = arrayList[position]
            holder.mLayout_Search_Picture_Textview_Title.text       = item.getPictureTitle()
            holder.mLayout_Search_Picture_Textview_Description.text = item.getPictureDescription()
            holder.mLayout_Search_Picture_Textview_Like.text        = DM.getInstance().compressInt(context, item.getPictureLike())


            val uri = Uri.parse(BuildConfig.BASE_URL + BuildConfig.BASE_PATH + item.getPictureUser() +"/" + item.getPictureTitle() + DM.mFileExtension)
            Picasso.get().load(uri).into(holder.mLayout_Search_Picture_Imageview)

            holder.mLayout_Search_Picture_Constraintlayout.setOnClickListener {
                val intent = Intent(context, PictureDetailActivity::class.java)
                intent.putExtra(DM.mIntentkey_PictureId, item.getPictureId())
                DM.getInstance().startActivityTransition(context, holder.mLayout_Search_Picture_Imageview, activity, intent)
            }

        }

        inner class Holder(itemview: View?) : RecyclerView.ViewHolder(itemview!!) {
            open var mLayout_Search_Picture_Constraintlayout     = itemview!!.findViewById<ConstraintLayout>(R.id.layout_search_picture_constraintlayout)
            open var mLayout_Search_Picture_Textview_Title       = itemview!!.findViewById<TextView>(R.id.layout_search_picture_textview_title)
            open var mLayout_Search_Picture_Textview_Description = itemview!!.findViewById<TextView>(R.id.layout_search_picture_textview_description)
            open var mLayout_Search_Picture_Textview_Like        = itemview!!.findViewById<TextView>(R.id.layout_search_picture_textview_like)
            open var mLayout_Search_Picture_Imageview            = itemview!!.findViewById<ImageView>(R.id.layout_search_picture_imageview)

        }


    }

    class SearchData(
        private val mPictureId          : Int,
        private val mPictureUser        : String,
        private val mPictureTitle       : String,
        private val mPictureDescription : String,
        private val mPictureLike        : Int
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

        fun getPictureDescription(): String {
            return this.mPictureDescription
        }

        fun getPictureLike(): Int {
            return this.mPictureLike
        }


    }

}