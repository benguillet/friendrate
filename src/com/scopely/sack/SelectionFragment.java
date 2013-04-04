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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
	private static final String TAG = "com.scopely.sack";
	
	private ProfilePictureView profilePictureFriend1;
	private ProfilePictureView profilePictureFriend2;
	private TextView userNameFriend1;
	private TextView userNameFriend2;
	
	private FriendsData friends;
	private int[] randomFriends;
	private ArrayList<Integer> tabuListFriendsId;
	private int friendCount;
	
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(final Session session, final SessionState state, final Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	    friends = new FriendsData(getActivity());
	    tabuListFriendsId = new ArrayList<Integer>();
	    randomFriends = new int[2];
	    friendCount = 0;
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
	    
	    profilePictureFriend1 = (ProfilePictureView) view.findViewById(R.id.profile_pic_friend_1);
	    profilePictureFriend2 = (ProfilePictureView) view.findViewById(R.id.profile_pic_friend_2);
	    profilePictureFriend1.setCropped(true);
	    profilePictureFriend2.setCropped(true);
	    
	    userNameFriend1 = (TextView) view.findViewById(R.id.user_name_friend_1);
	    userNameFriend2 = (TextView) view.findViewById(R.id.user_name_friend_2);
	   
	    
	    // Check for an open session
	    Session session = Session.getActiveSession();
	    if (session != null && session.isOpened()) {
	        // Get the user's friends
	        makeFriendsRequest(session);
	    }
        
	    pickTwoRandomFriends();
	    return view;
	}
	
	private void makeFriendsRequest(final Session session) {
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
	
	private void pickTwoRandomFriends() {
		if (friendCount == 0) {
			SQLiteDatabase db = friends.getReadableDatabase();
			Cursor fCount = db.rawQuery("SELECT COUNT(*) FROM "+ TABLE_NAME +";", null);
			fCount.moveToFirst();
			friendCount = fCount.getInt(0);
			fCount.close();
		}
		
		Random randomGenerator = new Random();
		int counter = 0;
		while(counter < 2) {
			Integer randomFriend = Integer.valueOf(randomGenerator.nextInt(friendCount));
			if (!tabuListFriendsId.contains(randomFriend)) {
				randomFriends[counter++] = randomFriend;	
			}
		}
		//Log.d(TAG, "friendCount: " + friendCount);
		//Log.d(TAG, "friend1: " + randomFriends[0]);
		//Log.d(TAG, "friend2: " + randomFriends[1]);
		
		displayFriends(randomFriends[0], randomFriends[1]);
	}

	private void displayFriends(int friend1, int friend2) {
		// TODO: prevent recall when screen orientation has changed:
		// http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
		SQLiteDatabase db = friends.getReadableDatabase();
		
		String[] FRIEND = {FACEBOOK_ID, FIRST_NAME, LAST_NAME};
		String WHERE    =  _ID + " = " + friend1 + " OR " + _ID + " = " + friend2;
		Cursor cursor = db.query(TABLE_NAME, FRIEND, WHERE, null, null, null, null);
		
		long[] friendsFacebookID = new long[2];
		String[] friendsName     = new String[2];
		int i = 0;
		while (cursor.moveToNext()) {
			friendsFacebookID[i] = cursor.getLong(0);
			friendsName[i] = cursor.getString(1) + " " + cursor.getString(2);
			++i;
		}
		cursor.close();
		
		for (int j = 0; j < 2; ++j) {
			Log.d(TAG, "id" + j + ": " + friendsFacebookID[j]);
			Log.d(TAG, "name" + j + ": " + friendsName[j]);
		}
		
		profilePictureFriend1.setProfileId(Long.toString(friendsFacebookID[0]));
		profilePictureFriend2.setProfileId(Long.toString(friendsFacebookID[1]));
		
		userNameFriend1.setText(friendsName[0]);
	    userNameFriend2.setText(friendsName[1]);
	}
}
