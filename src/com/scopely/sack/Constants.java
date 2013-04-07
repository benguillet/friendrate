package com.scopely.sack;

import android.provider.BaseColumns;

public interface Constants extends BaseColumns {
	public static final String TAG = "com.scopely.sack";
	
	public static final int SPLASH         = 0;
	public static final int SELECTION      = 1;
	public static final int SETTINGS 	   = 2;
	public static final int FRAGMENT_COUNT = SETTINGS + 1;
	
	public static final int deltaLaunch    = 100;

	public static final String TABLE_NAME  = "friends";
	
	public static final String FACEBOOK_ID = "facebook_id";
	public static final String FIRST_NAME  = "first_name";
	public static final String LAST_NAME   = "last_name";
	public static final String SCORE       = "score";
	public static final String CREATED_AT  = "created_at";
	public static final String UPDATED_AT  = "updated_at";
	
}
