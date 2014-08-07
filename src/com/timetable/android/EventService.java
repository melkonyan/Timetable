package com.timetable.android;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
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
import android.os.Vibrator;

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

	private static final long VIBRATE_DURATION_MILLIS = 500;
	
	private AlarmManager alarmManager;
	
	private AudioManager audioManager;
	
	private Vibrator vibrator;
	
	private TimetableDatabase db; 
	
	private TaskReceiver mReceiver;
	
	private Set<Event> currentEvents = new TreeSet<Event>(new Comparator<Event>() {

		@Override
		public int compare(Event lhs, Event rhs) {
			return lhs.id - rhs.id;
		}
	});
	
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
		intent.putExtra(EXTRA_EVENT_ID, event.id);
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
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		db = TimetableDatabase.getInstance(EventService.this);
		mReceiver = new TaskReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(RECEIVER_ACTION);
		intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
		registerReceiver(mReceiver, intentFilter);
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
	
	private void vibrate() {
		vibrator.vibrate(VIBRATE_DURATION_MILLIS);
	}
	
	private void muteDevice() {
		TimetableLogger.log("EventService.muteDevice: try to mute device.");
		audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		vibrate();
	}
	
	private void unmuteDevice() {
		TimetableLogger.log("EventService.unmuteDevice: try to unmute device.");
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		vibrate();
	}
	
	private void createAlarm(Event event) {
		Date nextStartTime;
		if (event.isCurrent(TimetableUtils.getCurrentTime())) {
			//TimetableLogger.error("event is current");
			nextStartTime = TimetableUtils.getCurrentTime();
		}
		else {
			nextStartTime = event.getNextStartTime(TimetableUtils.getCurrentTime());
		}
		
		if (nextStartTime == null) {
			//TimetableLogger.error("no start time");
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
		alarmManager.cancel(getPendingIntentFromEvent(event, ACTION_UNMUTE_DEVICE));
	}
	
	public class TaskReceiver extends BroadcastReceiver {

		public TaskReceiver() {
			super();
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
				context.startService(new Intent(context, EventService.class));
				return;
			}
			
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
				event = new Event();
				event.id = eventId;
			}
			if (currentEvents.contains(event)) {
				currentEvents.remove(event);
				if (currentEvents.isEmpty()) {
					unmuteDevice();
				}
			}
			//TimetableLogger.error("EventService: event received: " + event.toString());
			if (action.equals(ACTION_ADD_EVENT)) {
				createAlarm(event);
			} else if (action.equals(ACTION_UPDATE_EVENT)) {
				updateAlarm(event);
			} else if (action.equals(ACTION_DELETE_EVENT)) {
				deleteAlarm(event);
			} else if (action.equals(ACTION_MUTE_DEVICE)) {
				//TimetableLogger.error("EventService: event is current - " + Boolean.toString(event.isCurrent(TimetableUtils.getCurrentTime())));
				if (event.isCurrent(TimetableUtils.getCurrentTime())) {
					//TimetableLogger.error("EventService. event is current");
					currentEvents.add(event);
					muteDevice();
					createUnmuteAlarm(event);
						
				}
			} else if (action.equals(ACTION_UNMUTE_DEVICE)) {
			}
		}
		
	}
	
	
}
