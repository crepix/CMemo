package crepix.java_conf.gr.jp.cmemo.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import crepix.java_conf.gr.jp.cmemo.R

class BackgroundImageView : View {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)
    val mPaint = Paint()
    val mRes = this.context.resources!!
    var mBackground = BitmapFactory.decodeResource(mRes, R.drawable.backc1)!!
    var mMaxHeight = 0
    var isInit = false

    override fun onDraw(canvas: Canvas) {
        if (height > mMaxHeight && isInit) mMaxHeight = height
        isInit = true
        val scale = width / mBackground.width.toFloat()
        val he = mMaxHeight - mBackground.height.toFloat() * scale
        val back = Bitmap.createScaledBitmap(mBackground, width, (scale * mBackground.height).toInt(), false)
        canvas.drawBitmap(back, 0.toFloat(), he, mPaint)
    }

    fun updateBackground(resource: Int) {
        mBackground = BitmapFactory.decodeResource(mRes, resource)!!
        invalidate()
    }

}
