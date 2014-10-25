package com.timetable.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.timetable.android.alarm.AlarmService;

/*
 * Class, that receives broadcast, when device is restarted and starts all services.
 */
public class ServiceStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Logger.log("EventStarter.onReceive: action is received " + action);
		
		if (BroadcastActions.ACTION_APP_STARTED.equals(action) || Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			context.startService(new Intent(context, DeviceMuteService.class));
			context.startService(new Intent(context, AlarmService.class));
			context.startService(new Intent(context, EventService.class));
		}
	}
	
	
}
