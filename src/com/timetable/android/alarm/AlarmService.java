package com.timetable.android.alarm;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Vector;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.timetable.android.Event;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.utils.TimetableUtils;
import com.timetable.android.R;

public class AlarmService extends Service {

	public  static final int ALARM_NOTIFICATION_CODE = 123;

	public static final String EXTRA_EVENT_ID_STRING = "event_id"; 

	public static final int MAX_QUEUE_SIZE = 10000;
	
	private static final String NEXT_ALARM_NOTIFICATION_PREFIX = "Next alarm is on: ";
	
	public static final SimpleDateFormat alarmTimeFormat = new SimpleDateFormat("EEE, d. MMM yyyy 'at' HH:mm", Locale.US);
	
	private final AlarmServiceBinder mBinder = new AlarmServiceBinder(); 

	private NotificationManager notificationManager;

	private AlarmManager alarmManager;
	
	
	private PriorityQueue<EventAlarm> alarmQueue = new PriorityQueue<EventAlarm>(MAX_QUEUE_SIZE, new AlarmTimeComparator());
	
	private class AlarmTimeComparator implements Comparator<EventAlarm> {

		@Override
		public int compare(EventAlarm alarm1, EventAlarm alarm2) {
			Date today = TimetableUtils.getCurrentTime();
			return alarm1.getNextOccurrence(today).compareTo(alarm2.getNextOccurrence(today));
		}
	}
	
	@Override 
	public void onCreate() {
		super.onCreate();
		alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	@Override 
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		loadAlarms();
		
		return Service.START_STICKY; 
	}
	
	public class AlarmServiceBinder extends Binder {
		public AlarmService getService() {
			return AlarmService.this;
		}
	}
	
	private Intent getIntentFromAlarm(EventAlarm alarm) {
		Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
		intent.putExtra(EXTRA_EVENT_ID_STRING, alarm.event.id);
		return intent;
	}
	
	private PendingIntent getPendingIntentFromAlarm(EventAlarm alarm) {
		return PendingIntent.getBroadcast(this, alarm.event.id, getIntentFromAlarm(alarm), PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	
	
	/*
	 * Create alarm with pending intent, that will be broadcasted to this class, when alarm should run.
	 */
	public void createAlarm(EventAlarm alarm) {
		TimetableLogger.log("AlarmService.createAlarm: creating alarm");
		
		Date nextOccurrence = alarm.getNextOccurrence(TimetableUtils.getCurrentTime());
		if (nextOccurrence == null) {
			TimetableLogger.log("AlarmService.creeateAlarm: has no next occurrence");
			return;
		}
		
		alarmManager.set(AlarmManager.RTC_WAKEUP, nextOccurrence.getTime(), getPendingIntentFromAlarm(alarm));
		Iterator<EventAlarm> iterator = alarmQueue.iterator();
		while(iterator.hasNext()) {
			if (iterator.next().id == alarm.id) {
				iterator.remove();
				break;
			}
		}
		alarmQueue.offer(alarm);
		updateNotification();
		TimetableLogger.log("Alarm successfully created.");
	}
	
	
	/*
	 * Delete alarm.
	 * Delete notification, if needed.
	 */
	public void deleteAlarm(EventAlarm alarm) {
		TimetableLogger.log("AlarmService.updateAlarm: deleting alarm");
		if (!alarmQueue.contains(alarm)) {
			return;
		}
		PendingIntent mIntent = getPendingIntentFromAlarm(alarm);
		alarmManager.cancel(mIntent);
		mIntent.cancel();
		alarmQueue.remove(alarm);
		updateNotification();
	}
	
	public void updateAlarm(EventAlarm alarm) {
		if (alarm.getNextOccurrence(TimetableUtils.getCurrentTime()) != null) {
			createAlarm(alarm);
		} else {
			deleteAlarm(alarm);
		}	
	}
	
	public boolean existAlarm(EventAlarm alarm) {
		return PendingIntent.getBroadcast(this, alarm.event.id, getIntentFromAlarm(alarm), PendingIntent.FLAG_NO_CREATE) != null;
	}
	
	public EventAlarm getNextAlarm() {
		return alarmQueue.peek();
	}
	
	public void loadAlarms() {
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		
		Vector<Event> events = db.searchEventsWithAlarm();
		Date today = TimetableUtils.getCurrentTime();
		for (Event event : events) {
			EventAlarm alarm = event.alarm;
			if (alarm.getNextOccurrence(today) != null) {
				TimetableLogger.error("Creating alarm.");
				createAlarm(alarm);
			}
		}
		db.close();
	}
	
	public Intent getNotificationIntent() {
		Intent notificationIntent = new Intent(this, EventDayViewActivity.class);
		if (getNextAlarm() != null) {
			Date nextAlarmEventDate = getNextAlarm().getNextEventOccurrence(TimetableUtils.getCurrentTime());
			notificationIntent.putExtra(EventDayViewActivity.EXTRAS_DATE, EventDayViewActivity.EXTRAS_DATE_FORMAT.format(nextAlarmEventDate));
		}
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return notificationIntent;
	}
	
	public PendingIntent getNotificationPendingIntent() {
		PendingIntent intent = PendingIntent.getActivity(this, 0, getNotificationIntent(), 0);
		return intent;
        //return PendingIntent.getBroadcast(this, ALARM_NOTIFICATION_CODE, 
		//		new Intent(this, EventDayViewActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	/*
	 * Create notification informing user, that there are some alarms set.
	 */
	public void createNotification() {
		PendingIntent mIntent = getNotificationPendingIntent(); 
		String nextAlarmString = "No alarms are set.";
		if (getNextAlarm() != null ) {
			Date nextAlarm = getNextAlarm().getNextOccurrence(TimetableUtils.getCurrentTime());
			if (nextAlarm != null) {
				nextAlarmString = NEXT_ALARM_NOTIFICATION_PREFIX + alarmTimeFormat.format(nextAlarm); 
			}
		}
		NotificationCompat.Builder mBuilder = new NotificationCompat
			.Builder(this)
			.setSmallIcon(R.drawable.ic_action_alarms_light)
			.setContentTitle("Timetable")
			.setContentText(nextAlarmString)
			.setLargeIcon(((BitmapDrawable)this.getResources().getDrawable(R.drawable.ic_action_alarms_light)).getBitmap())
			.setContentIntent(mIntent);
		notificationManager.notify(ALARM_NOTIFICATION_CODE, mBuilder.build());
		TimetableLogger.log("Creating notification");
	}
	
	/*
	 * Delete notification.
	 */
	public void deleteNotification() {
		notificationManager.cancel(ALARM_NOTIFICATION_CODE);
		getNotificationPendingIntent().cancel();
	}
	
	public void updateNotification() {
		if (alarmQueue.size() == 0) {
			deleteNotification();
		} else {
			createNotification();
		}
		
	}

}
