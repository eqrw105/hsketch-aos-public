package com.nims.hsketch

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

open class SettingItemView : ConstraintLayout {
    constructor(context: Context) : super(context){
        init(context, null)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        init(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init(context, attrs)
    }

    private lateinit var mLayout_Settingitemview_Textview_Title : TextView
    private lateinit var mLayout_Settingitemview_Imageview      : ImageView

    private fun init(context: Context, attrs: AttributeSet?){
        val v = View.inflate(context, R.layout.layout_settingitemview, this)
        mLayout_Settingitemview_Textview_Title = v.findViewById(R.id.layout_settingitemview_textview_title)
        mLayout_Settingitemview_Imageview      = v.findViewById(R.id.layout_settingitemview_imageview)
        val typeArray = context.theme.obtainStyledAttributes(attrs, R.styleable.settingItemViewData, 0, 0)

        try {
            setTitleText(typeArray.getString(R.styleable.settingItemViewData_itemTitleText))
            setDrawable(typeArray.getDrawable(R.styleable.settingItemViewData_itemDrawable))
        } finally {
            typeArray.recycle()
        }
    }



    open fun setTitleText(text: String?){
        mLayout_Settingitemview_Textview_Title.text = text
        onRefresh()
    }

    open fun setDrawable(drawable: Drawable?){
        mLayout_Settingitemview_Imageview.setImageDrawable(drawable)
        onRefresh()
    }

    private fun onRefresh(){
        invalidate()
        requestLayout()
    }

}