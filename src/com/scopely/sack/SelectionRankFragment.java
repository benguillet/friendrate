package com.scopely.sack;

import java.util.ArrayList;

import android.content.Intent;
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
	    friends = new FriendsData(getActivity());
	    ranks = new TextView[10];
	    setRetainInstance(true);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.selection_rank, container, false);
	    
	    Intent intent = getActivity().getIntent();	    
		this.alreadySelectedFriendsId = intent.getIntegerArrayListExtra("alreadySelectedFriendsId");

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
	    
	    displayRank();
	    		
	    return view;
	}
	
	private void displayRank() {
	    SQLiteDatabase db = friends.getReadableDatabase();
	    StringBuilder selectionRankQuery = new StringBuilder();
	    selectionRankQuery.append("SELECT ");
	    selectionRankQuery.append(FIRST_NAME);
	    selectionRankQuery.append(", ");
	    selectionRankQuery.append(LAST_NAME);
	    selectionRankQuery.append(", ");
	    selectionRankQuery.append(SCORE);
	    selectionRankQuery.append(" FROM ");
	    selectionRankQuery.append(TABLE_NAME);
	    selectionRankQuery.append(" WHERE ");
	    selectionRankQuery.append(_ID);
	    selectionRankQuery.append(" IN (");
	    for (int i = 0; i < this.alreadySelectedFriendsId.size(); ++i) {
	    	selectionRankQuery.append(alreadySelectedFriendsId.get(i));
	    	if (i < 9) {
	    		selectionRankQuery.append(", ");
	    	}
	    }
		selectionRankQuery.append(") ORDER BY ");
		selectionRankQuery.append(SCORE);
		selectionRankQuery.append(" DESC;");
	    
	    Cursor cursor = db.rawQuery(selectionRankQuery.toString(), null);
	    int  i = 0;
	    String friendInfo = "";
	    while (cursor.moveToNext()) {
	    	friendInfo = (i + 1) + ". " + cursor.getString(0) + " " + cursor.getString(1) + " -  " + cursor.getInt(2) + "pts";
	    	ranks[i++].setText(friendInfo);
		}
	    cursor.close();
	}
	
}
