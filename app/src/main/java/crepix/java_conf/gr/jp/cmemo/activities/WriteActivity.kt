package crepix.java_conf.gr.jp.cmemo.activities

import android.app.Notification
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.speech.RecognizerIntent
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.format.DateFormat
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import crepix.java_conf.gr.jp.cmemo.R
import crepix.java_conf.gr.jp.cmemo.entities.Memo
import crepix.java_conf.gr.jp.cmemo.repositories.MemoRepository
import crepix.java_conf.gr.jp.cmemo.views.BackgroundWriteView
import java.util.*

class WriteActivity : AppCompatActivity() {

    var repository = MemoRepository(this)
    var memo = Memo(null, "", 0, null, null)
    val DATE_PATTERN ="yyyy/MM/dd (E) HH:mm"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setNavigationOnClickListener { view ->
            save()
            finish()
        }
        val delete = findViewById(R.id.delete_box) as ImageView?
        delete!!.setOnClickListener { view ->
            if (memo.id != null) {
                repository.delete(memo.id!!)
            }
            finish()
        }
        val mike = findViewById(R.id.mike_box) as ImageView?
        mike!!.setOnClickListener { view ->
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "マイクに向けて\n喋ってください")
            startActivityForResult(intent, 0)
        }
        val notification = findViewById(R.id.notification_box) as ImageView?
        notification!!.setOnClickListener { view ->
            val textView = findViewById(R.id.text_area) as EditText?

            val taskBuilder = TaskStackBuilder.create(this)
            val writeIntent = Intent(this, WriteActivity::class.java)
            if (memo.id != null) writeIntent.putExtra("id", memo.id!!)
            writeIntent.putExtra("text", memo.text)
            taskBuilder.addNextIntent(Intent(this, MainActivity::class.java))
            taskBuilder.addNextIntent(writeIntent)
            val contentIntent = PendingIntent.getActivities(
                    applicationContext,
                    Random(System.currentTimeMillis()).nextInt(),
                    taskBuilder.intents,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            val builder = Notification.Builder(applicationContext)
            val icon = when(memo.icon) {
                0 -> R.drawable.notification_icon
                1 -> R.drawable.notification_icon2
                2 -> R.drawable.notification_icon3
                3 -> R.drawable.notification_icon4
                else -> R.drawable.notification_icon
            }
            builder.setSmallIcon(icon)
                    .setContentIntent(contentIntent)
                    .setContentTitle("CMemo")
                    .setContentText(textView!!.text.toString())
                    .setSubText("ピンチインで全文表示")
                    .setWhen(System.currentTimeMillis())
            val no = Notification.BigTextStyle(builder).bigText(textView.text.toString()).build()
            val manager = NotificationManagerCompat.from(applicationContext)
            manager.notify(Random(System.currentTimeMillis()).nextInt(), no)
        }
        repository = MemoRepository(this)
        val id = intent.getLongExtra("id", -1)
        val createdView = findViewById(R.id.text_created) as TextView?
        val updatedView = findViewById(R.id.text_updated) as TextView?
        if (id != -1L) {
            val m = repository.resolve(id)
            if (m != null) {
                memo = m
                createdView!!.text = createdView.text.toString() + DateFormat.format(DATE_PATTERN, m.createdAt).toString()
                updatedView!!.text = updatedView.text.toString() + DateFormat.format(DATE_PATTERN, m.updatedAt).toString()
            }
        } else {
            createdView!!.text = ""
            updatedView!!.text = ""
            createdView.background = null
            updatedView.background = null
            memo = Memo(null, intent.getStringExtra("text") ?: "", Random(System.currentTimeMillis()).nextInt(4), null, null)
        }
        val textView = findViewById(R.id.text_area) as TextView?
        val icon = when(memo.icon) {
            0 -> R.drawable.memo1
            1 -> R.drawable.memo2
            2 -> R.drawable.memo3
            3 -> R.drawable.memo4
            else -> R.drawable.memo1
        }
        textView!!.setBackgroundResource(icon)
        textView.text = memo.text
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)!!
        val textSize = sharedPref.getInt("textSize", 4)
        textView.textSize = textSize.toFloat() * 5
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        val back = sharedPref.getInt("back", 0)
        val backNum = when(back) {
            0 -> R.drawable.tile_background
            1 -> R.drawable.back_grad
            else -> R.drawable.tile_background
        }
        val backCNum = when(back) {
            0 -> R.drawable.backc2
            1 -> R.drawable.backc4
            else -> R.drawable.backc2
        }
        val background = findViewById(R.id.background) as android.support.design.widget.CoordinatorLayout
        background.setBackgroundResource(backNum)
        val backC = findViewById(R.id.back) as BackgroundWriteView
        backC.updateBackground(backCNum)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            save()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            // 結果文字列リスト
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val textView = findViewById(R.id.text_area) as TextView?
            val text = textView!!.text
            textView.text = text.toString() + results[0]
        }
    }

    private fun save() {
        val textView = findViewById(R.id.text_area) as EditText?
        val m = Memo(memo.id, textView!!.text.toString(), memo.icon, memo.createdAt, memo.updatedAt)
        if (m.text == "") {
            if (m.id != null) {
                repository.delete(m.id)
            }
        } else {
            if (m.id == null) {
                repository.store(m)
            } else if(m.text != memo.text) {
                repository.update(m)
            }
        }
    }
}
