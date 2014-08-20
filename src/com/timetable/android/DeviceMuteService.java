package com.timetable.android;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;

/*
 * Service, that mutes device when appropriate event is started.
 */
public class DeviceMuteService extends Service {

	
	private static final long VIBRATE_DURATION_MILLIS = 500;
	
	private AudioManager audioManager;
	
	private Vibrator vibrator;

	private TaskReceiver mReceiver;
	
	//indicates, weather device was muted by the user, before event, that mutes device, has started.  
	private boolean deviceWasMuted = false;
	
	private Set<Event> currentEvents = new TreeSet<Event>(new Comparator<Event>() {

		@Override
		public int compare(Event lhs, Event rhs) {
			return lhs.getId() - rhs.getId();
		}
	});
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override 
	public void onCreate() {
		super.onCreate();
		TimetableLogger.log("DeviceMuteService: service is created.");
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mReceiver = new TaskReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BroadcastActions.ACTION_EVENT_STARTED);
		intentFilter.addAction(BroadcastActions.ACTION_EVENT_ENDED);
		registerReceiver(mReceiver, intentFilter);
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		TimetableLogger.log("DeviceMuteService: service is started.");
		return Service.START_STICKY; 
	}
	
	@Override
	public void onDestroy () {
		super.onDestroy();
		TimetableLogger.log("DeviceMuteService.onDestroy: service is destroyed.");
		unregisterReceiver(mReceiver);
	}
	
	private boolean isMuted() {
		int state = audioManager.getRingerMode();
		return state == AudioManager.RINGER_MODE_SILENT || state == AudioManager.RINGER_MODE_VIBRATE;
	}
	
	private void vibrate() {
		vibrator.vibrate(VIBRATE_DURATION_MILLIS);
	}
	
	private void muteDevice() {
		TimetableLogger.log("DeviceMuteService.muteDevice: try to mute device.");
		audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		vibrate();
	}
	
	private void unmuteDevice() {
		TimetableLogger.log("DeviceMuteService.unmuteDevice: try to unmute device.");
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		vibrate();
	}
	
	
	public class TaskReceiver extends BroadcastReceiver {

		public TaskReceiver() {
			super();
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			Bundle extras = intent.getExtras();
			if (extras == null) {
				TimetableLogger.error("DeviceMuteService.TaskReceiver.onReceive: intent with no data is received");
				return;
			}
			
			String action = intent.getAction();
			Bundle eventData = intent.getExtras();
			Event event;
			try {
				event = new Event(eventData);
			} catch (Exception e) {
				TimetableLogger.error("DeviceMuteService.TaskReceiver.onReceive: unable to parse event. " + e.getMessage());
				return;
			}
			TimetableLogger.log("DeviceMuteService.TaskReceiver: action " + action + " received with event " + event.getName() + ", id " + Integer.toString(event.getId()));
			
			if (action.equals(BroadcastActions.ACTION_EVENT_STARTED) && event.mutesDevice() && !currentEvents.contains(event)) {
				boolean deviceIsMuted = isMuted();
				if (currentEvents.isEmpty() || !deviceIsMuted) {
					if (deviceIsMuted) {
						deviceWasMuted = true; 
					} else {
						deviceWasMuted = false;
						muteDevice();
					}
				}
				currentEvents.add(event);
			} else if (action.equals(BroadcastActions.ACTION_EVENT_ENDED) && event.mutesDevice() && currentEvents.contains(event)
						//event is updated
						|| action.equals(BroadcastActions.ACTION_EVENT_STARTED) && currentEvents.contains(event) && !event.mutesDevice()) {
				currentEvents.remove(event);
				if (currentEvents.isEmpty()) {
					if (!deviceWasMuted) {
						unmuteDevice();
					}
				}
			}
		}
	}
}
