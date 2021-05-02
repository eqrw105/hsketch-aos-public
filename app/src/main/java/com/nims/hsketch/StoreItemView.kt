package com.nims.hsketch

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

open class StoreItemView : ConstraintLayout {
    constructor(context: Context) : super(context){
        init(context, null)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        init(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init(context, attrs)
    }

    private lateinit var mLayout_Storeitemview_Textview_Title       : TextView
    private lateinit var mLayout_Storeitemview_Textview_Price       : TextView
    private lateinit var mLayout_Storeitemview_Textview_Description : TextView

    private fun init(context: Context, attrs: AttributeSet?){
        val v = View.inflate(context, R.layout.layout_storeitemview, this)
        mLayout_Storeitemview_Textview_Title       = v.findViewById(R.id.layout_storeitemview_textview_title)
        mLayout_Storeitemview_Textview_Price       = v.findViewById(R.id.layout_storeitemview_textview_price)
        mLayout_Storeitemview_Textview_Description = v.findViewById(R.id.layout_storeitemview_textview_description)
        val typeArray = context.theme.obtainStyledAttributes(attrs, R.styleable.settingItemViewData, 0, 0)

        try {
            setTitleText      (typeArray.getString(R.styleable.storeItemViewData_storeTitle))
            setPriceText      (typeArray.getString(R.styleable.storeItemViewData_storePrice))
            setDescriptionText(typeArray.getString(R.styleable.storeItemViewData_storeDescription))
        } finally {
            typeArray.recycle()
        }
    }



    open fun setTitleText(text: String?){
        mLayout_Storeitemview_Textview_Title.text = text
        onRefresh()
    }

    open fun setPriceText(text: String?){
        mLayout_Storeitemview_Textview_Price.text = text
        onRefresh()
    }

    open fun setDescriptionText(text: String?){
        mLayout_Storeitemview_Textview_Description.text = text
        onRefresh()
    }

    private fun onRefresh(){
        invalidate()
        requestLayout()
    }

}