package com.homerours.musiccontrols;

import android.app.Service;
import android.os.IBinder;
import android.os.Binder;
import android.os.PowerManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.util.Log;

import static android.os.PowerManager.PARTIAL_WAKE_LOCK;

public class MusicControlsNotificationKiller extends Service {
    private static final String TAG = "MusicControlsNotificationKiller";

	private static int NOTIFICATION_ID;
	private NotificationManager mNM;
	private final IBinder mBinder = new KillBinder(this);

    // Partial wake lock to prevent the app from going to sleep when locked
    private PowerManager.WakeLock wakeLock;

	@Override
	public IBinder onBind(Intent intent) {
		this.NOTIFICATION_ID=intent.getIntExtra("notificationID",1);
		return mBinder;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

    /**
     * Put the service in a foreground state to prevent app from being killed
     * by the OS.
     */
    private void keepAwake() {
        PowerManager pm = (PowerManager)
                getSystemService(POWER_SERVICE);

        wakeLock = pm.newWakeLock(PARTIAL_WAKE_LOCK, TAG);

        Log.i(TAG, "Acquiring LOCK");
        wakeLock.acquire();
        if (wakeLock.isHeld()) {
            Log.i(TAG, "wakeLock acquired");
        } else {
            Log.e(TAG, "wakeLock not acquired yet");
        }
    }

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNM.cancel(NOTIFICATION_ID);
        keepAwake();
	}

    /**
     * Stop background mode.
     */
    private void sleepWell() {
        Log.i(TAG, "Stopping background mode");
//        getNotificationManager().cancel(NOTIFICATION_ID);

        if (wakeLock != null) {
            wakeLock.release();
            Log.i(TAG, "wakeLock released");
            wakeLock = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sleepWell();
    }

}
