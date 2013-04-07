package com.scopely.sack;

import static com.scopely.sack.Constants.TAG;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;

public class RanksActivity extends FragmentActivity implements Constants {
	
	public static class RanksTabListener<T extends Fragment> implements ActionBar.TabListener {
		private Fragment mFragment;
	    private final FragmentActivity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;
	    private FragmentTransaction fft;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public RanksTabListener(FragmentActivity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	    	fft = mActivity.getSupportFragmentManager().beginTransaction();
	    }

		@Override
		public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
			// Check if the fragment is already initialized
	        if (mFragment == null) {
	            // If not, instantiate and add it to the activity
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	            fft.add(android.R.id.content, mFragment, mTag);
	        } else {
	            // If it exists, simply attach it in order to show it
	            fft.attach(mFragment);
	        }
			
		}
		
		@Override
		public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
			// Do nothing.		
		}


		@Override
		public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
			if (mFragment != null) {
	            // Detach the fragment, because another one is being attached
	            fft.detach(mFragment);
	        }
			
		}
	}
	
	private ArrayList<Integer> alreadySelectedFriendsId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // Notice that setContentView() is not used, because we use the root
	    // android.R.id.content as the container for each fragment
	    
	    Intent intent = getIntent();
		alreadySelectedFriendsId = intent.getIntegerArrayListExtra("alreadySelectedFriendsId");
		for (int i = 0; i < alreadySelectedFriendsId.size(); ++i) {
		    Log.d(TAG, "friend:"  + alreadySelectedFriendsId.get(i));
		}
	    
	    // setup action bar for tabs
	    ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(false);
	    actionBar.setDisplayHomeAsUpEnabled(true);

	    Tab tab = actionBar.newTab()
	            .setText(R.string.selection_rank)
	            .setTabListener(new RanksTabListener<SelectionRankFragment>(
	                    this, "Selection Rank", SelectionRankFragment.class));
	    actionBar.addTab(tab);

	    tab = actionBar.newTab()
	        .setText(R.string.general_rank)
	        .setTabListener(new RanksTabListener<GeneralRankFragment>(
	                this, "General Rank", GeneralRankFragment.class));
	    actionBar.addTab(tab);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // This is called when the Home (Up) button is pressed
	            // in the Action Bar.
	            Intent parentActivityIntent = new Intent(this, MainActivity.class);
	            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(parentActivityIntent);
	            return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
