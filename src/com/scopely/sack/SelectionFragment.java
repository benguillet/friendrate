package com.scopely.sack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
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
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

public class SelectionFragment extends Fragment implements Constants {
	private ProfilePictureView profilePictureView;
	private TextView userNameView;
	private static final int REAUTH_ACTIVITY_CODE = 100;
	private static final String TAG = "com.scopely.sack";
	
	private FriendsData friends;
	
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
	    
	    profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);
	    profilePictureView.setCropped(true);
	    userNameView = (TextView) view.findViewById(R.id.selection_user_name);
	    
	    // Check for an open session
	    Session session = Session.getActiveSession();
	    if (session != null && session.isOpened()) {
	        // Get the user's data
	        makeMeRequest(session);
	        makeFriendsRequest(session);
	    }
	   
	    return view;
	}
	
	private void makeMeRequest(final Session session) {
	    // Make an API call to get user data and define a 
	    // new callback to handle the response.
	    Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (user != null) {
	                    // Set the id for the ProfilePictureView
	                    // view that in turn displays the profile picture.
	                    profilePictureView.setProfileId(user.getId());
	                    // Set the Textview's text to the user's name.
	                    userNameView.setText(user.getName());
	                }
	            }
	            if (response.getError() != null) {
	                // TODO:
	            }
	        }
	    });
	    request.executeAsync();
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
                    Log.i(TAG, "Result: " + response.toString());
                    try {
                    	GraphObject graphObject = response.getGraphObject();
                        if (graphObject != null) {
                            JSONObject jsonObject = graphObject.getInnerJSONObject();
                            try {
                            	JSONArray friends = jsonObject.getJSONArray("data");
                            	for (int i = 0; i < friends.length(); ++i) {
                            		JSONObject friend = (JSONObject) friends.get(i);
                            		// the uid can overflows a 32 bits integer
                            		long uid          = Long.parseLong(friend.get("uid").toString());
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
	        // Get the user's data.
	        makeMeRequest(session);
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
}
