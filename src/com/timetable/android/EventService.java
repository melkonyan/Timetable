package com.timetable.android;

import java.util.Date;
import java.util.Vector;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;

import com.timetable.android.utils.TimetableUtils;

public class EventService extends Service {

	private static final String EXTRA_ACTION = "extra_action";
	
	private static final String EXTRA_EVENT_ID = "extra_event_id";
	
	private static final String ACTION_MUTE_DEVICE = "action_mute_device";
	
	private static final String ACTION_UNMUTE_DEVICE = "action_unmute_device";
	
	private static final String ACTION_ADD_EVENT = "action_add_event";
	
	private static final String ACTION_UPDATE_EVENT = "action_update_event";
	
	private static final String ACTION_DELETE_EVENT = "action_delete_event";
	
	private static final String RECEIVER_ACTION = "com.timetable.android.RECEIVER_ACTION";
	
	private AlarmManager alarmManager;
	
	private AudioManager audioManager;
	
	private TimetableDatabase db; 
	
	private TaskReceiver mReceiver;
	
	public static void startService(Context context) {
		context.startService(new Intent(context, EventService.class));			
	}
	
	public static void addEvent(Context context, Event event) {
		if (!event.muteDevice) {
			TimetableLogger.error("device not mutes event.");
			return;
		}
		sendTask(context, ACTION_ADD_EVENT, event);
	}
	
	public static void updateEvent(Context context, Event event) {
		sendTask(context, ACTION_UPDATE_EVENT, event);
	}
	
	public static void deleteEvent(Context context, Event event) {
		sendTask(context, ACTION_DELETE_EVENT, event);
	}
	
	private static void sendTask(Context context, String action, Event event) {
		Intent intent = new Intent(RECEIVER_ACTION);
		intent.putExtra(EXTRA_ACTION, action);
		intent.putExtra(EXTRA_EVENT_ID, Integer.toString(event.id));
		context.sendBroadcast(intent);
	}
	
	private void loadEvents() {
		
		Vector<Event> events = db.searchEventsThatMuteDevice();
		
		for (Event event: events) {
			createAlarm(event);
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override 
	public void onCreate() {
		super.onCreate();
		TimetableLogger.log("EventService: service is created.");
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		db = TimetableDatabase.getInstance(EventService.this);
		mReceiver = new TaskReceiver();
		registerReceiver(mReceiver, new IntentFilter(RECEIVER_ACTION));
		loadEvents();
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		TimetableLogger.log("EventService: service is started.");
		return Service.START_STICKY; 
	}
	
	@Override
	public void onDestroy () {
		super.onDestroy();
		TimetableLogger.log("EventService.onDestroy: service is destroyed.");
		unregisterReceiver(mReceiver);
	}
	
	private PendingIntent getPendingIntentFromEvent(Event event, String action) {
		Intent intent = new Intent(RECEIVER_ACTION);
		TimetableLogger.error("EventService.getPendingIntentFromEvent: event id " + Integer.toString(event.id));
		intent.putExtra(EXTRA_ACTION, action);
		intent.putExtra(EXTRA_EVENT_ID, event.id);
		return PendingIntent.getBroadcast(this.getApplicationContext(), event.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	private void muteDevice() {
		TimetableLogger.log("EventService.muteDevice: try to mute device.");
		audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
	}
	
	private void unmuteDevice() {
		TimetableLogger.log("EventService.unmuteDevice: try to unmute device.");
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}
	
	private void createAlarm(Event event) {
		Date nextStartTime = event.getNextStartTime(TimetableUtils.getCurrentTime());
		if (nextStartTime == null && event.isCurrent(TimetableUtils.getCurrentTime())) {
			TimetableLogger.error("event is current");
			nextStartTime = TimetableUtils.getCurrentTime();
		}
		
		if (nextStartTime == null) {
			TimetableLogger.error("no start time");
			return;
		}
		TimetableLogger.log("EventService.createAlarm: creating alarm at " + nextStartTime.toString());
		alarmManager.set(AlarmManager.RTC_WAKEUP, 
							nextStartTime.getTime(), 
							getPendingIntentFromEvent(event, ACTION_MUTE_DEVICE));
	}
	
	private void createUnmuteAlarm(Event event) {
		Date nextEndTime = event.getNextEndTime(TimetableUtils.getCurrentTime());
		if (nextEndTime == null) {
			return;
		}
		alarmManager.set(AlarmManager.RTC_WAKEUP, 
							nextEndTime.getTime(), 
							getPendingIntentFromEvent(event, ACTION_UNMUTE_DEVICE));
	}
	
	private void updateAlarm(Event event) {
		if (event.getNextStartTime(TimetableUtils.getCurrentTime()) != null) {
			createAlarm(event);
		} else {
			deleteAlarm(event);
		}
	}
	
	private void deleteAlarm(Event event) {
		//TODO: if device is muted by this event, unmute id immediately
		alarmManager.cancel(getPendingIntentFromEvent(event, ACTION_MUTE_DEVICE));
	}
	
	public class TaskReceiver extends BroadcastReceiver {

		public TaskReceiver() {
			super();
		}
		@Override
		public void onReceive(Context context, Intent intent) {
			//TODO: receive boot complete event
			Bundle extras = intent.getExtras();
			if (extras == null) {
				TimetableLogger.error("EventService.TaskReceiver.onReceive: intent with no data is received");
				return;
			}
			String action = extras.getString(EXTRA_ACTION);
			TimetableLogger.log("EventService.TaskReceiver: action " + action + " received.");
			int eventId = extras.getInt(EXTRA_EVENT_ID);
			Event event = db.searchEventById(eventId);
			if (event == null) {
				TimetableLogger.error("EventService.TaskReceiver.onReceive: event with id " + Integer.toString(eventId) + " is not found.");
				return;
			}
			//TimetableLogger.error("EventService: event received: " + event.toString());
			if (action == ACTION_ADD_EVENT) {
				createAlarm(event);
			} else if (action == ACTION_UPDATE_EVENT) {
				updateAlarm(event);
			} else if (action == ACTION_DELETE_EVENT) {
				deleteAlarm(event);
			} else if (action.equals(ACTION_MUTE_DEVICE)) {
				//TimetableLogger.error("EventService: event is current - " + Boolean.toString(event.isCurrent(TimetableUtils.getCurrentTime())));
				if (event.isCurrent(TimetableUtils.getCurrentTime())) {
					//TimetableLogger.error("EventService. event is current");
					muteDevice();
					createUnmuteAlarm(event);
						
				}
			} else if (action == ACTION_UNMUTE_DEVICE) {
				unmuteDevice();
			}
		}
		
	}
	
	
}
