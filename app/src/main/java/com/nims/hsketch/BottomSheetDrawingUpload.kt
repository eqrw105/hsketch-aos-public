package com.nims.hsketch

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*
import java.io.*
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList

class BottomSheetDrawingUpload(
    private val mPicture: Bitmap
    ) : BottomSheetDialogFragment() {

    private lateinit var mDrawing_Upload_Imageview_Picture    : ImageView
    private lateinit var mDrawing_Upload_Imageview_Close      : ImageView
    private lateinit var mDrawing_Upload_Button_Gallery       : Button
    private lateinit var mDrawing_Upload_Button_Shared        : Button
    private lateinit var mDrawing_Upload_Button               : Button
    private lateinit var mDrawing_Upload_Edittext_Title       : EditText
    private lateinit var mDrawing_Upload_Edittext_Description : EditText
    private lateinit var mFirebaseAuth                        : FirebaseAuth

    override fun onCreateView(
        inflater           : LayoutInflater,
        container          : ViewGroup?,
        savedInstanceState : Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_drawing_upload_bottomsheet, container, false)
        init(view)
        return view
    }

    private fun init(v: View){
        //bind
        mDrawing_Upload_Imageview_Picture    = v.findViewById(R.id.drawing_upload_imageview_picture)
        mDrawing_Upload_Imageview_Close      = v.findViewById(R.id.drawing_upload_imageview_close)
        mDrawing_Upload_Button_Gallery       = v.findViewById(R.id.drawing_upload_button_gallery)
        mDrawing_Upload_Button_Shared        = v.findViewById(R.id.drawing_upload_button_shared)
        mDrawing_Upload_Button               = v.findViewById(R.id.drawing_upload_button)
        mDrawing_Upload_Edittext_Title       = v.findViewById(R.id.drawing_upload_edittext_title)
        mDrawing_Upload_Edittext_Description = v.findViewById(R.id.drawing_upload_edittext_description)

        mFirebaseAuth                        = FirebaseAuth.getInstance()

        mDrawing_Upload_Imageview_Picture.setImageBitmap(mPicture)

        onActivityFinish()
        saveImageToGallery()
        onShareImage()
        onHttpImageSend()

    }

    private fun onActivityFinish(){
        mDrawing_Upload_Imageview_Close.setOnClickListener {
            dismiss()
        }
    }

    private fun saveImageToGallery(){
        mDrawing_Upload_Button_Gallery.setOnClickListener {
            //?????? ??????
            if(!DM.getInstance().checkPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                !DM.getInstance().checkPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                return@setOnClickListener
            }

            //?????? ??????
            if(!DM.getInstance().imageExternalSave(activity!!, mPicture, activity!!.getString(R.string.app_name))){
                DM.getInstance().showToast(activity!!, activity!!.getString(R.string.picture_save_failed))
                return@setOnClickListener
            }

            DM.getInstance().showToast(activity!!, activity!!.getString(R.string.picture_save_success))
            dismiss()
        }
    }

    private fun onShareImage(){
        mDrawing_Upload_Button_Shared.setOnClickListener {
            DM.getInstance().onShareImage(activity!!, mPicture, getString(R.string.app_name))
        }
    }

    private fun onHttpImageSend(){
        mDrawing_Upload_Button.setOnClickListener {
            val pictureTitle       = mDrawing_Upload_Edittext_Title.text.toString()
            val pictureDescription = mDrawing_Upload_Edittext_Description.text.toString()

            if (pictureTitle.isBlank()
                ||pictureDescription.isBlank()) {
                DM.getInstance().showToast(activity!!, getString(R.string.blank))
                return@setOnClickListener
            }

            val file        = File(activity!!.filesDir, DM.getInstance().imageLocalSave(activity!!, mPicture, pictureTitle))
            val currentUser = mFirebaseAuth.currentUser
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val params      = ArrayList<MultipartBody.Part>()
            params.add(MultipartBody.Part.createFormData("picture", URLEncoder.encode(file.name), requestFile)) //???????????????
            params.add(MultipartBody.Part.createFormData("reqcmd", "picture_upload"))
            params.add(MultipartBody.Part.createFormData("picture_title", pictureTitle))
            params.add(MultipartBody.Part.createFormData("picture_description", pictureDescription))
            params.add(MultipartBody.Part.createFormData("picture_user", currentUser.uid))
            params.add(MultipartBody.Part.createFormData("picture_path", BuildConfig.BASE_PATH + currentUser.uid + "/"))//????????? ??????
            params.add(MultipartBody.Part.createFormData("picture_date", DM.getInstance().getNow()))

            //HTTP ??????
            DM.getInstance().HTTP_POST_CONNECT(activity!!, params, this::onHttpImageSendResult)
        }
    }

    private fun onHttpImageSendResult(response: Response<ResponseBody>) {
        try {
            val responseStringFromJson = response.body()!!.string() as String
            val jsonObject = JSONObject(responseStringFromJson)
            Log.d("response =>", jsonObject.toString())
            //???????????? ???????????? ?????? ????????? ???????????? ????????? ??????
            val isFile = jsonObject.getBoolean("isFile")
            if (isFile) {
                DM.getInstance().showToast(activity!!, activity!!.getString(R.string.drawing_upload_isfile))
                return
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        DM.getInstance().showToast(activity!!, activity!!.getString(R.string.picture_upload_success))
        dismiss()


    }




}