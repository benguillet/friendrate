package com.benjaminguillet.friendrate;

import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.ProfilePictureView;


public class SelectionFragment extends Fragment implements Constants, OnClickListener {
	private static final int REAUTH_ACTIVITY_CODE = 100;
	
	private ProfilePictureView[] profilePictureFriends;
	private TextView userNameFriends[];
	private ProgressBar loadingCircle;
	private ProgressBar progressBar;
	private TextView progressText;
	
	private FriendsData friends;
	//private int[] randomFriends;
	private int[] lastRandomFriends;
	private ArrayList<Integer> alreadySelectedFriendsId;
	private long[] friendsFacebookID;
	private String[] friendsName;
	private int friendCount;
	private int friendsSelected;
	private int launchCounter;
	private boolean firstFragmentApparition;
		
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(final Session session, final SessionState state, final Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	private class SimpleGestureFilter extends SimpleOnGestureListener {
		public SimpleGestureFilter() {
			super();
		}
		
		@Override
	    public boolean onDown(MotionEvent e) {
	        return true;
	    }

	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
	        float velocityY) {
	        //Log.i(TAG, "onFling has been called!");
	        final int SWIPE_MIN_DISTANCE = 120;
	        final int SWIPE_MAX_OFF_PATH = 250;
	        final int SWIPE_THRESHOLD_VELOCITY = 200;
	        try {
	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
	                return false;
	            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
	                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	           	 	showNextFriends();
	                //Log.i(TAG, "Right to Left");
	            }
	            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
	                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	                //Log.i(TAG, "Left to Right");
	            }
	        }
	        catch (Exception e) {
	            // nothing
	        }
	        return super.onFling(e1, e2, velocityX, velocityY);
	    }

	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	    friends = new FriendsData(getActivity());
	    alreadySelectedFriendsId = new ArrayList<Integer>();
	    //randomFriends = new int[2];
	    profilePictureFriends = new ProfilePictureView[2];
	    userNameFriends = new TextView[2];
	    lastRandomFriends = new int[2];
	    friendsFacebookID = new long[2];
		friendsName = new String[2];
	    friendCount = 0;
	    friendsSelected = 0;
	    firstFragmentApparition = true;
	    
	    SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.saved_launch_counter),Context.MODE_PRIVATE);
        launchCounter = sharedPref.getInt(getString(R.string.saved_launch_counter), 0);
        ++launchCounter;
        //Log.i(TAG, "launchCounter: " + launchCounter);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.saved_launch_counter), launchCounter);
        editor.commit();
        
	    //Log.i(TAG, "in onCreate() of SelectionFragment");
	    setRetainInstance(true);
	    // TODO: if keep being launch, show a loading during the makeFriendsRequest! ie. Updating list of friends...
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.selection, container, false);
	    
	    final GestureDetector gesture = new GestureDetector(getActivity(), new SimpleGestureFilter());

	    view.setOnTouchListener(new View.OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            return gesture.onTouchEvent(event);
	        }
	    });
	    
	    profilePictureFriends[0] = (ProfilePictureView) view.findViewById(R.id.profile_pic_friend_1);
	    profilePictureFriends[1] = (ProfilePictureView) view.findViewById(R.id.profile_pic_friend_2);
	    profilePictureFriends[0].setCropped(true);
	    profilePictureFriends[1].setCropped(true);   
	    profilePictureFriends[0].setOnClickListener(this);
	    profilePictureFriends[1].setOnClickListener(this);
	    
	    userNameFriends[0] = (TextView) view.findViewById(R.id.user_name_friend_1);
	    userNameFriends[1] = (TextView) view.findViewById(R.id.user_name_friend_2);
	    loadingCircle = (ProgressBar) view.findViewById(R.id.loadingCircle);
	    progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	    progressBar.setProgress(0);
	    progressText = (TextView) view.findViewById(R.id.progressTextDynamic);
	    
	    // Check for an open session
	    Session session = Session.getActiveSession();
	  
	    if (timeToUpdate() || FriendsData.updated) {
		    if (session != null && session.isOpened()) {
		        // Get the user's friends
		        makeFriendsRequest(session);
		    }
	    }
	    else {
	    	if (firstFragmentApparition) {
	    		int[] randomFriends = pickTwoRandomFriends();
	    		displayFriends(randomFriends[0], randomFriends[1]);
	    		firstFragmentApparition = false;
	    	}
	    	profilePictureFriends[0].setVisibility(View.VISIBLE);
			profilePictureFriends[1].setVisibility(View.VISIBLE);
			userNameFriends[0].setVisibility(View.VISIBLE);
			userNameFriends[1].setVisibility(View.VISIBLE);
			displayFriends(lastRandomFriends[0], lastRandomFriends[1]);
		    
	    }
	    
	    return view;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == REAUTH_ACTIVITY_CODE) {
	        uiHelper.onActivityResult(requestCode, resultCode, data);
	    }
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
	    super.onSaveInstanceState(bundle);
	    uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		    case R.id.profile_pic_friend_1:
				incrementScore(lastRandomFriends[0]);
		        break;
		    case R.id.profile_pic_friend_2:
		       incrementScore(lastRandomFriends[1]);
		       break;
	    }   
		showNextFriends();
	}

	

	// TODO: move everything below to another class?
	private void makeFriendsRequest(final Session session) {
		// TODO: STOP BLOCKING UI THREAD!! Probably need to do the add friend and request into another thread!!!
		Toast.makeText(getActivity(), "Fetching Facebook friends.", Toast.LENGTH_SHORT).show();
		loadingCircle.setVisibility(View.VISIBLE);
		String friendsListQuery = "SELECT uid, first_name, last_name FROM user WHERE uid IN " +
	              	"(SELECT uid2 FROM friend WHERE uid1 = me() LIMIT 1000)";
        Bundle params = new Bundle();
        params.putString("q", friendsListQuery);
        Request request = new Request(session,
            "/fql",                         
            params,                         
            HttpMethod.GET,                 
            new Request.Callback() {       
                public void onCompleted(Response response) {
                    //Log.i(TAG, "Result: " + response.toString());
                    // TODO: refactor for taking care of the json somewhere else
                	try {
                    	GraphObject graphObject = response.getGraphObject();
                        if (graphObject != null) {
                            JSONObject jsonObject = graphObject.getInnerJSONObject();
                            try {
                            	JSONArray friends = jsonObject.getJSONArray("data");
                            	for (int i = 0; i < friends.length(); ++i) {
                            		JSONObject friend = (JSONObject) friends.get(i);
                            		// the uid can overflows a 32 bits integer
                            		long uid         = Long.parseLong(friend.get("uid").toString());
                            		String firstName = friend.get("first_name").toString();
                            		String lastName  = friend.get("last_name").toString();
                            		addFriend(uid, firstName, lastName);	
                            	}
                            	firstFragmentApparition = false;
                            	int[] randomFriends = pickTwoRandomFriends();
                        	    displayFriends(randomFriends[0], randomFriends[1]);
                        	    loadingCircle.setVisibility(View.GONE);
                            }
                            catch (JSONException e) {
                            	e.printStackTrace();
                            }
                        }
                        else {
                        	Log.i(TAG, "graphObject is null!");
                        }
                    }
                	finally {
                		friends.close();
                	}
                }                  
        }); 
        Request.executeBatchAsync(request);                 
    }

	
	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
		if (timeToUpdate()) { 
			if (session != null && session.isOpened()) {
		        makeFriendsRequest(session);
		    }
		}
	}
	
	private void addFriend(long uid, String firstName, String lastName) {
		SQLiteDatabase db = friends.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FACEBOOK_ID, uid);
		values.put(FIRST_NAME, firstName);
		values.put(LAST_NAME, lastName);
		int unixTime = (int) (System.currentTimeMillis() / 1000L);
		values.put(CREATED_AT, unixTime);
		values.put(UPDATED_AT, unixTime);
		try {
			db.insertOrThrow(TABLE_NAME, null, values);
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		profilePictureFriends[0].setVisibility(View.VISIBLE);
		profilePictureFriends[1].setVisibility(View.VISIBLE);
		userNameFriends[0].setVisibility(View.VISIBLE);
		userNameFriends[1].setVisibility(View.VISIBLE);
	}
	
	private int[] pickTwoRandomFriends() {
		Log.i(TAG, "before getFriendCount friendCount: " + this.friendCount);
		if (this.friendCount == 0) {
			this.friendCount = getFriendCount();
		}
		Log.i(TAG, "after getFriendCount friendCount: " + this.friendCount);

		
		Random randomGenerator = new Random();
		int[] randomFriends = new int[2];
		int counter = 0;
		while (counter < 2) {
			Integer randomFriend = Integer.valueOf(randomGenerator.nextInt(this.friendCount));
			if (!alreadySelectedFriendsId.contains(randomFriend)) {
				randomFriends[counter++] = randomFriend;	
			}
		}
		Log.i(TAG, "randomFriends[0] " + randomFriends[0]);
		Log.i(TAG, "randomFriends[1]: " + randomFriends[1]);
		
		return randomFriends;
	}
	
	private int getFriendCount() {
		int friendCount = 0;
		SQLiteDatabase db = friends.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
	    if (cursor != null) {
	        cursor.moveToFirst();
	        if (cursor.getInt(0) == 0) {
	        	Log.d(TAG, "DATABASE IS EMPTY");
	        }
	        else {
	        	friendCount = cursor.getInt(0);
	        }
	        cursor.close();
	    }
	    else {
	    	Log.d(TAG, "DATABASE DOESN'T EXIST!");
	    }
		return friendCount;
	}

	private void displayFriends(int friend1, int friend2) {
		// Execute the query only if new friend infos requested
		if ((lastRandomFriends[0] != friend1) || (lastRandomFriends[1] != friend2)) {
			SQLiteDatabase db = friends.getReadableDatabase();
			StringBuilder friendInfoQuery = new StringBuilder();
			friendInfoQuery.append("SELECT " + _ID + ", " + FACEBOOK_ID + ", " + FIRST_NAME + ", " + LAST_NAME);
			friendInfoQuery.append(" FROM " + TABLE_NAME);
			friendInfoQuery.append(" WHERE " + _ID + " IN (" + friend1 + ", " + friend2 + ")");
			
			Cursor cursor = db.rawQuery(friendInfoQuery.toString(), null);
			while (cursor.moveToNext()) {
				if (cursor.getInt(0) == friend1) {
					friendsFacebookID[0] = cursor.getLong(1);
					friendsName[0] = cursor.getString(2) + " " + cursor.getString(3);
				}
				else if (cursor.getInt(0) == friend2) {
					friendsFacebookID[1] = cursor.getLong(1);
					friendsName[1] = cursor.getString(2) + " " + cursor.getString(3);
				}
			}
			cursor.close();
			lastRandomFriends[0] = friend1;
			lastRandomFriends[1] = friend2;
		}
		
		profilePictureFriends[0].setProfileId(Long.toString(friendsFacebookID[0]));
		profilePictureFriends[1].setProfileId(Long.toString(friendsFacebookID[1]));
		
		userNameFriends[0].setText(friendsName[0]);
	    userNameFriends[1].setText(friendsName[1]);
	}
	
	private void incrementScore(int idFriend) {
		SQLiteDatabase db = friends.getWritableDatabase();
		// TODO: unit test to check if score incremented
		db.execSQL("UPDATE " + TABLE_NAME + " SET " + SCORE + " = " + SCORE + " + 1 " + "WHERE " + _ID + " = " + idFriend + ";");
		// TODO: unit test to check if friendSelected incremented
		alreadySelectedFriendsId.add(Integer.valueOf(idFriend));
		// TODO: no need of friendSelected
		++friendsSelected;
		progressBar.setProgress(friendsSelected);
		progressText.setText(Integer.toString(friendsSelected));
	}
	
	private void showNextFriends() {
		if (friendsSelected < 10) {
			int[] randomFriends = pickTwoRandomFriends();
			displayFriends(randomFriends[0], randomFriends[1]);
		}
		else {
			showFriendsRank();
		}
	}
	
	private void showFriendsRank() {
		Intent launchRankActivity = new Intent(getActivity(), RanksActivity.class);
		launchRankActivity.putIntegerArrayListExtra("alreadySelectedFriendsId", alreadySelectedFriendsId);
		getActivity().startActivity(launchRankActivity);
	}
	
    // return true only the very first time AND first fragment apparition) OR every deltaLaunch launches AND first fragment apparition
	private boolean timeToUpdate() {
		return (launchCounter == 1 || launchCounter % deltaLaunch ==  0) && firstFragmentApparition;
	}
	
	
}
