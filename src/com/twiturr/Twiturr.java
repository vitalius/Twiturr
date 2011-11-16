package com.twiturr;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.OAuthSignpostClient;

import android.app.Application;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.content.Intent;
import android.content.ContentValues;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;

import android.util.Log;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.List;

public class Twiturr extends Application implements OnSharedPreferenceChangeListener {

    private static final String TAG = Twiturr.class.getSimpleName();
    private Twitter twitter;
    private StatusData statusData;
    private SharedPreferences prefs;
    private boolean serviceRunning;

    /*
     * Create application
     */
    @Override public void onCreate() {
	super.onCreate();
	this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	this.prefs.registerOnSharedPreferenceChangeListener(this);
	Log.i(TAG, "Created");

    }


    /*
     * Terminate
     */
    @Override public void onTerminate() {
	super.onTerminate();
	Log.i(TAG, "Terminated");
    }

    
    /*
     * Twitter generator
     */
    public synchronized Twitter getTwitter() {
	if (this.twitter == null) {
	    String username = this.prefs.getString("username", "");
	    String apiRoot = this.prefs.getString("apiRoot", "http://api.twitter.com");
	    String accessToken = this.prefs.getString(AuthActivity.ACCESS_TOKEN, "");
	    String accessSecret = this.prefs.getString(AuthActivity.ACCESS_SECRET, "");

	    OAuthSignpostClient client = new OAuthSignpostClient(AuthActivity.CONSUMER_KEY, 
								 AuthActivity.CONSUMER_SECRET, 
								 accessToken, 
								 accessSecret);

	    //Log.d(TAG, username+", "+apiRoot+", "+accessToken+", "+accessSecret);

	    if( !TextUtils.isEmpty(username) && !TextUtils.isEmpty(apiRoot) ) {
		this.twitter = new Twitter(username, client);
		this.twitter.setAPIRootUrl(apiRoot);
	    }
	    //List<Twitter.Status> statusUpdates = twitter.getFriendsTimeline();
	    //Log.d(TAG, "Updates: "+statusUpdates.size());
	}	
	return this.twitter;
    }


    /*
     * StatusData generator
     */
    public StatusData getStatusData() {
	if (statusData == null) {
	    statusData = new StatusData(this);
	}
	return statusData;
    }

    /*
     * Twitter updates.
     * Populate local database with new feeds and return the number of updates
     */
    public synchronized int fetchStatusUpdates() {
	Log.d(TAG, "Fetching status updates");
	Twitter twitter = getTwitter();
	if (twitter == null) {
	    Log.d(TAG, "Twitter connection not initialized");
	    return 0;
	}

	try {
	    //List<Twitter.Status> statusUpdates = twitter.getPublicTimeline();	    
	    List<Twitter.Status> statusUpdates = twitter.getFriendsTimeline();		     
	    long latestStatusCreatedAtTime = this.getStatusData().getLatestStatusCreatedAtTime();

	    int count = 0;
	    ContentValues values = new ContentValues();
	    for (Twitter.Status status : statusUpdates) {
		long createdAt = status.getCreatedAt().getTime();
		values.put(StatusData.C_ID, status.getId().toString());
		values.put(StatusData.C_CREATED_AT, status.createdAt.getTime());
		values.put(StatusData.C_SOURCE, status.source);
		values.put(StatusData.C_TEXT, status.text);
		values.put(StatusData.C_USER, status.user.name);
		//Log.d(TAG, "Update with id " + status.getId() + ". Saving.");
		this.getStatusData().insertOrIgnore(values);

		if (latestStatusCreatedAtTime < createdAt)
		    count++;
	    }
	    Log.d(TAG, count > 0 ? "Got " + count + " status updates" : "No new status updates");
	    return count;
	} catch (RuntimeException e) {
	    Log.e(TAG, "Failed to fetch status updates", e);
	    return 0;
	}
    }


    /*
     * Reset twitter object if preferences are changed
     */
    public synchronized void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String key) {
	this.twitter = null;
    }


    /*
     * getter for UpdaterService flag
     */
    public boolean isServiceRunning() {
	return serviceRunning;
    }

    /*
     * setter for UpdaterService flag
     */
    public void setServiceRunning(boolean isRunning) {
	serviceRunning = isRunning;
    }
}
	    