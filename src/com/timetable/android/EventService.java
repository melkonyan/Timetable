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

import com.timetable.android.utils.Utils;

/*
 * Class, that receives such actions as ACTION_EVENT_ADDED, ACTION_EVENT_UPDATED, ACTION_EVENT_DELETED,
 * when events are changed. It creates alarms, notifying that event has started or ended by  
 * sending actions ACTION_EVENT_STARTED, ACTION_EVENT_ENDED.
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
		Logger.log("EventService: service is successfully created.");
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Logger.log("EventService: service is successfully started.");
		return Service.START_STICKY; 
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		Logger.log("EventService: service is destroyed.");
	}
	
	/*
	 * Function to call when service is created.
	 * Load all events from database and create alarms if needed.
	 */
	private void loadEvents(Context context) {
		TimetableDatabase db = TimetableDatabase.getInstance(context);
		Vector<Event> events = db.getAllEvents();
		for (Event event: events) {
			createEventAlarms(context, event);
		}
	}

	private PendingIntent getPendingIntentFromEvent(Context context, Event event, String action) {
		Intent intent = new Intent(action);
		intent.putExtras(event.convert());
		return PendingIntent.getBroadcast(context, event.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	/*
	 * If event has start and end time, alarms would be created.
	 */
	private void createEventAlarms(Context context, Event event) {
		if (event.hasStartTime() && event.hasEndTime()) {
			createEventStartedAlarm(context, event);
			createEventEndedAlarm(context, event);
		}
	}
	
	/*
	 * Function to call, when event was updated.
	 * It checks whether event has occurrence in the future, and creates or deletes alarm for it.
	 */
	private void updateEventAlarms(Context context, Event event) {
		if (event.getNextStartTime(Utils.getCurrDateTime()) != null || event.isCurrent(Utils.getCurrDateTime())) {
			createEventAlarms(context, event);
		} else {
			deleteEventAlarms(context, event);
		}
	}
	
	/*
	 * If event was deleted, we need to delete alarms, notifying about the start or the end of event.
	 */
	private void deleteEventAlarms(Context context, Event event) {
		alarmManager.cancel(getPendingIntentFromEvent(context, event, BroadcastActions.ACTION_EVENT_STARTED));
		alarmManager.cancel(getPendingIntentFromEvent(context, event, BroadcastActions.ACTION_EVENT_ENDED));
	}
	
	/*
	 * Create alarm, notifying, that event has started. 
	 */
	private void createEventStartedAlarm(Context context, Event event) {
		Date nextStartTime;
		Date currentTime = Utils.getCurrDateTime();
		if (event.isCurrent(currentTime)) {
			nextStartTime = currentTime;
		}
		else {
			nextStartTime = event.getNextStartTime(currentTime);
		}
		
		if (nextStartTime == null) {
			Logger.log("EventService.createEventStartedAlarm: event already finished.");
			return;
		}
		
		Logger.log("EventService.createEventStartedAlarm: creating alarm at " + nextStartTime.toString());
		alarmManager.set(AlarmManager.RTC_WAKEUP, 
							nextStartTime.getTime(), 
							getPendingIntentFromEvent(context, event, BroadcastActions.ACTION_EVENT_STARTED));
	}
	
	/*
	 * Create alarm, notifying, that event has ended.
	 */
	private void createEventEndedAlarm(Context context, Event event) {
		Date nextEndTime = event.getNextEndTime(Utils.getCurrDateTime());
		if (nextEndTime == null) {
			Logger.log("EventService.createEventEndedAlarm: event already finished.");
			return;
		}
		Logger.log("EventService.createEventEndedAlarm: creating alarm at " + nextEndTime.toString());
		alarmManager.set(AlarmManager.RTC_WAKEUP, 
							nextEndTime.getTime(), 
							getPendingIntentFromEvent(context, event, BroadcastActions.ACTION_EVENT_ENDED));
	}
	
	
	private class EventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == null) {
				return;
			}
			Logger.log("EventService.onReceive: received action " + action);
			alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Bundle eventData = intent.getExtras();
			Event event;
			try {
				event = new Event(eventData);
			} catch (ParseException e) {
				Logger.error("EventService.onReceive: unable to create event from received data. " + e.getMessage());
				return;
			}
			
			if (BroadcastActions.ACTION_EVENT_ADDED.equals(action)) {
				createEventAlarms(context, event);
			} else if (BroadcastActions.ACTION_EVENT_UPDATED.equals(action)) {
				updateEventAlarms(context, event);
				if (!event.isCurrent(Utils.getCurrDateTime())) {
					EventBroadcastSender.sendEventEndedBroadcast(context, eventData);
				}
			} else if (BroadcastActions.ACTION_EVENT_DELETED.equals(action)) {
				deleteEventAlarms(context, event);
				if (event.isCurrent(Utils.getCurrDateTime())) {
					EventBroadcastSender.sendEventEndedBroadcast(context, eventData);
				}
			}
		}
	}
	
}
