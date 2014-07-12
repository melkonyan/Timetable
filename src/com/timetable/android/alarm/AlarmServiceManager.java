package com.timetable.android.alarm;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.timetable.android.TimetableLogger;

/*
 * Class for creating and binding AlarmService.
 */
public class AlarmServiceManager {

	
	final Object lock = new Object();
	private static Intent createServiceIntent(Context context) {
		return new Intent(context, AlarmService.class);
	}
	
	public static void startService(Context context) {
		context.startService(createServiceIntent(context));		
	}
	
	private Context context;
	private AlarmServiceConnection mConnection;
	 	
	public AlarmServiceManager(Context context) {
		this.context = context;
		this.mConnection = new AlarmServiceConnection();
	}
	
	public boolean bindService() {
		return context.bindService(createServiceIntent(context), mConnection, Context.BIND_AUTO_CREATE); 
	}

	public void unbindService() {
		try {
			context.unbindService(mConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	public AlarmService getService() {
		return mConnection.getService();
	}
	
	private class AlarmServiceConnection implements ServiceConnection {
		
		private AlarmService mService;
		
	    public void onServiceConnected(ComponentName className, 
	        IBinder binder) {
	    	TimetableLogger.error("Service Connected");
    		mService =  ((AlarmService.AlarmServiceBinder) binder).getService();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	    	TimetableLogger.error("Service Deisconnected");
	    	mService = null;
	    }
	    
	    public AlarmService getService() {
	    	return mService;
	    }
	 };

}
