package com.timetable.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
	
	private AlarmManager alarmManager;
	
	private AudioManager audioManager;
	
	public static void addEvent(Context context, Event event) {
		sendTask(context, ACTION_ADD_EVENT, event);
	}
	
	public static void updateEvent(Context context, Event event) {
		sendTask(context, ACTION_UPDATE_EVENT, event);
	}
	
	public static void deleteEvent(Context context, Event event) {
		sendTask(context, ACTION_DELETE_EVENT, event);
	}
	
	private static void sendTask(Context context, String action, Event event) {
		Intent intent = new Intent(context, EventService.class);
		intent.putExtra(EXTRA_ACTION, action);
		intent.putExtra(EXTRA_EVENT_ID, Integer.toString(event.id));
		context.sendBroadcast(intent);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override 
	public void onCreate() {
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY; 
	}
	
	private PendingIntent getPendingIntentFromEvent(Event event, String action) {
		Intent intent = new Intent(this, TaskReceiver.class);
		intent.putExtra(EXTRA_ACTION, action);
		intent.putExtra(EXTRA_EVENT_ID, Integer.toString(event.id));
		return PendingIntent.getBroadcast(this, event.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	private void muteDevice() {
		audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
	}
	
	private void unmuteDevice() {
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}
	
	private void createAlarm(Event event) {
		alarmManager.set(AlarmManager.RTC_WAKEUP, 
							event.getNextOccurrenceTime(TimetableUtils.getCurrentTime()).getTime(), 
							getPendingIntentFromEvent(event, ACTION_MUTE_DEVICE));
	}
	
	private void updateAlarm(Event event) {
		if (event.getNextOccurrenceTime(TimetableUtils.getCurrentTime()) != null) {
			createAlarm(event);
		} else {
			deleteAlarm(event);
		}
	}
	
	private void deleteAlarm(Event event) {
		alarmManager.cancel(getPendingIntentFromEvent(event, ACTION_MUTE_DEVICE));
	}
	
	private class TaskReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			String action = extras.getString(EXTRA_ACTION);
			int eventId = extras.getInt(EXTRA_EVENT_ID);
			TimetableDatabase db = TimetableDatabase.getInstance(EventService.this);
			
			Event event = db.searchEventById(eventId);
			
			if (action == ACTION_ADD_EVENT) {
				createAlarm(event);
			} else if (action == ACTION_UPDATE_EVENT) {
				updateAlarm(event);
			} else if (action == ACTION_DELETE_EVENT) {
				deleteAlarm(event);
			} else if (action == ACTION_MUTE_DEVICE) {
				if (event.isCurrent(TimetableUtils.getCurrentTime())) {
					muteDevice();
				}
				//TODO: add intent to unmute device
			} else if (action == ACTION_UNMUTE_DEVICE) {
				unmuteDevice();
			}
		}
		
	}
	
	
}
