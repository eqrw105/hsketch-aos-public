package com.nims.hsketch

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.properties.Delegates

class DrawView: View {
    constructor(context: Context) : super(context){
        penInit()
        init()
    }
    
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        penInit()
        init()
    }
    
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        penInit()
        init()
    }

    private val mAlphaUnit          = 2.25f

    private val mPenSizeInitailize  = resources.getInteger(R.integer.draw_stroke_size_min).toFloat()
    private val mPenAlphaInitailize = (resources.getInteger(R.integer.draw_stroke_ahlpa_max) * mAlphaUnit).toInt()
    private val mPenColorInitailize = Color.BLACK

    private var mPenColor           = 0
    private var mPenSize            = resources.getInteger(R.integer.draw_stroke_size_min).toFloat()
    private var mPenAlpha           = (resources.getInteger(R.integer.draw_stroke_ahlpa_max) * mAlphaUnit).toInt()

    private val mUndoList           = ArrayList<Path>()
    private val mUndoPaintList      = ArrayList<Paint>()
    private val mRedoList           = ArrayList<Path>()
    private val mRedoPaintList      = ArrayList<Paint>()

    private val mTouchTolerance     = 4
    private var mX by Delegates.notNull<Float>()
    private var mY by Delegates.notNull<Float>()
    
    private lateinit var mDrawPath       : Path
    private lateinit var mDrawPaint      : Paint
    private lateinit var mDrawCanvas     : Canvas
    private lateinit var mCanvasBitmap   : Bitmap
    
    private fun penInit(){
        this.mPenSize  = this.mPenSizeInitailize
        this.mPenAlpha = this.mPenAlphaInitailize
        this.mPenColor = this.mPenColorInitailize
    }

    //prepare drawing
    private fun init() {
        mDrawPath              = Path()
        mDrawPaint             = Paint()
        mDrawPaint.color       = mPenColor
        mDrawPaint.isAntiAlias = true
        mDrawPaint.strokeWidth = mPenSize
        mDrawPaint.style       = Paint.Style.STROKE
        mDrawPaint.strokeJoin  = Paint.Join.ROUND
        mDrawPaint.strokeCap   = Paint.Cap.ROUND
        mDrawPaint.alpha       = mPenAlpha
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mDrawCanvas   = Canvas(mCanvasBitmap)
        mDrawCanvas.drawColor(Color.WHITE)
    }

    open fun setColor(color: Int) {
        this.mPenColor   = color
        mDrawPaint.color = this.mPenColor
        mDrawPaint.alpha = this.mPenAlpha
    }

    open fun getColor(): Int {
        return this.mPenColor
    }

    open fun setPenAlpha(alpha: Int?) {
        this.mPenAlpha   = (alpha!! * mAlphaUnit).toInt()
        mDrawPaint.alpha = this.mPenAlpha
    }

    open fun getPenAlpha(): Int {
        return this.mPenAlpha
    }

    open fun setSize(size: Float) {
        this.mPenSize          = size
        mDrawPaint.strokeWidth = this.mPenSize
    }

    open fun getSize(): Float {
        return this.mPenSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for  (i in 0 until mUndoList.size) {
            canvas.drawPath(mUndoList[i], mUndoPaintList[i])
        }
        canvas.drawPath(mDrawPath, mDrawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN ->{
                mRedoList     .clear()
                mRedoPaintList.clear()
                mDrawPath     .reset()
                mDrawPath     .moveTo(x, y)
                mX = x
                mY = y
            }
            MotionEvent.ACTION_MOVE ->{
                val dx = abs(x - mX)
                val dy = abs(y - mY)
                if (dx >= mTouchTolerance || dy >= mTouchTolerance) {
                    mDrawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                    mX = x
                    mY = y
                }
            }
            MotionEvent.ACTION_UP ->{
                mDrawPath     .lineTo(mX, mY)
                mDrawCanvas   .drawPath(mDrawPath, mDrawPaint)
                mUndoList     .add(mDrawPath)
                mUndoPaintList.add(mDrawPaint)
                init()
            }
        }
        invalidate()
        return true

    }

    open fun getCanvasBitmap(): Bitmap {
        val canvas = Canvas(mCanvasBitmap)
        draw(canvas)
        return mCanvasBitmap
    }


    open fun onUndo() {
        if (mUndoList.isEmpty()) return
        mRedoList     .add(mUndoList.removeAt(mUndoList.size - 1))
        mRedoPaintList.add(mUndoPaintList.removeAt(mUndoPaintList.size - 1))
        invalidate()
    }

    open fun onRedo() {
        if (mRedoList.isEmpty()) return
        mUndoList     .add(mRedoList.removeAt(mRedoList.size - 1))
        mUndoPaintList.add(mRedoPaintList.removeAt(mRedoPaintList.size - 1))
        invalidate()
    }

    open fun onReset() {
        mUndoList     .clear()
        mRedoList     .clear()
        mUndoPaintList.clear()
        mRedoPaintList.clear()
        onSizeChanged(width, height, width, height)
        penInit()
        init()
        invalidate()
    }


}