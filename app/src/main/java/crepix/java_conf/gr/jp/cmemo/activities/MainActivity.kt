package crepix.java_conf.gr.jp.cmemo.activities

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import crepix.java_conf.gr.jp.cmemo.R
import crepix.java_conf.gr.jp.cmemo.entities.Memo
import crepix.java_conf.gr.jp.cmemo.repositories.MemoRepository
import crepix.java_conf.gr.jp.cmemo.views.BackgroundImageView
import crepix.java_conf.gr.jp.cmemo.views.MemoTextView
import crepix.java_conf.gr.jp.cmemo.views.MyScrollView

class MainActivity : AppCompatActivity(), TextWatcher {

    var repository = MemoRepository(this)
    val originList: MutableList<Memo> = mutableListOf()
    var sortedList: MutableList<Memo> = mutableListOf()
    val MAX_LENGTH = 500
    val MAX_LOAD = 20

    var order = 0
    var beforeText = ""
    var visibleViews = 0
    var firstScroll = false
    var listHeight = 0
    var introShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        toolbar!!.title = ""
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)!!
        order = sharedPref.getInt("order", 0)
        val icon = when(order) {
            0 -> R.drawable.ar_down_red
            1 -> R.drawable.ar_up_red
            2 -> R.drawable.ar_down_blue
            3 -> R.drawable.ar_up_blue
            else -> R.drawable.ar_down_red
        }
        toolbar.setNavigationIcon(icon)
        toolbar.setNavigationOnClickListener { view ->
            order = (order + 1) % 4
            val editor = sharedPref.edit()
            editor.putInt("order", order)
            editor.apply()
            val i = when(order) {
                0 -> R.drawable.ar_down_red
                1 -> R.drawable.ar_up_red
                2 -> R.drawable.ar_down_blue
                3 -> R.drawable.ar_up_blue
                else -> R.drawable.ar_down_red
            }
            toolbar.setNavigationIcon(i)
            memoListChange()
        }
        val editText = findViewById(R.id.editText) as EditText
        editText.addTextChangedListener(this)

        val fab = findViewById(R.id.fab) as FloatingActionButton?
        fab!!.setOnClickListener { view ->
            if (originList.size >= MAX_LENGTH) {
                Toast.makeText(this, "メモ数が上限です。メモを捨ててください", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, WriteActivity::class.java)
                startActivity(intent)
            }
        }

        val scrollView = findViewById(R.id.scrollView) as MyScrollView
        scrollView.setOnScrollListener { view, y ->
            if (!firstScroll) {
                firstScroll = true
                val container = findViewById(R.id.memo_container) as LinearLayout?
                listHeight = container!!.height
            }
            if (view.height + y == listHeight) {
                addViewWithScroll()
            }
        }

        val back = sharedPref.getInt("back", 0)
        val backNum = when(back) {
            0 -> R.drawable.tile_background
            1 -> R.drawable.back_grad
            else -> R.drawable.tile_background
        }
        val backCNum = when(back) {
            0 -> R.drawable.backc1
            1 -> R.drawable.backc3
            else -> R.drawable.backc1
        }
        val background = findViewById(R.id.background) as android.support.design.widget.CoordinatorLayout
        background.setBackgroundResource(backNum)
        val backC = findViewById(R.id.back) as BackgroundImageView
        backC.updateBackground(backCNum)
    }

    override fun onResume() {
        super.onResume()
        repository = MemoRepository(this)
        originList.clear()
        originList.addAll(repository.find())
        memoListChange()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_text_up) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)!!
            val textSize = sharedPref.getInt("textSize", 4)
            if (textSize < 10) {
                val editor = sharedPref.edit()
                editor.putInt("textSize", textSize + 1)
                editor.apply()
                val container = findViewById(R.id.memo_container) as LinearLayout?
                if (container!!.childCount !== 0) {
                    for (i in 0..container!!.childCount - 1) {
                        if (i != container.childCount - 1 || visibleViews != sortedList.size)
                            (container.getChildAt(i) as TextView).textSize = (textSize + 1).toFloat() * 5
                        firstScroll = false
                    }
                }
            }

            return true
        }
        if (id == R.id.action_text_down) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)!!
            val textSize = sharedPref.getInt("textSize", 4)
            if (textSize > 1) {
                val editor = sharedPref.edit()
                editor.putInt("textSize", textSize - 1)
                editor.apply()
                val container = findViewById(R.id.memo_container) as LinearLayout?
                if (container!!.childCount !== 0) {
                    for (i in 0..container!!.childCount - 1) {
                        if (i != container.childCount - 1 || visibleViews != sortedList.size)
                            (container.getChildAt(i) as TextView).textSize = (textSize - 1).toFloat() * 5
                        firstScroll = false
                    }
                }
            }
            return true
        }
        if (id == R.id.action_back_change) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)!!
            val back = sharedPref.getInt("back", 0)
            val editor = sharedPref.edit()
            editor.putInt("back", (back + 1) % 2)
            editor.apply()
            val backNum = when((back + 1) % 2) {
                0 -> R.drawable.tile_background
                1 -> R.drawable.back_grad
                else -> R.drawable.tile_background
            }
            val backCNum = when((back + 1) % 2) {
                0 -> R.drawable.backc1
                1 -> R.drawable.backc3
                else -> R.drawable.backc1
            }
            val background = findViewById(R.id.background) as android.support.design.widget.CoordinatorLayout
            background.setBackgroundResource(backNum)
            val backC = findViewById(R.id.back) as BackgroundImageView
            backC.updateBackground(backCNum)
            return true
        }
        if (id == R.id.action_introduction) {
            if (!introShowing) {
                val frame = findViewById(R.id.frameLayout) as FrameLayout
                val inflater = LayoutInflater.from(this)
                val introduction = inflater.inflate(R.layout.introduction, null)
                introduction.setOnClickListener { view ->
                    frame.removeView(view)
                    introShowing = false
                }
                frame.addView(introduction)
                introShowing = true
            }
            return true
        }
        if (id == R.id.action_all_clear) {
            val alert = AlertDialog.Builder(this)
            alert.setTitle("注意")
            alert.setMessage("本当に全てのメモを消しますか？")
            alert.setPositiveButton("はい") { dialog, which ->
                repository.allDelete()
                originList.clear()
                memoListChange()
            }
            alert.setNegativeButton("いいえ") { dialog, which ->
            }
            alert.show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Nothing to do
    }

    override fun afterTextChanged(s: Editable?) {
        val text = if (s!!.length > 1) {
            s.toString()
        } else {
            ""
        }
        if (beforeText != text) {
            beforeText = text
            memoListChange()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Nothing to do
    }

    private fun memoListChange() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)!!
        val textSize = sharedPref.getInt("textSize", 4)
        visibleViews = 0
        firstScroll = false
        val filteredList = originList.filter { memo -> beforeText == "" || memo.text.contains(beforeText) }
        sortedList = when(order) {
            0 -> filteredList.sortedByDescending { memo -> memo.createdAt!!.time }
            1 -> filteredList.sortedBy { memo -> memo.createdAt!!.time }
            2 -> filteredList.sortedByDescending { memo -> memo.updatedAt!!.time }
            3 -> filteredList.sortedBy { memo -> memo.updatedAt!!.time }
            else -> filteredList.sortedBy { memo -> memo.createdAt!!.time }
        }.toMutableList()
        val slicedList = sortedList.slice(0..if(filteredList.size < MAX_LOAD) filteredList.size - 1 else MAX_LOAD - 1)
        visibleViews = slicedList.size
        val container = findViewById(R.id.memo_container) as LinearLayout?
        container!!.removeAllViewsInLayout()
        slicedList.forEach {
            val view = MemoTextView(it, this)
            view.textSize = (textSize * 5).toFloat()
            view.setOnFlickListener { view ->
                flickEvent(view, it, container)
            }

            view.setOnClickListener { view ->
                val intent = Intent(this, WriteActivity::class.java)
                intent.putExtra("id", it.id)
                startActivity(intent)
            }
            container.addView(view)
        }
        if(sortedList.size == visibleViews) {
            addBottomInfo(container)
        }
    }

    private fun addViewWithScroll() {
        if (visibleViews < sortedList.size) {
            val nowMax = if(sortedList.size - visibleViews < MAX_LOAD) sortedList.size - 1 else visibleViews + MAX_LOAD - 1
            val slicedList = sortedList.slice(visibleViews..nowMax)
            visibleViews = nowMax + 1
            val container = findViewById(R.id.memo_container) as LinearLayout?
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)!!
            val textSize = sharedPref.getInt("textSize", 4)
            slicedList.forEach {
                val view = MemoTextView(it, this)
                view.setOnFlickListener { view ->
                    flickEvent(view, it, container!!)
                }

                view.setOnClickListener { view ->
                    val intent = Intent(this, WriteActivity::class.java)
                    intent.putExtra("id", it.id)
                    startActivity(intent)
                }
                view.textSize = textSize.toFloat() * 5
                container!!.addView(view)
            }
            if(sortedList.size == visibleViews) {
                addBottomInfo(container!!)
            }
            firstScroll = false
        }
    }

    private fun addBottomInfo(container: LinearLayout) {
        val dustInformation = TextView(this)
        dustInformation.setBackgroundResource(R.drawable.memo_dust)
        dustInformation.setTextColor(Color.GRAY)
        dustInformation.text = "メモは右にフリックすることで捨てることができます"
        container.addView(dustInformation)
    }

    private fun flickEvent(view: View, memo: Memo, container: LinearLayout) {
        container.removeView(view)
        originList.remove(memo)
        sortedList.remove(memo)
        if(sortedList.size >= visibleViews) {
            val m = sortedList[visibleViews - 1]
            val v = MemoTextView(m, this)
            v.setOnClickListener { view ->
                val intent = Intent(this, WriteActivity::class.java)
                intent.putExtra("id", m.id)
                startActivity(intent)
            }
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)!!
            val textSize = sharedPref.getInt("textSize", 4)
            v.textSize = textSize.toFloat() * 5
            container.addView(v)
            listHeight = container.height
            firstScroll = false
            if (sortedList.size == visibleViews) addBottomInfo(container)
            v.setOnFlickListener { vi ->
                flickEvent(vi, m, container)
            }
        } else {
            visibleViews -= 1
        }
        if (memo.id != null) repository.delete(memo.id)
    }

}
