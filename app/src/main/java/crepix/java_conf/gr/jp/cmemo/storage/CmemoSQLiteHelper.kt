package crepix.java_conf.gr.jp.cmemo.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

val DB = "sqlite.cmemo.db"
val DB_VERSION = 1
val CREATE_TABLE =
        """
        | create table memos(
        |    id integer primary key autoincrement,
        |    text text not null,
        |    icon integer not null,
        |    created_at integer not null,
        |    updated_at integer not null
        | );
        """.trimMargin()
val DROP_TABLE =
        """
        | drop table memos;
        """.trimMargin()

class CmemoSQLiteHelper(c: Context) : SQLiteOpenHelper(c, DB, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DROP_TABLE)
        onCreate(db)
    }
}