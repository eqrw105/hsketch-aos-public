package com.nims.hsketch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.util.zip.Inflater
import kotlin.properties.Delegates

class MypictureActivity : AppCompatActivity() {

    private lateinit var mMypictureList_Agree                : ArrayList<MypictureData>
    private lateinit var mMypictureAdapter_Agree             : MypictureAdapter
    private lateinit var mMypicture_Recyclerview_Agree       : RecyclerView
    private lateinit var mMypicture_Constraintlaouy_Checking : ConstraintLayout

    private lateinit var mMypictureList_Checking             : ArrayList<MypictureData>
    private lateinit var mMypictureAdapter_Checking          : MypictureAdapter
    private lateinit var mMypicture_Recyclerview_Checking    : RecyclerView
    private lateinit var mMypicture_Constraintlaouy_Agree    : ConstraintLayout

    private lateinit var mMypictureList_Unagree              : ArrayList<MypictureData>
    private lateinit var mMypictureAdapter_Unagree           : MypictureAdapter
    private lateinit var mMypicture_Recyclerview_Unagree     : RecyclerView
    private lateinit var mMypicture_Constraintlaouy_Unagree  : ConstraintLayout

    private lateinit var mMypicture_Imageview_Back           : ImageView

    private lateinit var mFirebaseAuth                       : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypicture)
        init()

    }

    private fun init(){
        mMypicture_Constraintlaouy_Checking             = findViewById(R.id.mypicture_constraintlayout_checking)
        mMypicture_Constraintlaouy_Agree                = findViewById(R.id.mypicture_constraintlayout_agree)
        mMypicture_Constraintlaouy_Unagree              = findViewById(R.id.mypicture_constraintlayout_unagree)

        mMypicture_Recyclerview_Agree                   = findViewById(R.id.mypicture_recyclerview_agree)
        mMypicture_Recyclerview_Checking                = findViewById(R.id.mypicture_recyclerview_checking)
        mMypicture_Recyclerview_Unagree                 = findViewById(R.id.mypicture_recyclerview_unagree)

        mMypicture_Imageview_Back                       = findViewById(R.id.mypicture_imageview_back)

        mMypictureList_Agree                            = ArrayList()
        mMypictureList_Checking                         = ArrayList()
        mMypictureList_Unagree                          = ArrayList()

        val recyclerlayoutManager_Agree                 = LinearLayoutManager(this)
        recyclerlayoutManager_Agree.orientation         = LinearLayoutManager.HORIZONTAL

        val recyclerlayoutManager_Checking              = LinearLayoutManager(this)
        recyclerlayoutManager_Checking.orientation      = LinearLayoutManager.HORIZONTAL

        val recyclerlayoutManager_Unagree               = LinearLayoutManager(this)
        recyclerlayoutManager_Unagree.orientation       = LinearLayoutManager.HORIZONTAL

        mMypicture_Recyclerview_Agree.layoutManager     = recyclerlayoutManager_Agree
        mMypictureAdapter_Agree                         = MypictureAdapter(this, mMypictureList_Agree, this)
        mMypicture_Recyclerview_Agree.adapter           = mMypictureAdapter_Agree
        mMypicture_Recyclerview_Agree.setHasFixedSize(true)

        mMypicture_Recyclerview_Checking.layoutManager  = recyclerlayoutManager_Checking
        mMypictureAdapter_Checking                      = MypictureAdapter(this, mMypictureList_Checking, this)
        mMypicture_Recyclerview_Checking.adapter        = mMypictureAdapter_Checking
        mMypicture_Recyclerview_Checking.setHasFixedSize(true)

        mMypicture_Recyclerview_Unagree.layoutManager   = recyclerlayoutManager_Unagree
        mMypictureAdapter_Unagree                       = MypictureAdapter(this, mMypictureList_Unagree, this)
        mMypicture_Recyclerview_Unagree.adapter         = mMypictureAdapter_Unagree
        mMypicture_Recyclerview_Unagree.setHasFixedSize(true)

        mFirebaseAuth                                   = FirebaseAuth.getInstance()

        onHttpMyPicture()
        onActivityFinish()
    }

    override fun finish() {
        super.finish()
        DM.getInstance().finishActivity(this)
    }

    private fun onActivityFinish(){
        mMypicture_Imageview_Back.setOnClickListener {
            finish()
        }
    }

    private fun onHttpMyPicture(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "my_picture_download"))
        params.add(MultipartBody.Part.createFormData("user_id", mFirebaseAuth.currentUser.uid))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpMyPictureResult)
    }

    private fun onHttpMyPictureResult(response: Response<ResponseBody>){
        try{
            //JSON 형태의 문자열 타입
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject             = JSONObject(responseStringFromJson)
            val success                = jsonObject.getBoolean("success")
            val items                  = jsonObject.getString("items")
            if(!success) return
            val jsonArray              = JSONArray(items)
            for (i in 0 until jsonArray.length()){
                val item         = jsonArray.getJSONObject(i)

                val picture_id        = item.getInt("picture_id")
                val picture_user      = item.getString("picture_user")
                val picture_title     = item.getString("picture_title")
                val picture_like      = item.getInt("picture_like")
                val picture_agree     = item.getInt("picture_agree")
                val pictureData       = MypictureData(
                    picture_id,
                    picture_user,
                    picture_title,
                    picture_agree,
                    picture_like
                )
                when(picture_agree){
                    //검토중
                    DM.mPictureStatusCheking -> {
                        mMypictureList_Checking   .add(pictureData)
                        mMypictureAdapter_Checking.notifyItemInserted(mMypictureList_Checking.size-1)

                    }
                    //승인
                    DM.mPictureStatusAgree -> {
                        mMypictureList_Agree   .add(pictureData)
                        mMypictureAdapter_Agree.notifyItemInserted(mMypictureList_Agree.size-1)
                    }
                    //거절
                    DM.mPictureStatusUnagree -> {
                        mMypictureList_Unagree   .add(pictureData)
                        mMypictureAdapter_Unagree.notifyItemInserted(mMypictureList_Unagree.size-1)
                    }
                }

                if(mMypictureList_Agree   .isNotEmpty()) mMypicture_Constraintlaouy_Agree.visibility    = View.VISIBLE
                if(mMypictureList_Checking.isNotEmpty()) mMypicture_Constraintlaouy_Checking.visibility = View.VISIBLE
                if(mMypictureList_Unagree .isNotEmpty()) mMypicture_Constraintlaouy_Unagree.visibility  = View.VISIBLE

            }
            Log.d("mypicture_response => ", jsonObject.toString())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private class MypictureAdapter(private val context: Context, private val arrayList: ArrayList<MypictureData>, private val activity: Activity) :
        RecyclerView.Adapter<MypictureAdapter.Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_mypicture_picture, parent, false)
            return Holder(view)
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item  = arrayList[position]
            val uri   = Uri.parse(BuildConfig.BASE_URL + BuildConfig.BASE_PATH + item.getMypictureUser() +"/" + item.getMypictureTitle() + DM.mFileExtension)

            holder.mLayout_Mypicture_Picture_Textview_Title.text = item.getMypictureTitle()
            holder.mLayout_Mypicture_Picture_Textview_Like.text  = DM.getInstance().compressInt(context, item.getMypictureLike())

            Picasso.get().load(uri).into(holder.mLayout_Mypicture_Picture_Imageview)

            onPictureClick(holder, item)
            onRemoveMenu(holder, position, item)
        }

        private fun onPictureClick(holder: Holder, item: MypictureData){
            holder.mLayout_Mypicture_Picture_Cardview.setOnClickListener {
                if(item.getMypictureAgree() == DM.mPictureStatusAgree) {
                    //승인된 게시물이면 상세페이지로 이동
                    val intent = Intent(context, PictureDetailActivity::class.java)
                    intent.putExtra(DM.mIntentkey_PictureId, item.getMypictureId())
                    DM.getInstance().startActivityTransition(context, it, activity, intent)
                }else{
                    //승인 안된 게시물이면 업로드 취소할건지 띄우기
                    activity.registerForContextMenu(it)
                    activity.openContextMenu(it)
                    activity.unregisterForContextMenu(it)
                }
            }
        }

        private fun onRemoveMenu(holder: Holder, position: Int, item: MypictureData){
            holder.itemView.setOnCreateContextMenuListener(object :View.OnCreateContextMenuListener{
                override fun onCreateContextMenu(contextMenu: ContextMenu?, view: View?, contextMenuInfo: ContextMenu.ContextMenuInfo?) {
                    val inflater = activity.menuInflater
                    inflater.inflate(R.menu.mypicture_upload_cancel, contextMenu)
                    contextMenu!!.getItem(0).setOnMenuItemClickListener {
                        val firebaseAuth = FirebaseAuth.getInstance()
                        val user_id      = firebaseAuth.currentUser.uid
                        onHttpRemovePicture(item.getMypictureId(), item.getMypictureTitle(), user_id, position)

                        return@setOnMenuItemClickListener true
                    }
                }
            })
        }

        private fun onHttpRemovePicture(picture_id: Int, picture_title: String, user_id: String, position: Int){
            val picture_path = BuildConfig.BASE_PATH + user_id +"/" + picture_title + DM.mFileExtension
            val params = ArrayList<MultipartBody.Part>()
            params.add(MultipartBody.Part.createFormData("reqcmd", "picture_remove"))
            params.add(MultipartBody.Part.createFormData("picture_id", picture_id.toString()))
            params.add(MultipartBody.Part.createFormData("picture_path", picture_path))
            params.add(MultipartBody.Part.createFormData("user_id", user_id))

            //HTTP 통신
            DM.getInstance().HTTP_POST_CONNECT(activity, params, null)

            if(itemCount>1) {
                arrayList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeRemoved(0, arrayList.size)
            }else{
                arrayList.removeAt(position)
                notifyDataSetChanged()
            }
        }

        open inner class Holder(itemview: View) : RecyclerView.ViewHolder(itemview) {
            var mLayout_Mypicture_Picture_Imageview      = itemview.findViewById<ImageView>(R.id.layout_mypicture_picture_imageview)
            var mLayout_Mypicture_Picture_Cardview       = itemview.findViewById<CardView>(R.id.layout_mypicture_picture_cardview)
            var mLayout_Mypicture_Picture_Textview_Like  = itemview.findViewById<TextView>(R.id.layout_mypicture_picture_textview_like)
            var mLayout_Mypicture_Picture_Textview_Title = itemview.findViewById<TextView>(R.id.layout_mypicture_picture_textview_title)
        }
    }

    open class MypictureData(
        private val mMypictureId    : Int,
        private val mMypictureUser  : String,
        private val mMypictureTitle : String,
        private val mMypictureAgree : Int,
        private val mMypictureLike  : Int
    ){
        fun getMypictureId(): Int {
            return this.mMypictureId
        }

        fun getMypictureUser(): String {
            return this.mMypictureUser
        }

        fun getMypictureTitle(): String {
            return this.mMypictureTitle
        }

        fun getMypictureAgree(): Int {
            return this.mMypictureAgree
        }

        fun getMypictureLike(): Int {
            return this.mMypictureLike
        }
    }
}