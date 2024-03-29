package com.benjaminguillet.friendrate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class RanksActivity extends SherlockFragmentActivity implements Constants {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.ranks);	
	    
	    // setup action bar for tabs
	    ActionBar actionBar = getSupportActionBar();
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
	
	public static class RanksTabListener<T extends Fragment> implements ActionBar.TabListener {
	    private final FragmentActivity mActivity;
	    private final Class<T> mClass;
	    private Fragment mFragment;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public RanksTabListener(FragmentActivity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mClass = clz;
            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	    }


		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.ranks_container, mFragment);			
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(mFragment);			
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// Do nothing.		
		}
	}
}
