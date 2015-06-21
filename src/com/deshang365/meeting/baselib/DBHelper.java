package com.deshang365.meeting.baselib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public final static String DATABASE_NAME = "meeting";
	public final static String TABLE_NAME = "meetings";
	public final static int VERSION = 1;
	public final static String PRO_ID = "_id";
	public final static String PRO_HXID = "hxid";
	public final static String PRO_MOBILE = "mobile";
	public final static String PRO_GROUPNAME = "groupname";
	public final static String PRO_UID = "uid";
	static SQLiteDatabase sql;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
		sql = getWritableDatabase();
	}

	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		sql = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createSQLite = "create table " + TABLE_NAME + " ( " + PRO_ID + " INTEGER PRIMARY KEY autoincrement , " + PRO_HXID
				+ " nvarchar(20) , " + PRO_MOBILE + " nvarchar(20) , " + PRO_GROUPNAME + " nvarchar(20) , " + PRO_UID + " int ) ";
		db.execSQL(createSQLite);

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	public static boolean mInsertData(String table, ContentValues values) {
		return sql.insert(table, null, values) != -1;
	}

	public static boolean mUpdate(String table, ContentValues values, String whereClause, String[] whereArgs) {
		return sql.update(table, values, whereClause, whereArgs) > 0;
	}

	public static void mDeleteData(String table, String whereClause, String[] whereArgs) {
		sql.delete(table, whereClause, whereArgs);
	}

	public static List<Map<String, String>> mQuery(String table, String[] columns, String selection, String[] selectionArgs) {
		List<Map<String, String>> datas = new ArrayList<Map<String, String>>();
		Cursor cursor = sql.query(table, columns, selection, selectionArgs, null, null, null);
		Map<String, String> map = null;
		while (cursor.moveToNext()) {
			map = new HashMap<String, String>();
			map.put(DBHelper.PRO_MOBILE, cursor.getString(cursor.getColumnIndex(DBHelper.PRO_MOBILE)));
			map.put(DBHelper.PRO_UID, cursor.getString(cursor.getColumnIndex(DBHelper.PRO_UID)));
			datas.add(map);

		}
		return datas;
	}
}
