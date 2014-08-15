package com.timetable.android;

import java.text.ParseException;
import java.util.Date;
import java.util.Vector;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;

import com.timetable.android.utils.TimetableUtils;

/*
 * Class, that receives such actions as ACTION_EVENT_ADDED, ACTION_EVENT_UPDATED, ACTION_EVENT_DELETED 
 * and sends action ACTION_EVENT_STARTED, ACTION_EVENT_ENDED.
 */
public class EventService extends Service {
	
	private AlarmManager alarmManager;
	
	private EventReceiver mReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override 
	public void onCreate() {
		super.onCreate();
		mReceiver = new EventReceiver();
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BroadcastActions.ACTION_EVENT_ADDED);
		intentFilter.addAction(BroadcastActions.ACTION_EVENT_UPDATED);
		intentFilter.addAction(BroadcastActions.ACTION_EVENT_DELETED);
		registerReceiver(mReceiver, intentFilter);
		loadEvents(this);
		TimetableLogger.log("EventService: service is successfully created.");
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		TimetableLogger.log("EventService: service is successfully started.");
		return Service.START_STICKY; 
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		TimetableLogger.log("EventService: service is destroyed.");
	}
	
	
	private void loadEvents(Context context) {
		TimetableDatabase db = TimetableDatabase.getInstance(context);
		Vector<Event> events = db.getAllEvents();
		for (Event event: events) {
			createEventStartedAlarm(this, event);
		}
	}

	private PendingIntent getPendingIntentFromEvent(Context context, Event event, String action) {
		Intent intent = new Intent(action);
		intent.putExtras(event.convert());
		return PendingIntent.getBroadcast(context, event.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	private void createEventStartedAlarm(Context context, Event event) {
		Date nextStartTime;
		Date currentTime = TimetableUtils.getCurrentTime();
		if (event.isCurrent(currentTime)) {
			nextStartTime = currentTime;
		}
		else {
			nextStartTime = event.getNextStartTime(currentTime);
		}
		
		if (nextStartTime == null) {
			TimetableLogger.log("EventService.createEventStartedAlarm: event already finished.");
			return;
		}
		
		TimetableLogger.log("EventService.createEventStartedAlarm: creating alarm at " + nextStartTime.toString());
		alarmManager.set(AlarmManager.RTC_WAKEUP, 
							nextStartTime.getTime(), 
							getPendingIntentFromEvent(context, event, BroadcastActions.ACTION_EVENT_STARTED));
	}
	
	private void createEventEndedAlarm(Context context, Event event) {
		Date nextEndTime = event.getNextEndTime(TimetableUtils.getCurrentTime());
		if (nextEndTime == null) {
			TimetableLogger.log("EventService.createEventEndedAlarm: event already finished.");
			return;
		}
		TimetableLogger.log("EventService.createEventEndedAlarm: creating alarm at " + nextEndTime.toString());
		alarmManager.set(AlarmManager.RTC_WAKEUP, 
							nextEndTime.getTime(), 
							getPendingIntentFromEvent(context, event, BroadcastActions.ACTION_EVENT_ENDED));
	}
	
	private void updateAlarm(Context context, Event event) {
		if (event.getNextStartTime(TimetableUtils.getCurrentTime()) != null || event.isCurrent(TimetableUtils.getCurrentTime())) {
			createEventStartedAlarm(context, event);
			createEventEndedAlarm(context, event);
		} else {
			deleteAlarm(context, event);
		}
	}
	
	private void deleteAlarm(Context context, Event event) {
		alarmManager.cancel(getPendingIntentFromEvent(context, event, BroadcastActions.ACTION_EVENT_STARTED));
	}
	
	private class EventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == null) {
				return;
			}
			TimetableLogger.log("EventService.onReceive: received action " + action);
			alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Bundle eventData = intent.getExtras();
			TimetableLogger.error(eventData.toString());
			Event event;
			try {
				event = new Event(eventData);
			} catch (ParseException e) {
				TimetableLogger.error("EventService.onReceive: unable to create event from received data. " + e.getMessage());
				return;
			}
			
			if (BroadcastActions.ACTION_EVENT_ADDED.equals(action)) {
				createEventStartedAlarm(context, event);
				createEventEndedAlarm(context, event);
			} else if (BroadcastActions.ACTION_EVENT_UPDATED.equals(action)) {
				updateAlarm(context, event);
				if (!event.isCurrent(TimetableUtils.getCurrentTime())) {
					EventBroadcastSender.sendEventEndedBroadcast(context, eventData);
				}
			} else if (BroadcastActions.ACTION_EVENT_DELETED.equals(action)) {
				deleteAlarm(context, event);
				if (event.isCurrent(TimetableUtils.getCurrentTime())) {
					EventBroadcastSender.sendEventEndedBroadcast(context, eventData);
				}
			}
		}
	}
	
}
