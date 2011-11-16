package com.twiturr;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class TimelineActivity extends BaseActivity {
    static final String TAG = "TimelineActivity";
    static final String SEND_TIMELINE = "com.twiturr.SEND_TIMELINE_NOTIFICATIONS";

    ListView listTimeline;
    Cursor cursor;
    TimelineAdapter adapter;
    SharedPreferences prefs;
    TimelineReceiver receiver;
    IntentFilter filter;

    @Override protected void onCreate(Bundle b) {
	super.onCreate(b);
	setContentView(R.layout.timeline);
	listTimeline = (ListView) findViewById(R.id.listTimeline);

	receiver = new TimelineReceiver();
	filter = new IntentFilter(UpdaterService.NEW_STATUS_FILTER);

	prefs = PreferenceManager.getDefaultSharedPreferences(this);
	
	if (prefs.getString("username", null) == null) {
	    startActivity(new Intent(this, PrefsActivity.class));
	    Toast.makeText(this, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show();
	}
    }

    @Override protected void onResume() {
	super.onResume();

	setupList();
	registerReceiver(receiver, filter, SEND_TIMELINE, null);

	Log.d(TAG, "onResume()");
    }
    
    @Override protected void onPause() {
	super.onPause();
	unregisterReceiver(receiver);
	cursor.close();
	Log.d(TAG, "onPause()");
    }

    @Override protected void onDestroy() {
	super.onDestroy();
	cursor.close();
	turr.getStatusData().close();
    }

    public void setupList() {
	cursor = turr.getStatusData().getStatusUpdates();
	adapter = new TimelineAdapter(this, cursor);
	listTimeline.setAdapter(adapter);
    }

    class TimelineReceiver extends BroadcastReceiver {
	@Override public void onReceive(Context context, Intent intent) {
	    Log.d("TimelineReceiver", "onReceive");
	    TimelineActivity.this.setupList();
	    TimelineActivity.this.adapter.notifyDataSetChanged();
	}
    }
}