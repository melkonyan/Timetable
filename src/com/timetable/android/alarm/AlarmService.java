package com.timetable.android.alarm;

import java.text.SimpleDateFormat;
import java.util.Collections;
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
import android.view.WindowManager;

import com.timetable.android.BroadcastActions;
import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.Logger;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.Utils;

/*
 * Service, that creates alarm, that would be fired later.
 * Has internal BroadcastReceiver, that receives actions: ACTION_EVENT_ADDED, ACTION_EVENT_UPDATED, ACTION_EVENT_DELETED 
 * and ACTION_ALARM_DISMISSED, that is broadcasted by AlarmDialogActivity, when user dismisses alarm, and it should be recreated.
 */
public class AlarmService extends Service {

	public  static final int ALARM_NOTIFICATION_CODE = 123;

	public static final String ACTION_ALARM_FIRED = "com.timetable.android.ACTION_ALARM_FIRED";
	
	public static final String ACTION_ALARM_DISMISSED = "com.timetable.android.ACTION_ALARM_DISMISSED";
	
	public static final String ACTION_ALARM_SNOOZED = "com.timetable.android.ACTION_ALARM_SNOOZED";
	
	public static final long SNOOZE_TIME = 10 * DateUtils.MINUTE_MILLIS;
	
	public static final int MAX_QUEUE_SIZE = 10000;
	
	private static final String NEXT_ALARM_NOTIFICATION_PREFIX = "";
	
	public static final SimpleDateFormat alarmTimeFormat = new SimpleDateFormat("EEE, d. MMM yyyy 'at' HH:mm", Locale.US);
	
	private NotificationManager notificationManager;

	private AlarmManager alarmManager;
	
	private AlarmBroadcastReceiver mReceiver;
	
	private AlarmAdapter mAlarmAdapter;
	
	private class AlarmTimeComparator implements Comparator<AlarmContainer> {

		@Override
		public int compare(AlarmContainer alarm1, AlarmContainer alarm2) {
			return alarm1.nextOccurrence.compareTo(alarm2.nextOccurrence);
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
		mAlarmAdapter = new AlarmAdapter();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BroadcastActions.ACTION_EVENT_ADDED);
		intentFilter.addAction(BroadcastActions.ACTION_EVENT_UPDATED);
		intentFilter.addAction(BroadcastActions.ACTION_EVENT_DELETED);
		intentFilter.addAction(AlarmService.ACTION_ALARM_FIRED);
		intentFilter.addAction(AlarmService.ACTION_ALARM_DISMISSED);
		intentFilter.addAction(AlarmService.ACTION_ALARM_SNOOZED);
		registerReceiver(mReceiver, intentFilter);
		
		loadAlarms();
		Logger.log("AlarmService.onCreate: service is successfully created");
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.log("AlarmService.onStartCommand: service is successfully started");
		return Service.START_STICKY; 
	}
	
	private Intent getIntentFromEvent(Event event) {
		Intent intent = new Intent(ACTION_ALARM_FIRED);
		intent.putExtras(event.convert());
		return intent;
	}
	
	private PendingIntent getPendingIntentFromEvent(Event event) {
		return PendingIntent.getBroadcast(this, event.getId(), getIntentFromEvent(event), PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	public void createAlarm(Event event, Long alarmTime) {
		alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, getPendingIntentFromEvent(event));
		mAlarmAdapter.add(event, new Date(alarmTime));
		updateNotification();
		
	}
	/*
	 * Create alarm with pending intent, that will be broadcasted to this class, when alarm should run.
	 */
	public void createAlarm(Event event) {
		if (!event.hasAlarm()) {
			return;
		}
		Date nextOccurrence = event.getAlarm().getNextOccurrence();
		if (nextOccurrence == null) {
			return;
		}
		if (nextOccurrence.compareTo(Utils.getCurrDateTime()) <= 0) {
			Logger.error("AlarmService.createAlarm: next alarm occurrence is before current time. \n Next alarm: " 
									+ nextOccurrence.toString() + "\n current time: " + Utils.getCurrDateTime().toString() 
									+ "Event information: \n" + event.toString());
			return;
		}
		createAlarm(event, nextOccurrence.getTime());
		Logger.log("AlarmService.createAlarm: creating alarm on date: " + nextOccurrence.toString());
		
	}
	
	
	/*
	 * Delete alarm.
	 * Delete notification, if needed.
	 */
	public void deleteAlarm(Event event) {
		Logger.log("AlarmService.deleteAlarm: deleting alarm");
		if (!mAlarmAdapter.delete(event)) {
			Logger.log("AlarmService.deleteAlarm: no such alarm found.");
			return;
		}
		PendingIntent mIntent = getPendingIntentFromEvent(event);
		alarmManager.cancel(mIntent);
		mIntent.cancel();
		updateNotification();
	}
	
	public void updateAlarm(Event event) {
		if (event.hasAlarm() && event.getAlarm().getNextOccurrence() != null) {
			createAlarm(event);
		} else {
			deleteAlarm(event);
		}	
	}
	
	public void snoozeAlarm(Event event) {
		mAlarmAdapter.delete(event);
		createAlarm(event, Utils.getCurrDateTime().getTime() + SNOOZE_TIME);
	}
	
	public boolean existAlarm(Event event) {
		return PendingIntent.getBroadcast(this, event.getId(), getIntentFromEvent(event), PendingIntent.FLAG_NO_CREATE) != null;
	}
	
	
	public void loadAlarms() {
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		
		Vector<Event> events = db.searchEventsWithAlarm();
		Collections.reverse(events);
		Date today = Utils.getCurrDateTime();
		for (Event event : events) {
			EventAlarm alarm = event.getAlarm();
			if (alarm.getNextOccurrence(today) != null) {
				createAlarm(event);
			}
		}
	}
	
	public Intent getNotificationIntent() {
		Intent notificationIntent = new Intent(this, EventDayViewActivity.class);
		if (mAlarmAdapter.getNextAlarm() != null) {
			Date nextAlarmEventDate = mAlarmAdapter.getNextAlarm().nextOccurrence;
			notificationIntent.putExtra(EventDayViewActivity.EXTRAS_DATE, EventDayViewActivity.EXTRAS_DATE_FORMAT.format(nextAlarmEventDate));
			//Unless this hack pending intent in notification is not updated and false date is shown, when user clicks it.
			notificationIntent.setAction(Long.toString(System.currentTimeMillis()));
		}
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return notificationIntent;
	}
	
	public PendingIntent getNotificationPendingIntent() {
		PendingIntent intent = PendingIntent.getActivity(this, PendingIntent.FLAG_CANCEL_CURRENT, getNotificationIntent(), 0);
		return intent;
		
    }
	
	/*
	 * Create notification informing user, that there are some alarms set.
	 */
	public void createNotification() {
		PendingIntent mIntent = getNotificationPendingIntent(); 
		String nextAlarmString = "No alarms are set.";
		if (mAlarmAdapter.getNextAlarm() != null ) {
			Date nextAlarm = mAlarmAdapter.getNextAlarm().nextOccurrence;
			if (nextAlarm != null) {
				nextAlarmString = NEXT_ALARM_NOTIFICATION_PREFIX + alarmTimeFormat.format(nextAlarm); 
			}
		}
		NotificationCompat.Builder mBuilder = new NotificationCompat
			.Builder(this)
			.setSmallIcon(R.drawable.ic_action_alarms_light)
			.setContentTitle("Next alarm: ")
			.setContentText(nextAlarmString)
			.setLargeIcon(((BitmapDrawable)this.getResources().getDrawable(R.drawable.ic_action_alarms_light)).getBitmap())
			.setWhen(0)
			.setContentIntent(mIntent);
		notificationManager.notify(ALARM_NOTIFICATION_CODE, mBuilder.build());
		Logger.log("AlarmService: alarm notification created.");
	}
	
	/*
	 * Delete notification.
	 */
	public void deleteNotification() {
		notificationManager.cancel(ALARM_NOTIFICATION_CODE);
		getNotificationPendingIntent().cancel();
	}
	
	public void updateNotification() {
		if (mAlarmAdapter.isEmpty()) {
			deleteNotification();
		} else {
			createNotification();
		}
		
	}
	
	public static class AlarmContainer {
		
		public EventAlarm alarm;
		
		public Date nextOccurrence;
		
		public AlarmContainer(EventAlarm _alarm, Date _nextOccurrence) {
			alarm = _alarm;
			nextOccurrence = _nextOccurrence;
		}
	}
	
	public class AlarmAdapter {

		private PriorityQueue<AlarmContainer> alarmQueue = new PriorityQueue<AlarmContainer>(MAX_QUEUE_SIZE, new AlarmTimeComparator());
		
		public void add(Event event) {
			add(event, event.getAlarm().getNextOccurrence());
		}
		
		public void add(Event event, Date nextOccurrence) {
			delete(event);
			alarmQueue.offer(new AlarmContainer(event.getAlarm(), nextOccurrence));
		}
		
		/*
		 * Remove event's alarm from adapter. Return true if alarm was found.
		 */
		public boolean delete(Event event) {
			Iterator<AlarmContainer> iterator = alarmQueue.iterator();
			while(iterator.hasNext()) {
				EventAlarm nextAlarm = iterator.next().alarm;
				if (nextAlarm.event.getId() == event.getId()) {
					iterator.remove();
					return true;
				}
			}
			return false;
		
		}
		
		public void update(Event event) {
			add(event);
		}
		
		public AlarmContainer getNextAlarm() {
			if (isEmpty()) {
				return null;
			}
			return alarmQueue.peek();
		}
		
		public boolean isEmpty() {
			return alarmQueue.isEmpty();
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
				Logger.error("AlarmService.onReceive: unable to create event from received data. " + e.getMessage());
				return;
			}
			Logger.log("AlarmService.onReceive: action " + action + " received with event " + event.getName() + ", id " + Integer.toString(event.getId()));
			
			if (action.equals(AlarmService.ACTION_ALARM_FIRED)) {
				if (!AlarmDialogActivity.checkEvent(context, eventData)) {
					return;
				}
				Intent alarmDialogIntent = new Intent(context, AlarmDialogActivity.class);
				alarmDialogIntent.putExtras(eventData);
				alarmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				alarmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				alarmDialogIntent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD + WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON + WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
			    context.startActivity(alarmDialogIntent);
				return;
			}
			
			if (action.equals(BroadcastActions.ACTION_EVENT_ADDED)) {
				createAlarm(event);
			} else if (action.equals(BroadcastActions.ACTION_EVENT_UPDATED) || action.equals(ACTION_ALARM_DISMISSED)) {
				updateAlarm(event);
			} else if (action.equals(BroadcastActions.ACTION_EVENT_DELETED)) {
				deleteAlarm(event);
			}  else if (action.equals(ACTION_ALARM_SNOOZED)) {
				snoozeAlarm(event);
			}
		}
	}
}

	
