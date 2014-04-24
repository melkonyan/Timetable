package com.timetable.app;

import org.holoeverywhere.widget.Toast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EventAlarmManager extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "Test", Toast.LENGTH_SHORT).show();
		TimetableLogger.log("Alarm received.");
	}
	
	private AlarmManager alarmManager; 
	
	private Context context;
	
	public EventAlarmManager(Context context) {
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		this.context = context;
	}
	
	public void createAlarm(EventAlarm alarm) throws AlarmCreationErrorException {
		Intent intent = new Intent(context, EventAlarmManager.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.time.getTime(), sender);
	}
	
	public class AlarmCreationErrorException extends Exception {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7753790028855593557L;

		public AlarmCreationErrorException(String message) {
			super(message);
		}
	}
}

