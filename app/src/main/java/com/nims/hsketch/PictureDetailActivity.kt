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
        onPictureDetail()
        onActivityFinish()
        onFavorite()
        onMenu()
    }

    private fun onFavorite(){
        mPicture_Detail_Imageview_Favorite.setOnClickListener {
            onFavoritePicture()
        }
    }

    private fun setLike(picture_like: String){
        mPicture_Detail_Imageview_Favorite.imageTintList = ColorStateList.valueOf(getColor(R.color.picture_favorite))
        mPicture_Detail_Textview_Favorite.text           = "회원님 외 $picture_like"
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
                onReportPicture()
            }
            R.id.picture_detail_menu_remove -> {
                if(user_id == mPicture_UserId) {
                    onRemovePicture()
                }else{
                    DM.getInstance().showToast(this, getString(R.string.picture_remove_user_not_admin))
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    //그림 상세정보
    private fun onPictureDetail(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_download_detail"))
        params.add(MultipartBody.Part.createFormData("picture_id", mPicture_Id))
        params.add(MultipartBody.Part.createFormData("user_id", mFirebaseAuth.currentUser.uid))
        //HTTP 통신
        DM.getInstance().onHTTP_POST_Connect(this, params, ::onPictureDetailResult)
    }

    private fun onPictureDetailResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject             = JSONObject(responseStringFromJson)
            val success                = jsonObject.get("success")
            if(success == false){
                DM.getInstance().showToast(this, getString(R.string.picture_detail_result_null))
                finish()
                return
            }

            val picture_title       = jsonObject.get("picture_title")      .toString()
            val picture_description = jsonObject.get("picture_description").toString()
            val picture_user_email  = jsonObject.get("picture_user_email") .toString().replace(DM.mGoogleEmailType, "")
            val picture_uploader    = DM.getInstance().stringToHide(picture_user_email) + DM.mGoogleEmailType
            val picture_favorite    = jsonObject.get("picture_favorite")

            mPicture_UserId                           = jsonObject.get("picture_user").toString()
            mPicture_Like                             = jsonObject.get("picture_like").toString().toInt()
            mPicture_Detail_Textview_Title.text       = picture_title
            mPicture_Detail_Textview_Description.text = picture_description

            val compress_pictureLike = DM.getInstance().compressInt(this, mPicture_Like)
            if(picture_favorite == true){
                setLike(compress_pictureLike)
            }else{
                setUnLike(compress_pictureLike)
            }

            val uri = Uri.parse(BuildConfig.BASE_URL + BuildConfig.BASE_PATH + mPicture_UserId +"/" + picture_title + DM.mFileExtension)
            Picasso.get().load(uri).into(mPicture_Detail_Imageview)
            TooltipCompat.setTooltipText(mPicture_Detail_Imageview, picture_uploader)

            Log.d("detail_response =>", jsonObject.toString())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //삭제하기
    private fun onRemovePicture(){
        val picture_path = BuildConfig.BASE_PATH + mPicture_UserId +"/" + mPicture_Detail_Textview_Title.text + DM.mFileExtension
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_remove"))
        params.add(MultipartBody.Part.createFormData("picture_id", mPicture_Id))
        params.add(MultipartBody.Part.createFormData("picture_path", picture_path))
        params.add(MultipartBody.Part.createFormData("user_id", mPicture_UserId))

        //HTTP 통신
        DM.getInstance().onHTTP_POST_Connect(this, params, ::onRemovePictureResult)
    }

    private fun onRemovePictureResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson  = response.body()!!.string() as String
            val jsonObject              = JSONObject(responseStringFromJson)
            val success                 = jsonObject.get("success")
            if(success == false) return

            DM.getInstance().showToast(this, getString(R.string.picture_remove_success))
            finish()

            Log.d("remove_response =>", jsonObject.toString())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //신고하기
    private fun onReportPicture(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_report"))
        params.add(MultipartBody.Part.createFormData("picture_id", mPicture_Id))
        params.add(MultipartBody.Part.createFormData("user_id", mPicture_UserId))
        params.add(MultipartBody.Part.createFormData("report_date", DM.getInstance().getNow()))

        //HTTP 통신
        DM.getInstance().onHTTP_POST_Connect(this, params, ::onReportPictureResult)
    }

    private fun onReportPictureResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson  = response.body()!!.string() as String
            val jsonObject              = JSONObject(responseStringFromJson)
            val success                 = jsonObject.get("success")
            if(success == false) return
            DM.getInstance().showToast(this, getString(R.string.picture_report_success))

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //좋아요 기능
    private fun onFavoritePicture(){
        val params = ArrayList<MultipartBody.Part>()
        params.add(MultipartBody.Part.createFormData("reqcmd", "picture_favorite"))
        params.add(MultipartBody.Part.createFormData("picture_id", mPicture_Id))
        params.add(MultipartBody.Part.createFormData("user_id", mFirebaseAuth.currentUser.uid))

        //HTTP 통신
        DM.getInstance().onHTTP_POST_Connect(this, params, ::onFavoritePictureResult)
    }

    private fun onFavoritePictureResult(response: Response<ResponseBody>){
        //JSON 형태의 문자열 타입
        try{
            val responseStringFromJson  = response.body()!!.string() as String
            val jsonObject              = JSONObject(responseStringFromJson)
            val favorite_state          = jsonObject.get("favorite_state")
            val picture_like            = DM.getInstance().compressInt(this, mPicture_Like)
            if(favorite_state == true) {
                setLike(picture_like)
            }else{
                setUnLike(picture_like)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

}