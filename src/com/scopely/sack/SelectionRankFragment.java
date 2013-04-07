package com.scopely.sack;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SelectionRankFragment extends Fragment implements Constants {
	private FriendsData friends;
	private TextView[] ranks;
	private ArrayList<Integer> alreadySelectedFriendsId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Log.i(TAG, "in onCreate() of SelectionRankFragment");
	    friends = new FriendsData(getActivity());
	    ranks = new TextView[10];
	    setRetainInstance(true);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.selection_rank, container, false);
	    Log.i(TAG, "in onCreateView() of SelectionRankFragment");
	    
	    //Intent intent = getActivity().getIntent();
		//this.alreadySelectedFriendsId = intent.getIntegerArrayListExtra("alreadySelectedFriendsId");
//		for (int i = 0; i < this.alreadySelectedFriendsId.size(); ++i) {
//		    Log.i(TAG, "friend:"  + this.alreadySelectedFriendsId.get(i));
//		}
	    
	    ranks[0] = (TextView) view.findViewById(R.id.rank1);
	    ranks[1] = (TextView) view.findViewById(R.id.rank2);
	    ranks[2] = (TextView) view.findViewById(R.id.rank3);
	    ranks[3] = (TextView) view.findViewById(R.id.rank4);
	    ranks[4] = (TextView) view.findViewById(R.id.rank5);
	    ranks[5] = (TextView) view.findViewById(R.id.rank6);
	    ranks[6] = (TextView) view.findViewById(R.id.rank7);
	    ranks[7] = (TextView) view.findViewById(R.id.rank8);
	    ranks[8] = (TextView) view.findViewById(R.id.rank9);
	    ranks[9] = (TextView) view.findViewById(R.id.rank10);
	    
	    SQLiteDatabase db = friends.getReadableDatabase();
	    StringBuilder selectionRankQuery = new StringBuilder();
	    selectionRankQuery.append("SELECT first_name, last_name, score FROM friends WHERE score IN (alreadySelectedFriendsId.get(0), alreadySelectedFriendsId.get(1)) ORDER BY score DESC;");
	    
	    
	    //"1. Benjamin Guillet -  10pts"
	    // TODO: create query
	    //Cursor cursor = db.rawQuery(selectionRankQuery.toString(), null);
	    //cursor.close();
	    		
	    return view;
	}
	
}
