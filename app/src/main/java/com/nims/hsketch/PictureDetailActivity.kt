package com.nims.hsketch

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import kotlin.properties.Delegates

class PictureDetailActivity : AppCompatActivity() {
    private lateinit var mPicture_Detail_Textview_Title       : TextView
    private lateinit var mPicture_Detail_Textview_Description : TextView
    private lateinit var mPicture_Detail_Textview_Favorite    : TextView
    private lateinit var mPicture_Detail_Imageview_Back       : ImageView
    private lateinit var mPicture_Detail_Imageview            : ImageView
    private lateinit var mPicture_Detail_Imageview_Favorite   : ImageView
    private lateinit var mPicture_Detail_Imageview_Menu       : ImageView
    private lateinit var mPicture_UserId                      : String
    private lateinit var mPicture_Id                          : String
    private lateinit var mFirebaseAuth                        : FirebaseAuth
    private var mPicture_Like by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_detail)
        init()
    }

    private fun init(){
        //bind
        mPicture_Detail_Textview_Title       = findViewById(R.id.picture_detail_textview_title)
        mPicture_Detail_Textview_Description = findViewById(R.id.picture_detail_textview_description)
        mPicture_Detail_Imageview_Back       = findViewById(R.id.picture_detail_imageview_back)
        mPicture_Detail_Imageview            = findViewById(R.id.picture_detail_imageview)
        mPicture_Detail_Imageview_Favorite   = findViewById(R.id.picture_detail_imageview_favorite)
        mPicture_Detail_Textview_Favorite    = findViewById(R.id.picture_detail_textview_favorite)
        mPicture_Detail_Imageview_Menu       = findViewById(R.id.picture_detail_imageview_menu)
        mFirebaseAuth                        = FirebaseAuth.getInstance()
        mPicture_Id                          = intent.getIntExtra(DM.mIntentkey_PictureId, -1).toString()
        mPicture_Like                        = 0
        onHttpPictureDetail()
        onActivityFinish()
        onFavorite()
        onMenu()
    }

    private fun onFavorite(){
        mPicture_Detail_Imageview_Favorite.setOnClickListener {
            onHttpFavoritePicture()
        }
    }

    private fun setLike(picture_like: String){
        mPicture_Detail_Imageview_Favorite.imageTintList = ColorStateList.valueOf(getColor(R.color.picture_favorite))
        mPicture_Detail_Textview_Favorite.text           = getString(R.string.favorite_status_true_text) + " " + picture_like
    }

    private fun setUnLike(picture_like: String){
        mPicture_Detail_Imageview_Favorite.imageTintList = null
        mPicture_Detail_Textview_Favorite.text           = picture_like
    }

    private fun onActivityFinish(){
        mPicture_Detail_Imageview_Back.setOnClickListener {
            supportFinishAfterTransition()
        }
    }

    private fun onMenu(){
        mPicture_Detail_Imageview_Menu.setOnClickListener {
            this.registerForContextMenu(it)
            openContextMenu(it)
            unregisterForContextMenu(it)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        val inflater = menuInflater
        inflater.inflate(R.menu.picture_detail, menu)
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val user_id = mFirebaseAuth.currentUser.uid
        when(item.itemId){
            R.id.picture_detail_menu_report -> {
                onHttpReportPicture()
            }
            R.id.picture_detail_menu_remove -> {
                if(user_id == mPicture_UserId) {
                    onHttpRemovePicture()
                }else{
                    DM.getInstance().showToast(this, getString(R.string.picture_remove_user_not_admin))
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    //그림 상세정보
    private fun onHttpPictureDetail(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_download_detail"))
        params.add(MultipartBody.Part.createFormData("picture_id", mPicture_Id))
        params.add(MultipartBody.Part.createFormData("user_id", mFirebaseAuth.currentUser.uid))
        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpPictureDetailResult)
    }

    private fun onHttpPictureDetailResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject             = JSONObject(responseStringFromJson)
            val success                = jsonObject.getBoolean("success")
            if(!success){
                DM.getInstance().showToast(this, getString(R.string.picture_detail_result_null))
                finish()
                return
            }

            val picture_title       = jsonObject.getString("picture_title")
            val picture_description = jsonObject.getString("picture_description")
            val picture_user_email  = jsonObject.getString("picture_user_email")
            val picture_favorite    = jsonObject.getBoolean("picture_favorite")

            mPicture_UserId                           = jsonObject.getString("picture_user")
            mPicture_Like                             = jsonObject.getInt("picture_like")
            mPicture_Detail_Textview_Title.text       = picture_title
            mPicture_Detail_Textview_Description.text = picture_description

            if(picture_favorite){
                val compress_pictureLike = DM.getInstance().compressInt(this, --mPicture_Like)
                setLike(compress_pictureLike)
            }else{
                val compress_pictureLike = DM.getInstance().compressInt(this, mPicture_Like)
                setUnLike(compress_pictureLike)
            }

            val uri = Uri.parse(BuildConfig.BASE_URL + BuildConfig.BASE_PATH + mPicture_UserId +"/" + picture_title + DM.mFileExtension)
            Picasso.get().load(uri).into(mPicture_Detail_Imageview)
            TooltipCompat.setTooltipText(mPicture_Detail_Imageview, picture_user_email)

            Log.d("detail_response =>", jsonObject.toString())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //삭제하기
    private fun onHttpRemovePicture(){
        val picture_path = BuildConfig.BASE_PATH + mPicture_UserId +"/" + mPicture_Detail_Textview_Title.text + DM.mFileExtension
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_remove"))
        params.add(MultipartBody.Part.createFormData("picture_id", mPicture_Id))
        params.add(MultipartBody.Part.createFormData("picture_path", picture_path))
        params.add(MultipartBody.Part.createFormData("user_id", mPicture_UserId))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpRemovePictureResult)
    }

    private fun onHttpRemovePictureResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson  = response.body()!!.string() as String
            val jsonObject              = JSONObject(responseStringFromJson)
            val success                 = jsonObject.getBoolean("success")
            if(!success) return

            DM.getInstance().showToast(this, getString(R.string.picture_remove_success))
            finish()

            Log.d("remove_response =>", jsonObject.toString())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //신고하기
    private fun onHttpReportPicture(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_report"))
        params.add(MultipartBody.Part.createFormData("picture_id", mPicture_Id))
        params.add(MultipartBody.Part.createFormData("user_id", mPicture_UserId))
        params.add(MultipartBody.Part.createFormData("report_date", DM.getInstance().getNow()))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpReportPictureResult)
    }

    private fun onHttpReportPictureResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson  = response.body()!!.string() as String
            val jsonObject              = JSONObject(responseStringFromJson)
            val success                 = jsonObject.getBoolean("success")
            if(!success) return
            DM.getInstance().showToast(this, getString(R.string.picture_report_success))

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //좋아요 기능
    private fun onHttpFavoritePicture(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_favorite"))
        params.add(MultipartBody.Part.createFormData("picture_id", mPicture_Id))
        params.add(MultipartBody.Part.createFormData("user_id", mFirebaseAuth.currentUser.uid))

        //HTTP 통신
        DM.getInstance().HTTP_POST_CONNECT(this, params, ::onHttpFavoritePictureResult)
    }

    private fun onHttpFavoritePictureResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson  = response.body()!!.string() as String
            val jsonObject              = JSONObject(responseStringFromJson)
            val favorite_state          = jsonObject.getBoolean("favorite_state")
            val picture_like            = DM.getInstance().compressInt(this, mPicture_Like)
            if(favorite_state) {
                setLike(picture_like)
            }else{
                setUnLike(picture_like)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

}