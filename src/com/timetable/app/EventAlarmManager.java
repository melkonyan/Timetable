package com.timetable.app;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class EventAlarmManager extends BroadcastReceiver {
	
	private static final int NOTIFICATION_REQUEST_CODE = 123;

	@Override
	public void onReceive(Context context, Intent intent) {
		//Toast.makeText(context, "Test", Toast.LENGTH_SHORT).show();
		Intent alarmDialogIntent = new Intent(context, EventAlarmDialogActivity.class);
		alarmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		context.startActivity(alarmDialogIntent);
		TimetableLogger.log("Alarm received.");
	}
	
	private AlarmManager alarmManager; 
	
	private Context context;
	
	public EventAlarmManager() {
		super();
	}
	
	public EventAlarmManager(Context context) {
		super();
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		this.context = context;
	}
	
	private PendingIntent getPendingIntentFromAlarm(EventAlarm alarm) {
		Intent intent = new Intent(context, EventAlarmManager.class);
		return PendingIntent.getBroadcast(context, alarm.id, intent, 0);
	}
	
	public void createAlarm(EventAlarm alarm) {
		
		alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.time.getTime(), getPendingIntentFromAlarm(alarm));
		updateNotification();
		TimetableLogger.log("Alarm successfully created.");
		
	}

	
	public void deleteAlarm(EventAlarm alarm) {
		alarmManager.cancel(getPendingIntentFromAlarm(alarm));
	}
	
	public void updateAlarm(EventAlarm alarm) {
		if (alarm != null) {
			createAlarm(alarm);
		} else {
			deleteAlarm(alarm);
		}
	}
	
	private NotificationManager getNotificationManager() {
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void updateNotification() {
		PendingIntent mIntent = PendingIntent.getBroadcast(context, NOTIFICATION_REQUEST_CODE, 
								new Intent(context, EventDayViewActivity.class), 0);
		NotificationCompat.Builder mBuilder = new NotificationCompat
			.Builder(context)
			.setSmallIcon(R.drawable.ic_action_alarms)
			.setContentTitle("alarm title")
			.setContentText("alarmtext")
			.setContentIntent(mIntent);
		NotificationManager mManager = getNotificationManager();
		mManager.notify(NOTIFICATION_REQUEST_CODE, mBuilder.build());
	}
	
	public void deleteNotification() {
		getNotificationManager().cancel(NOTIFICATION_REQUEST_CODE);
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

