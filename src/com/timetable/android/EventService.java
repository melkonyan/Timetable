package com.timetable.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class EventService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	private EventService() {
		
	}
		
}
