package com.scopely.sack;

import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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

public class SelectionFragment extends Fragment implements Constants {
	private static final int REAUTH_ACTIVITY_CODE = 100;
	
	private ProfilePictureView[] profilePictureFriends;
	private TextView userNameFriends[];
	private ProgressBar loadingCircle;
	
	private FriendsData friends;
	private int[] randomFriends;
	private int[] lastRandomFriends;
	private ArrayList<Integer> alreadySelectedFriendsId;
	private long[] friendsFacebookID;
	private String[] friendsName;
	private int friendCount;
	private int friendsSelected;
	private boolean firstTime;
		
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
	        //Log.d(TAG, "onFling has been called!");
	        final int SWIPE_MIN_DISTANCE = 120;
	        final int SWIPE_MAX_OFF_PATH = 250;
	        final int SWIPE_THRESHOLD_VELOCITY = 200;
	        try {
	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
	                return false;
	            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
	                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	           	 	showNextFriends();
	                //Log.d(TAG, "Right to Left");
	            }
	            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
	                && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	                //Log.d(TAG, "Left to Right");
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
	    randomFriends = new int[2];
	    profilePictureFriends = new ProfilePictureView[2];
	    userNameFriends = new TextView[2];
	    lastRandomFriends = new int[2];
	    friendsFacebookID = new long[2];
		friendsName     = new String[2];
	    friendCount = 0;
	    friendsSelected = 0;
	    firstTime = true;
	    Log.d(TAG, "onCreate");
	    setRetainInstance(true);
	    // TODO: if keep being launch, show a loading during the makeFriendsRequest! ie. Updating list of friends...
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
	    
	    
	    for (int i = 0; i < profilePictureFriends.length; ++i) {
    		final int f = i;
	    	profilePictureFriends[i].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					incrementScore(f);
					showNextFriends();
				}
			});
	    }
	    
	    userNameFriends[0] = (TextView) view.findViewById(R.id.user_name_friend_1);
	    userNameFriends[1] = (TextView) view.findViewById(R.id.user_name_friend_2);
	    
	    loadingCircle = (ProgressBar) view.findViewById(R.id.loadingCircle);
	    
	    
	    
	    // Check for an open session
	    Session session = Session.getActiveSession();
	    if (firstTime) {
		    if (session != null && session.isOpened()) {
		        // Get the user's friends
		        makeFriendsRequest(session);
		    }
	    }
	    displayFriends(randomFriends[0], randomFriends[1]);
	    
	    return view;
	}


	// TODO: move everything below to another class?
	private void makeFriendsRequest(final Session session) {
		// TODO: STOP BLOCKING UI THREAD!! Probably need to do the add friend and request into another thread!!!
		Toast.makeText(getActivity(), "Fetching Facebook friends.", Toast.LENGTH_LONG).show();
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
                    //Log.d(TAG, "Result: " + response.toString());
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
                            	randomFriends = pickTwoRandomFriends();
                        	    displayFriends(randomFriends[0], randomFriends[1]);
                		    	firstTime = false;
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
	    if (session != null && session.isOpened()) {
	        makeFriendsRequest(session);
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
		db.insertOrThrow(TABLE_NAME, null, values);
	}
	
	private int[] pickTwoRandomFriends() {
		if (friendCount == 0) {
			SQLiteDatabase db = friends.getReadableDatabase();
			Cursor fCount = db.rawQuery("SELECT COUNT(*) FROM "+ TABLE_NAME +";", null);
			fCount.moveToFirst();
			friendCount = fCount.getInt(0);
			fCount.close();
		}
		
		Random randomGenerator = new Random();
		int[] randomFriends = new int[2];
		int counter = 0;
		while(counter < 2) {
			Integer randomFriend = Integer.valueOf(randomGenerator.nextInt(friendCount));
			if (!alreadySelectedFriendsId.contains(randomFriend)) {
				randomFriends[counter++] = randomFriend;	
			}
		}
		Log.d(TAG, "friendCount: " + friendCount);
		Log.d(TAG, "friend1: " + randomFriends[0]);
		Log.d(TAG, "friend2: " + randomFriends[1]);
		
		return randomFriends;
	}

	private void displayFriends(int friend1, int friend2) {
		// Make the query only if new info friends requested
		if (lastRandomFriends[0] != friend1 || lastRandomFriends[1] != friend2) {
			SQLiteDatabase db = friends.getReadableDatabase();
			
			String[] FRIEND = {FACEBOOK_ID, FIRST_NAME, LAST_NAME};
			String WHERE    =  _ID + " = " + friend1 + " OR " + _ID + " = " + friend2;
			Cursor cursor = db.query(TABLE_NAME, FRIEND, WHERE, null, null, null, null);
			
			int i = 0;
			while (cursor.moveToNext()) {
				friendsFacebookID[i] = cursor.getLong(0);
				friendsName[i] = cursor.getString(1) + " " + cursor.getString(2);
				++i;
			}
			cursor.close();
			lastRandomFriends[0] = friend1;
			lastRandomFriends[1] = friend2;
		}
		
		// TODO: Clean Debug
		//for (int j = 0; j < 2; ++j) {
		//	Log.d(TAG, "id" + j + ": " + friendsFacebookID[j]);
		//	Log.d(TAG, "name" + j + ": " + friendsName[j]);
		//}
		
		profilePictureFriends[0].setProfileId(Long.toString(friendsFacebookID[0]));
		profilePictureFriends[1].setProfileId(Long.toString(friendsFacebookID[1]));
		
		userNameFriends[0].setText(friendsName[0]);
	    userNameFriends[1].setText(friendsName[1]);
	}
	
	private void incrementScore(int friend) {
		int idFriend = randomFriends[friend];
		Log.d(TAG, "id friend touched: " + idFriend);
		SQLiteDatabase db = friends.getWritableDatabase();
		Cursor cursor = db.rawQuery("SELECT first_name, last_name FROM friends WHERE _id = " + idFriend + ";", null);
		cursor.moveToNext();
		Log.d(TAG, "friend touched: " + idFriend + " - " + cursor.getString(0) + " " + cursor.getString(1));
		cursor.close();
		// TODO: unit test to check if score incremented
		db.execSQL("UPDATE " + TABLE_NAME + " SET " + SCORE + " = " + SCORE + " + 1 " + "WHERE " + _ID + " = " + idFriend + ";");
		// TODO: unit test to check if friendSelected incremented
		// TODO: use tabuListFriend.length to check if more than 10? No more need of friendsSelected
		alreadySelectedFriendsId.add(Integer.valueOf(idFriend));
		++friendsSelected;
		// TODO: clean debug
		//Log.d(TAG, "friendsSelected: " + friendsSelected);
		
	}
	
	private void showNextFriends() {
		if (friendsSelected < 10) {
			randomFriends = pickTwoRandomFriends();
			displayFriends(randomFriends[0], randomFriends[1]);
		}
		else {
			showFriendsRank();
		}
	}
	
	private void showFriendsRank() {
		Intent myIntent = new Intent(getActivity(), RanksActivity.class);
		myIntent.putIntegerArrayListExtra("alreadySelectedFriendsId", alreadySelectedFriendsId);
		getActivity().startActivity(myIntent);
	}
	
	
}
