package crepix.java_conf.gr.jp.cmemo.repositories

import android.content.ContentValues
import android.content.Context
import crepix.java_conf.gr.jp.cmemo.entities.Memo
import crepix.java_conf.gr.jp.cmemo.storage.CmemoSQLiteHelper
import java.util.*

class MemoRepository(c: Context) {
    val helper = CmemoSQLiteHelper(c)
    val queryArray = arrayOf("id", "text", "icon", "created_at", "updated_at")

    fun resolve(id: Long): Memo? {
        val db = helper.readableDatabase
        val c = db.query("memos", queryArray, "id = ?", arrayOf(id.toString()), null, null, null)
        if (c.moveToFirst()) {
            val memo = Memo(c.getLong(0), c.getString(1), c.getInt(2), Date(c.getLong(3)), Date(c.getLong(4)))
            c.close()
            db.close()
            return memo
        } else {
            c.close()
            db.close()
            return null
        }
    }
    fun find(): MutableList<Memo> {
        val db = helper.readableDatabase
        val c = db.query("memos", queryArray, null, null, null, null, null)
        var isEof = c.moveToFirst()
        val memos: MutableList<Memo> = mutableListOf()
        while(isEof) {
            memos.add(Memo(c.getLong(0), c.getString(1), c.getInt(2), Date(c.getLong(3)), Date(c.getLong(4))))
            isEof = c.moveToNext()
        }
        c.close()
        db.close()
        return memos
    }
    fun store(memo: Memo): Memo? {
        val db = helper.writableDatabase
        val values = ContentValues()
        values.put("text", memo.text)
        values.put("icon", memo.icon)
        values.put("created_at", System.currentTimeMillis())
        values.put("updated_at", System.currentTimeMillis())
        val id = db.insert("memos", null, values)
        db.close()
        return this.resolve(id)
    }
    fun update(memo: Memo): Memo? {
        val db = helper.writableDatabase
        val values = ContentValues()
        val whereArgs = arrayOf(memo.id.toString())
        values.put("text", memo.text)
        values.put("icon", memo.icon)
        values.put("updated_at", System.currentTimeMillis())
        db.update("memos", values, "id = ?", whereArgs)
        db.close()
        return this.resolve(memo.id!!)
    }
    fun delete(id: Long): Unit {
        val db = helper.writableDatabase
        db.delete("memos", "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun allDelete(): Unit {
        val db = helper.writableDatabase
        db.delete("memos", null, null)
        db.close()
    }
}