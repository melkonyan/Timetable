package com.timetable.app;


import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
/*
 * Class for working with event alarm.
 * It contains functional for creating and deleting alarms, receiving them, updating notification, that informs user, that alarm is set. 
 */
public class EventAlarmManager extends BroadcastReceiver {
	
	public  static final int ALARM_NOTIFICATION_CODE = 123;

	public static final String EXTRA_ALARM_ID_STRING = "alarm_id";
	
	private TimetableDatabase db = null; 
	
	private NotificationManager notificationManager;

	private AlarmManager alarmManager; 
	
	private Context context;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//Toast.makeText(context, "Test", Toast.LENGTH_SHORT).show();
		instantiate(context);
		Intent alarmDialogIntent = new Intent(context, EventAlarmDialogActivity.class);
		alarmDialogIntent.putExtra(EXTRA_ALARM_ID_STRING, intent.getExtras().getInt(EXTRA_ALARM_ID_STRING));
		alarmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		context.startActivity(alarmDialogIntent);
		TimetableLogger.log("Alarm received.");
	}
	
	public EventAlarmManager() {
		super();
	}
	
	public EventAlarmManager(Context context) {
		super();
		instantiate(context);
	}
	
	private void instantiate(Context context) {
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		this.context = context;
		this.db = new TimetableDatabase(context);
	}
	
	private PendingIntent getPendingIntentFromAlarm(EventAlarm alarm) {
		Intent intent = new Intent(context, EventAlarmManager.class);
		intent.putExtra(EXTRA_ALARM_ID_STRING, alarm.id);
		return PendingIntent.getBroadcast(context, alarm.id, intent, 0);
	}
	
	
	/*
	 * Create alarm with pending intent, that will be broadcasted to this class, when alarm should run.
	 */
	public void createAlarm(EventAlarm alarm) {
		alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.time.getTime(), getPendingIntentFromAlarm(alarm));
		if (isNotificationUpdateNeeded(alarm)) {
			createNotification();
		}
		TimetableLogger.log("Alarm successfully created.");
		
	}
	/*
	 * Delete alarm.
	 * Delete notification, if needed.
	 * Delete alarm from database.
	 */
	public void deleteAlarm(EventAlarm alarm) {
		alarmManager.cancel(getPendingIntentFromAlarm(alarm));
		if (isNotificationUpdateNeeded(alarm)) {
			deleteNotification();
		}
		db.deleteEventAlarm(alarm);
		
	}
	
	public void updateAlarm(EventAlarm alarm) {
		createAlarm(alarm);
	}
	
	public void checkAlarms() {
		Vector<EventAlarm> alarms = db.getAllAlarms();
		Date currentDate = Calendar.getInstance().getTime();
		for (EventAlarm alarm : alarms) {
			if (currentDate.after(alarm.time)) {
				deleteAlarm(alarm);
			} else {
				createAlarm(alarm);
			}
		}
	}
	
	/* 
	 * Return true if there is no event alarms in the database, or given alarm is the only one.
	 */
	boolean isNotificationUpdateNeeded(EventAlarm alarm) {
		return db.getEventAlarmCount() == 0 | alarm.equals(db.searchEventAlarmById(alarm.id));
	}
	
	/*
	 * Return notification manager. Create it if needed.
	 */
	private NotificationManager getNotificationManager() {
		if (notificationManager == null) {
			notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return notificationManager;
	}
	
	/*
	 * Create notification informing user, that there are some alarms set.
	 */
	public void createNotification() {
		PendingIntent mIntent = PendingIntent.getBroadcast(context, ALARM_NOTIFICATION_CODE, 
								new Intent(context, EventDayViewActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
		NotificationCompat.Builder mBuilder = new NotificationCompat
			.Builder(context)
			.setSmallIcon(R.drawable.ic_action_alarms)
			.setContentTitle("alarm title")
			.setContentText("alarmtext")
			.setContentIntent(mIntent);
		getNotificationManager().notify(ALARM_NOTIFICATION_CODE, mBuilder.build());
	}
	
	/*
	 * Delete notification.
	 */
	public void deleteNotification() {
		getNotificationManager().cancel(ALARM_NOTIFICATION_CODE);
	}

}

