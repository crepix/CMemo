package crepix.java_conf.gr.jp.cmemo.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import crepix.java_conf.gr.jp.cmemo.R

class MyScrollView : ScrollView {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)
    var scrollListener: (view: View, y: Int) -> Unit = { view, y -> }
    fun setOnScrollListener(listener: (view: View, y: Int) -> Unit) {
        scrollListener = listener
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        scrollListener(this, t)
        super.onScrollChanged(l, t, oldl, oldt)
    }
}
