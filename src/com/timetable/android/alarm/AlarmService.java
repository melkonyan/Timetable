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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.timetable.android.BroadcastActions;
import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.utils.TimetableUtils;

public class AlarmService extends Service {

	public  static final int ALARM_NOTIFICATION_CODE = 123;

	public static final String ACTION_ALARM_FIRED = "com.timetable.android.ACTION_ALARM_FIRED";
	
	public static final String ACTION_ALARM_UPDATED = "com.timetable.android.ACTION_ALARM_UPDATED";
	
	public static final int MAX_QUEUE_SIZE = 10000;
	
	private static final String NEXT_ALARM_NOTIFICATION_PREFIX = "Next alarm is on: ";
	
	public static final SimpleDateFormat alarmTimeFormat = new SimpleDateFormat("EEE, d. MMM yyyy 'at' HH:mm", Locale.US);
	
	private NotificationManager notificationManager;

	private AlarmManager alarmManager;
	
	private AlarmBroadcastReceiver mReceiver;
	
	private PriorityQueue<EventAlarm> alarmQueue = new PriorityQueue<EventAlarm>(MAX_QUEUE_SIZE, new AlarmTimeComparator());
	
	private class AlarmTimeComparator implements Comparator<EventAlarm> {

		@Override
		public int compare(EventAlarm alarm1, EventAlarm alarm2) {
			Date today = TimetableUtils.getCurrentTime();
			return alarm1.getNextOccurrence(today).compareTo(alarm2.getNextOccurrence(today));
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override 
	public void onCreate() {
		super.onCreate();
		alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		mReceiver = new AlarmBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BroadcastActions.ACTION_EVENT_ADDED);
		intentFilter.addAction(BroadcastActions.ACTION_EVENT_UPDATED);
		intentFilter.addAction(BroadcastActions.ACTION_EVENT_DELETED);
		intentFilter.addAction(AlarmService.ACTION_ALARM_FIRED);
		intentFilter.addAction(AlarmService.ACTION_ALARM_UPDATED);
		registerReceiver(mReceiver, intentFilter);
		loadAlarms();
		TimetableLogger.log("AlarmService.onCreate: service is successfully created");
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		TimetableLogger.log("AlarmService.onStartCommand: service is successfully started");
		return Service.START_STICKY; 
	}
	
	private Intent getIntentFromEvent(Event event) {
		Intent intent = new Intent(ACTION_ALARM_FIRED);
		intent.putExtras(event.convert());
		return intent;
	}
	
	private PendingIntent getPendingIntentFromEvent(Event event) {
		return PendingIntent.getBroadcast(this, event.id, getIntentFromEvent(event), PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	/*
	 * Create alarm with pending intent, that will be broadcasted to this class, when alarm should run.
	 */
	public void createAlarm(Event event) {
		Date nextOccurrence = event.alarm.getNextOccurrence();
		if (nextOccurrence == null) {
			return;
		}
		if (nextOccurrence.compareTo(TimetableUtils.getCurrentTime()) <= 0) {
			TimetableLogger.error("AlarmService.createAlarm: next alarm occurrence is before current time. \n Next alarm: " 
									+ nextOccurrence.toString() + "\n current time: " + TimetableUtils.getCurrentTime().toString() 
									+ "Event information: \n" + event.toString());
			return;
		}
		alarmManager.set(AlarmManager.RTC_WAKEUP, nextOccurrence.getTime(), getPendingIntentFromEvent(event));
		Iterator<EventAlarm> iterator = alarmQueue.iterator();
		while(iterator.hasNext()) {
			if (iterator.next().id == event.alarm.id) {
				iterator.remove();
				break;
			}
		}
		alarmQueue.offer(event.alarm);
		updateNotification();
		TimetableLogger.log("AlarmService.createAlarm: creating alarm on date: " + nextOccurrence.toString());
		
	}
	
	
	/*
	 * Delete alarm.
	 * Delete notification, if needed.
	 */
	public void deleteAlarm(Event event) {
		TimetableLogger.log("AlarmService.updateAlarm: deleting alarm");
		if (!alarmQueue.contains(event.alarm)) {
			return;
		}
		PendingIntent mIntent = getPendingIntentFromEvent(event);
		alarmManager.cancel(mIntent);
		mIntent.cancel();
		alarmQueue.remove(event.alarm);
		updateNotification();
	}
	
	public void updateAlarm(Event event) {
		if (event.alarm.getNextOccurrence() != null) {
			createAlarm(event);
		} else {
			deleteAlarm(event);
		}	
	}
	
	/*
	 * Method to call, event ACTION_EVENT_UPDATED is received, and received event has no alarm.
	 * We need to check, if event's alarm was deleted, and if so, delete it from alarmQueue.
	 */
	public void deleteEventAlarm(Event event) {
		Iterator<EventAlarm> iterator = alarmQueue.iterator();
		while(iterator.hasNext()) {
			EventAlarm nextAlarm = iterator.next();
			if (nextAlarm.event.id == event.id) {
				iterator.remove();
				PendingIntent mIntent = getPendingIntentFromEvent(event);
				alarmManager.cancel(mIntent);
				mIntent.cancel();
				updateNotification();
			}
		}
	}
	
	public boolean existAlarm(Event event) {
		return PendingIntent.getBroadcast(this, event.id, getIntentFromEvent(event), PendingIntent.FLAG_NO_CREATE) != null;
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
				createAlarm(event);
			}
		}
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
			.setWhen(0)
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
	
	/*
	 * Class for working with event alarm.
	 * It contains functional for creating and deleting alarms, receiving them, updating notification, that informs user, that alarm is set. 
	 */
	public class AlarmBroadcastReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Bundle eventData = intent.getExtras();
			Event event;
			try {
				event = new Event(eventData);
			} catch (Exception e) {
				TimetableLogger.error("AlarmService.onReceive: unable to create event from received data. " + e.getMessage());
				return;
			}
			TimetableLogger.log("AlarmService.onReceive: action " + action + " received with event " + event.name + ", id " + Integer.toString(event.id));
			if (!event.hasAlarm()) {
				deleteEventAlarm(event);
			}
			if (action.equals(BroadcastActions.ACTION_EVENT_ADDED)) {
				createAlarm(event);
			} else if (action.equals(BroadcastActions.ACTION_EVENT_UPDATED) || action.equals(ACTION_ALARM_UPDATED)) {
				
				updateAlarm(event);
			} else if (action.equals(BroadcastActions.ACTION_EVENT_DELETED)) {
				deleteAlarm(event);
			} else if (action.equals(AlarmService.ACTION_ALARM_FIRED)) {
				Intent alarmDialogIntent = new Intent(context, AlarmDialogActivity.class);
				alarmDialogIntent.putExtras(eventData);
				alarmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				context.startActivity(alarmDialogIntent);
			}
		}
	}
}

	
