package com.scopely.sack;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RankFragment extends Fragment implements Constants {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.rank, container, false);
	    
	    TextView rank1 = (TextView) view.findViewById(R.id.rank1);
	    TextView rank2 = (TextView) view.findViewById(R.id.rank2);
	    
	    rank1.setText("1. Benjamin Guillet - 10pts");
	    // TODO: just need access to alreadySelectedFriendsId, and then get score of each, and sort and display!
	    return view;
	}
}
