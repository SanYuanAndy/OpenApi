package com.openapi.comm.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper {
    private SQLiteDatabase mDB = null;

    public static interface IQueryCallBack {
        void onQuery(Cursor cursor);
    }

    private SQLiteHelper () {

    }

    public SQLiteHelper (String path, boolean onlyRead) throws Exception {
        mDB = open(path, onlyRead);
        if (mDB == null) {
            throw new Exception("open db error");
        }
    }

    public List<String> getTableList() {
        List<String> tables = new ArrayList<>();
        Cursor cursor = mDB.rawQuery("select name from sqlite_master where type = 'table'", null);
        query(cursor, 0, tables);
        cursor.close();
        return tables;
    }

    public List<String> getHead(String table) {
        List<String> head = new ArrayList<>();
        String sql = String.format("PRAGMA table_info(%s)", table);
        Cursor cursor = mDB.rawQuery(sql, null);
        query(cursor, 1, head);
        cursor.close();
        return head;
    }

    public void getTableData(String table, IQueryCallBack callBack) {
        String sql = String.format("SELECT * from %s", table);
        Cursor cursor = mDB.rawQuery(sql, null);
        query(cursor, callBack);
        cursor.close();
    }

    public void release() {
        SQLiteDatabase db = mDB;
        mDB = null;
        if (db != null) {
            try {
                db.close();
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    public static void query(Cursor cursor, final int col, final List<String> out) {
        query(cursor, new IQueryCallBack() {
            @Override
            public void onQuery(Cursor cursor) {
                out.add(cursor.getString(col));
            }
        });
    }

    public static void query(Cursor cursor, IQueryCallBack callBack) {
        cursor.moveToFirst();
        do {
            if (callBack != null) {
                callBack.onQuery(cursor);
            }
        } while (cursor.moveToNext());
        cursor.close();
    }

    public static SQLiteDatabase open(String path, boolean onlyReady) {
        File dbFile = new File(path);
        if (!dbFile.exists() || !dbFile.canRead()) {
            return null;
        }
        SQLiteDatabase db = null;
        if (onlyReady) {
            db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } else {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        }
        return db;
    }
}
