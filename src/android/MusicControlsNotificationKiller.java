package com.homerours.musiccontrols;

import java.lang.ref.WeakReference;

import android.app.Service;
import android.os.IBinder;
import android.os.Binder;
import android.os.PowerManager;
import android.app.NotificationManager;
import android.app.Notification;
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

    private WeakReference<Notification> notification;


	@Override
	public IBinder onBind(Intent intent) {
		this.NOTIFICATION_ID=intent.getIntExtra("notificationID",1);
		return mBinder;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

    public void setNotification(Notification n) {
        Log.i(TAG, "setNotification");
        if (notification != null) {
            sleepWell();
            notification = null;
        }
        if (n != null) {
            notification = new WeakReference<Notification>(n);
            keepAwake();
        }
    }

    /**
     * Put the service in a foreground state to prevent app from being killed
     * by the OS.
     */
    private void keepAwake() {
        if (notification != null) {
            startForeground(this.NOTIFICATION_ID, notification.get());
        }

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

    /**
     * Shared manager for the notification service.
     */
    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

	@Override
	public void onCreate() {
		/*mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNM.cancel(NOTIFICATION_ID);*/
        keepAwake();
	}

    /**
     * Stop background mode.
     */
    private void sleepWell() {
        Log.i(TAG, "Stopping WakeLock");
        stopForeground(true);
        getNotificationManager().cancel(NOTIFICATION_ID);

        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
                Log.i(TAG, "wakeLock released");
            } else {
                Log.i(TAG, "wakeLock not held");
            }
            wakeLock = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sleepWell();
    }

}
