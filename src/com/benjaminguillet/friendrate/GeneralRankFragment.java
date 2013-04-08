package com.benjaminguillet.friendrate;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GeneralRankFragment extends Fragment implements Constants {
	private FriendsData friends;
	private TextView[] ranks;
	
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
	    
	    displayGeneralRank();
	    		
	    return view;
	}
	
	private void displayGeneralRank() {
	    SQLiteDatabase db = friends.getReadableDatabase();
	    StringBuilder topFriendsQuery = new StringBuilder();
	    topFriendsQuery.append("SELECT ");
	    topFriendsQuery.append(FIRST_NAME);
	    topFriendsQuery.append(", ");
	    topFriendsQuery.append(LAST_NAME);
	    topFriendsQuery.append(", ");
	    topFriendsQuery.append(SCORE);
	    topFriendsQuery.append(" FROM ");
	    topFriendsQuery.append(TABLE_NAME);
	    topFriendsQuery.append(" ORDER BY ");
	    topFriendsQuery.append(SCORE);
	    topFriendsQuery.append(" DESC LIMIT 10;");
	    
	    Cursor cursor = db.rawQuery(topFriendsQuery.toString(), null);
	    int  i = 0;
	    String friendInfo = "";
	    while (cursor.moveToNext()) {
	    	friendInfo = (i + 1) + ". " + cursor.getString(0) + " " + cursor.getString(1) + " -  " + cursor.getInt(2) + "pts";
	    	ranks[i++].setText(friendInfo);
		}
	    cursor.close();
	}
	
}
