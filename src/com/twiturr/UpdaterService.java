package com.twiturr;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {
    static final String TAG = UpdaterService.class.getSimpleName(); 

    static final int DELAY = 15000; // 1000 = 1 sec
    private boolean runFlag = false;
    private Updater updater;
    private Twiturr turr;

    public static final String NEW_STATUS_FILTER = "com.twiturr.NEW_STATUS";
    public static final String NEW_STATUS_COUNT = "com.twiturr.NEW_STATUS_COUNT";

    @Override public IBinder onBind(Intent intent) {
	return null;
    }

    @Override public void onCreate() {
	super.onCreate();

	turr = (Twiturr) getApplication();
	updater = new Updater();

	Log.d(TAG, "onCreate");
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
	super.onStartCommand(intent, flags, startId);

	runFlag = true;
	turr.setServiceRunning(true);
	updater.start();

	Log.d(TAG, "onStarted");
	return START_STICKY;
    }

    @Override public void onDestroy() {
	super.onDestroy();

	runFlag = false;
	turr.setServiceRunning(false);
	updater.interrupt();
	updater = null;

	Log.d(TAG, "onDestroyed");
    }


    public class Updater extends Thread {
	static final String RECEIVE_TIMELINE = "com.twiturr.RECEIVE_TIMELINE_NOTIFICATIONS";
	public Updater() {
	    super("Updater-Thread");
	}

	@Override public void run() {
	    while (UpdaterService.this.runFlag) {
		Log.d(TAG, "Running background thread.");
		try {
		    Twiturr turr = (Twiturr) getApplication();
		    int count = turr.fetchStatusUpdates();
		    if (count > 0) {
			Log.d(TAG, "We have a new status");
			Intent intent = new Intent(NEW_STATUS_FILTER);
			intent.putExtra(NEW_STATUS_COUNT, count);
			UpdaterService.this.sendBroadcast(intent, RECEIVE_TIMELINE);
		    }
		    Thread.sleep(DELAY);
		} catch (InterruptedException e) {
		    UpdaterService.this.runFlag = false;
		}
	    }
	}
    }

}
