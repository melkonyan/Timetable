package com.timetable.android.alarm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.timetable.android.TimetableLogger;
/*
 * Class for working with event alarm.
 * It contains functional for creating and deleting alarms, receiving them, updating notification, that informs user, that alarm is set. 
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			context.startService(new Intent(context, AlarmService.class));
			return;
		}
		Intent alarmDialogIntent = new Intent(context, EventAlarmDialogActivity.class);
		alarmDialogIntent.putExtra(AlarmService.EXTRA_EVENT_ID_STRING, intent.getExtras().getInt(AlarmService.EXTRA_EVENT_ID_STRING));
		alarmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		context.startActivity(alarmDialogIntent);
		TimetableLogger.log("Alarm received.");
	}
	
	public AlarmBroadcastReceiver() {
		super();
	}
	
	
	
	
}

