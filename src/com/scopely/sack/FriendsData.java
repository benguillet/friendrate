package com.scopely.sack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FriendsData extends SQLiteOpenHelper implements Constants {
	private final static String DATABASE_NAME = "sack.db";
	private final static int DATABASE_VERSION = 2;
	
	public FriendsData(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createFriendTable = "CREATE TABLE " + TABLE_NAME + " (" + _ID +
				" INTEGER PRIMARY KEY AUTOINCREMENT, " + FACEBOOK_ID + 
				" INTEGER NOT NULL, " + FIRST_NAME + " TEXT NOT NULL, " + LAST_NAME + 
				" TEXT NOT NULL, " + SCORE + " INTEGER DEFAULT NULL, " + CREATED_AT +
				" INTEGER NOT NULL, " + UPDATED_AT + " INTEGER NOT NULL, " + 
				"UNIQUE(" + FACEBOOK_ID + ") ON CONFLICT IGNORE);";
		db.execSQL(createFriendTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}
