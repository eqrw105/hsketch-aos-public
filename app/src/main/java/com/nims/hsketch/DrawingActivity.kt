package com.nims.hsketch

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener

class DrawingActivity : AppCompatActivity(), ColorPickerDialogListener {
    private lateinit var mDrawView          : DrawView

    //펜 크기 뷰 그룹
    private lateinit var mSeekbar_Pensize   : SeekBar
    private lateinit var mTextview_Pensize  : TextView
    private lateinit var mImageview_Pensize : ImageView
    private lateinit var mCardviewPensize   : CardView

    //펜 투명도 뷰 그룹
    private lateinit var mSeekbar_Penalpha  : SeekBar
    private lateinit var mTextview_Penalpha : TextView
    private lateinit var mImageview_Penalpha: ImageView
    private lateinit var mCardviewPenalpha  : CardView

    //메뉴 항목
    private lateinit var mImageview_Pencolor: ImageView
    private lateinit var mImageview_Done    : ImageView
    private lateinit var mImageview_Undo    : ImageView
    private lateinit var mImageview_Redo    : ImageView
    private lateinit var mImageview_Restart : ImageView


    private lateinit var mImageview_Back    : ImageView

    private lateinit var mCardView_Draw     : CardView
    private lateinit var mCardView_Menu     : CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)
        init()
    }

    private fun init(){
        //bind
        //펜 크기 뷰 그룹
        mSeekbar_Pensize       = findViewById(R.id.drawing_seekbar_pensize)
        mTextview_Pensize      = findViewById(R.id.drawing_textview_pensize)
        mCardviewPensize       = findViewById(R.id.drawing_cardview_pensize)
        mImageview_Pensize     = findViewById(R.id.drawing_imageview_pensize)

        //펜 투명도 뷰 그룹
        mSeekbar_Penalpha      = findViewById(R.id.drawing_seekbar_penalpha)
        mTextview_Penalpha     = findViewById(R.id.drawing_textview_penalpha)
        mImageview_Penalpha    = findViewById(R.id.drawing_imageview_penalpha)
        mCardviewPenalpha      = findViewById(R.id.drawing_cardview_penalpha)

        //메뉴 항목
        mImageview_Pencolor    = findViewById(R.id.drawing_imageview_pencolor)
        mImageview_Done        = findViewById(R.id.drawing_imageview_done)
        mImageview_Undo        = findViewById(R.id.drawing_imageview_undo)
        mImageview_Redo        = findViewById(R.id.drawing_imageview_redo)
        mImageview_Restart     = findViewById(R.id.drawing_imageview_restart)

        mCardView_Draw         = findViewById(R.id.drawing_cardview_draw)

        mCardView_Menu         = findViewById(R.id.drawing_cardview_menu)
        mImageview_Back        = findViewById(R.id.drawing_imageview_back)
        mDrawView              = findViewById(R.id.drawing_drawview)

        drawViewInit()
        setPensize()
        setPenColor()
        setPenAlpha()
        onComplete()
        onRedo()
        onReset()
        onUndo()
        onActivityFinish()

    }

    private fun drawViewInit(){
        mTextview_Pensize.text     = mDrawView.getSize().toInt().toString() + getString(R.string.drawing_pensize_unit)
        mSeekbar_Pensize.progress  = mDrawView.getSize().toInt()
        mTextview_Penalpha.text    = mDrawView.getPenAlpha().toString() + getString(R.string.drawing_penalpha_unit)
        mSeekbar_Penalpha.progress = mDrawView.getPenAlpha()

        mImageview_Pencolor.setColorFilter(mDrawView.getColor())
    }

    private fun setPensize(){
        //펜사이즈 변경하는 이미지 버튼 클릭하면 게이지화면 껐다켜기
        mImageview_Pensize.setOnClickListener {
            when(mCardviewPensize.visibility){
                View.GONE -> {
                    mCardviewPensize.visibility  = View.VISIBLE
                    mCardviewPenalpha.visibility = View.GONE
                }
                View.VISIBLE -> {
                    mCardviewPensize.visibility  = View.GONE
                }
            }
        }

        //펜사이즈 게이지 변경 시
        mSeekbar_Pensize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                mTextview_Pensize.text = p1.toString() + getString(R.string.drawing_pensize_unit)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) { }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                mDrawView.setSize(p0!!.progress.toFloat())
            }

        })
    }

    private fun setPenColor(){
        mImageview_Pencolor.setOnClickListener {
            ColorPickerDialog.newBuilder().setColor(mDrawView.getColor()).show(this)

        }
    }

    private fun onComplete(){
        mImageview_Done.setOnClickListener {
            val picture                  = mDrawView.getCanvasBitmap()
            val drawingUploadBottomSheet = BottomSheetDrawingUpload(picture)

            drawingUploadBottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
            drawingUploadBottomSheet.show(supportFragmentManager, "")
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        mDrawView          .setColor(color)
        mImageview_Pencolor.setColorFilter(mDrawView.getColor())
    }

    override fun onDialogDismissed(dialogId: Int) {

    }

    private fun onRedo(){
        mImageview_Redo.setOnClickListener {
            mDrawView.onRedo()
        }
    }

    private fun onUndo(){
        mImageview_Undo.setOnClickListener {
            mDrawView.onUndo()
        }
    }

    private fun onReset(){
        mImageview_Restart.setOnClickListener {
            mDrawView.onReset()
            drawViewInit()
        }
    }

    private fun setPenAlpha(){
        //펜 투명도 변경 이미지 버튼 누르면 게이지화면 껐다켜기
        mImageview_Penalpha.setOnClickListener {
                when(mCardviewPenalpha.visibility){
                    View.GONE -> {
                        mCardviewPenalpha.visibility = View.VISIBLE
                        mCardviewPensize.visibility  = View.GONE
                    }
                    View.VISIBLE -> {
                        mCardviewPenalpha.visibility = View.GONE
                    }
                }
            }

        //펜 투명도 변경 시
        mSeekbar_Penalpha.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                mTextview_Penalpha.text = p1.toString() + getString(R.string.drawing_penalpha_unit)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) { }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                mDrawView.setPenAlpha(p0!!.progress)
            }

        })
    }

    private fun onActivityFinish(){
        mImageview_Back.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        DM.getInstance().finishActivity(this)
    }
}



