package com.scopely.sack;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class RanksActivity extends Activity implements Constants {
	private ArrayList<Integer> alreadySelectedFriendsId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ranks);
		Intent intent = getIntent();
		alreadySelectedFriendsId = intent.getIntegerArrayListExtra("alreadySelectedFriendsId");
		for (int i = 0; i < alreadySelectedFriendsId.size(); ++i) {
		    Log.d(TAG, "friend:"  + alreadySelectedFriendsId.get(i));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ranks, menu);
		return true;
	}

}
