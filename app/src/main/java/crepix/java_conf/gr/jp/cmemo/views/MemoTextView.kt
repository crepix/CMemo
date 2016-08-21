package crepix.java_conf.gr.jp.cmemo.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import crepix.java_conf.gr.jp.cmemo.entities.Memo
import crepix.java_conf.gr.jp.cmemo.R
import java.util.*

class MemoTextView : TextView {
    val ANIMATION_DURATION = 500L
    val FAST_OUT_SLOW_IN_INTERPOLATOR = FastOutSlowInInterpolator()
    val mPaint = Paint()
    var mMemo = Memo(0, "", 0, Date(), Date())
    val DATE_PATTERN ="yyyy/MM/dd (E) HH:mm"
    var flickListener: (view: View) -> Unit = {view -> }

    inner class MyGestureListener(val view: MemoTextView) : GestureDetector.SimpleOnGestureListener() {
        override fun  onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val dx = Math.abs(velocityX)
            val dy = Math.abs(velocityY)
            if (dx > dy && dx > 400) {
                // 指の移動方向(左右)の判定
                if (event1.x < event2.x) {
                    ViewCompat.animate(view).alpha(0f)
                        .translationX(width.toFloat())
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                        override fun onAnimationEnd(view: View) {
                            flickListener(view)
                        }
                    }).start()
                } else {
                }
                return true
            }
            return false
        }
    }
    val mGestureListener = MyGestureListener(this)
    val mGestureDetector = GestureDetector(context, mGestureListener)

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)
    constructor(memo: Memo, ctx: Context) : this(ctx) {
        val iconNum = when(memo.icon % 4) {
            0 -> R.drawable.memo1
            1 -> R.drawable.memo2
            2 -> R.drawable.memo3
            3 -> R.drawable.memo4
            else -> R.drawable.memo1
        }
        this.setBackgroundResource(iconNum)
        this.text = memo.text
        this.textSize = 20.toFloat()
        mMemo = memo
        this.setTextColor(Color.rgb(51, 36, 26))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val sizeSource = if (width < rootView.height) width else rootView.height
        mPaint.textSize = sizeSource / 30f
        mPaint.color = Color.RED
        canvas.drawText(
                DateFormat.format(DATE_PATTERN, mMemo.createdAt).toString(),
                width - paddingRight / 2 - mPaint.measureText(DateFormat.format(DATE_PATTERN, mMemo.createdAt).toString()) - 30,
                sizeSource / 20f, mPaint)
        mPaint.color = Color.BLUE
        canvas.drawText(
                DateFormat.format(DATE_PATTERN, mMemo.updatedAt).toString(),
                width - paddingRight / 2 - mPaint.measureText(DateFormat.format(DATE_PATTERN, mMemo.updatedAt).toString())
                        - mPaint.measureText(DateFormat.format(DATE_PATTERN, mMemo.createdAt).toString()) - 60,
                sizeSource / 20f, mPaint)
    }

    fun setOnFlickListener(listener: (view: View) -> Unit) {
        flickListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mGestureDetector.onTouchEvent(event)) {
            return false
        } else {
            return super.onTouchEvent(event)
        }
    }
}