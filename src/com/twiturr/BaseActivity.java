package com.twiturr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends Activity {

    Twiturr turr;

    @Override protected void onCreate(Bundle elephant_nostrils) {
	super.onCreate(elephant_nostrils);
	turr = (Twiturr)getApplication();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
	getMenuInflater().inflate(R.menu.menu, menu);
	return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
	switch(item.getItemId()) {
	case R.id.itemPrefs:         /* Preferences */
	    startActivity(new Intent(this, PrefsActivity.class)
			  .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	    break;
	case R.id.itemToggleService: /* Timeline updater service */
	    if (turr.isServiceRunning())
		stopService(new Intent(this, UpdaterService.class));
	    else
		startService(new Intent(this, UpdaterService.class));
	    break;
	case R.id.itemTimeline:      /* Timeline activity */
	    startActivity(new Intent(this, TimelineActivity.class)
			  .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			  .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	    break;
	case R.id.itemStatus:        /* Status activity */
	    startActivity(new Intent(this, StatusActivity.class)
			  .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	    break;
	case R.id.itemPurge:         /* Purging timeline database */
	    turr.getStatusData().delete();
	    Toast.makeText(this, R.string.msgAllDataPurged, Toast.LENGTH_LONG).show();
	    break;
	}
	return true;
    } 

    @Override public boolean onMenuOpened(int featureId, Menu menu) {
	MenuItem toggleItem = menu.findItem(R.id.itemToggleService);
	if (turr.isServiceRunning()) {
	    toggleItem.setTitle(R.string.titleServiceStop);
	    toggleItem.setIcon(android.R.drawable.ic_media_pause);
	} else {
	    toggleItem.setTitle(R.string.titleServiceStart);
	    toggleItem.setIcon(android.R.drawable.ic_media_play);
	}
	return true;
    }
}