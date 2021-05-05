package com.nims.hsketch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DM {
    private val mStorageExternalPermissionRequestCode       = 1001
    private val mCompressIntRound                           = "%.1f"
    private val mDateFormatType                             = "yyyy-MM-dd HH:mm:ss"


    companion object {

        private var mInstance                   : DM?       = null

        open val mIntentkey_PictureId                       = "picture_id"
        open val mIntentkey_NoticeId                        = "notice_id"
        open val mIntentkey_Web                             = "web"
        open val mIntentKey_Policy                          = "policy"
        open val mIntentKey_Termsofservice                  = "termsofservice"
        open val mPreferencesKeyRemoveAdmob                 = "remove_admob_status"
        open val mStoreItemIdRemoveAdmob                    = "admob_remove"
        open val mStoreItemIdDonateToDev                    = "donate_to_developer"
        open var mFileExtension                             = ".png"
        open val mGoogleSignRequestCode                     = 1002
        open val mPictureStatusAgree                        = 1
        open val mPictureStatusUnagree                      = 2
        open val mNoticeImportanceNormal                    = 0
        open val mNoticeImportanceImportant                 = 1
        open val mNoticeImportanceUrgent                    = 2


        open fun getInstance(): DM{
            if(mInstance == null) mInstance                 = DM()
            return mInstance!!
        }
    }

    open fun compressInt(context: Context, value: Int): String{
        val arr = arrayOf(
            context.resources.getInteger(R.integer.compress_int_unit_1) .toFloat(),
            context.resources.getInteger(R.integer.compress_int_unit_2)     .toFloat(),
            context.resources.getInteger(R.integer.compress_int_unit_3)      .toFloat()
        )
        val round = mCompressIntRound
        if(value >= arr[0]){
            return String.format(round + context.getString(R.string.compress_int_unit_1), value/arr[0])
        }else if(value >= arr[1]){
            return String.format(round + context.getString(R.string.compress_int_unit_2), value/arr[1])
        }else if(value >= arr[2]){
            return String.format(round + context.getString(R.string.compress_int_unit_3), value/arr[2])
        }else{
            return value.toString()
        }
    }

    open fun startActivity(
        startActivity : Activity,
        endActivity   : Activity
    ) {
        val intent = Intent(startActivity, endActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity.startActivity(intent)
        startActivity.overridePendingTransition(R.anim.activity_slide_in, R.anim.activity_slide_out)
    }

    open fun startActivity(
        startActivity: Activity,
        intent: Intent
    ) {
        intent
        startActivity.startActivity(intent)
        startActivity.overridePendingTransition(R.anim.activity_slide_in, R.anim.activity_slide_out)
    }

    open fun startActivityTransition(
        context  : Context,
        view     : View,
        activity : Activity,
        intent   : Intent
    ) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, ViewCompat.getTransitionName(view)!!)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent, options.toBundle())
    }

    open fun finishActivity(activity: Activity){
        activity.overridePendingTransition(R.anim.activity_slide_enter, R.anim.activity_slide_exit)
    }

    open fun imageLocalSave(context: Context, bitmap: Bitmap, fileName: String): String {
        val saveName = fileName + DM.mFileExtension
        val stream   = context.openFileOutput(saveName, Context.MODE_PRIVATE)

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        return saveName
    }

    open fun imageLocalLoad(context: Context, fileName: String?): Bitmap {
        val stream = context.openFileInput(fileName)
        val bitmap = BitmapFactory.decodeStream(stream)

        stream.close()

        return bitmap
    }

    open fun imageExternalSave(context: Context, bitmap: Bitmap, path: String): Boolean {
        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state) {

            val rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
            val dirName  = "/" + path
            val fileName = System.currentTimeMillis().toString() + DM.mFileExtension
            val savePath = File(rootPath + dirName)
            savePath.mkdirs()

            val file     = File(savePath, fileName)
            if (file.exists()) file.delete()

            try {
                val outStream  = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream.flush()
                outStream.close()

                //갤러리 갱신
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())))

                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    open fun checkPermission(activity: Activity, permission: String): Boolean {
        val permissionChecker = ContextCompat.checkSelfPermission(activity.applicationContext, permission)

        //권한이 없으면 권한 요청
        if (permissionChecker == PackageManager.PERMISSION_GRANTED) return true
        ActivityCompat.requestPermissions(activity, arrayOf(permission), mStorageExternalPermissionRequestCode)

        return false
    }

    open fun onShareImage(context: Context, bitmap: Bitmap, title: String){
        val bitmapPath = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, title, null)
        val bitmapUri  = Uri.parse(bitmapPath)
        val intent     = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type    = "image/png"
        context!!.startActivity(intent)
    }

    open fun showToast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    open fun getNow(): String{
        val time             = System.currentTimeMillis()
        val date             = Date(time)
        val simpleDateFormat = SimpleDateFormat(mDateFormatType)
        val now              = simpleDateFormat.format(date)
        return now
    }

    open fun getInternetCheck(context: Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetworkInfo
        if (activeNetwork != null) {
            return (activeNetwork.type == ConnectivityManager.TYPE_WIFI || activeNetwork.type == ConnectivityManager.TYPE_MOBILE)
        }
        showToast(context, context.getString(R.string.internet_not_connected))
        return false
    }

    open fun getWebView(view: WebView, url: String){
        view.settings.javaScriptEnabled = true // 자바 스크립트 허용
        // 웹뷰안에 새 창이 뜨지 않도록 방지
        view.webViewClient   = WebViewClient()
        view.webChromeClient = WebChromeClient()
        view.loadUrl(url)
    }

    open fun setKeyboardHide(context: Context, view: View){
        //키보드 내리기
        val imm = context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // HTTP 통신 성공 후 리턴값을 매개변수로 UI 적용 메서드 호출
    open fun HTTP_POST_CONNECT(context: Context, params: ArrayList<MultipartBody.Part>, method: ((response: Response<ResponseBody>) -> Unit)?){
        if(!getInternetCheck(context)) return
        val retrofit           = Retrofit.Builder().baseUrl(BuildConfig.BASE_URL).build()
        val retrofit_interface = retrofit.create(retrofit_interface::class.java)

        retrofit_interface.POST(params).enqueue(object : Callback<ResponseBody> {

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("retrofit throwable", "" + t.message.toString())
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                //연동성공 시 UI 반영 메서드 호출
                if (response?.isSuccessful) {
                    if(method != null) method(response!!)
                } else {
                    showToast(context, context.getString(R.string.http_connect_failed))
                }
            }
        })
    }

    interface retrofit_interface {
        @Multipart
        @POST(BuildConfig.END_POINT)
        open fun POST(@Part file : ArrayList<MultipartBody.Part>): Call<ResponseBody>
    }
}