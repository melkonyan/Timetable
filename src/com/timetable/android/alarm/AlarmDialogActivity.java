package com.timetable.android.alarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

import com.timetable.android.AlarmSoundPreference;
import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.TimetableLogger;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.activities.SettingsActivity;
import com.timetable.android.utils.TimetableUtils;

/*
 * Activity, that is created when event alarm is fired. Contains the single button, that allows user to disable alarm.
 * When alarm is disabled, it is recreated on it's next occurrence of deleted.
 */
public class AlarmDialogActivity extends Activity {
	
	public static final int DEFAULT_ALARM_SOUND = R.raw.new_gitar;
	
	//Time, that activity should run, until it will be automatically killed.
	private static final int TIME_TO_RUN_MILLIS = 3*60*1000;
	
	private Event event;
	
	private Bundle eventData;
	
	private MediaPlayer mediaPlayer;
	
	private boolean ok = true;
	
	private WakeLock screenLock;
	//handler, that will finish activity after a given amount of time
	private Handler autoKiller = new Handler();
	
	private Runnable autoKill = new Runnable() {
		
		@Override
		public void run() {
			AlarmDialogActivity.this.finish();
		}
	};
	
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    TimetableLogger.log("AlarmDialogActivity.onCreate: creating activity.");
	    eventData = getIntent().getExtras();
		if (eventData == null) {
			TimetableLogger.error("EventAlarmDialogActiovity.onCreate: intent with no data received");
			return;
		}
		
		try {
			event = new Event(eventData);
		} catch (Exception e) {
			TimetableLogger.error("AlarmDialogActivity.onReceive: unable to create event from received data. " + e.getMessage());
			ok = false;
			return;
		}
		
		PowerManager pm = ((PowerManager) getSystemService(POWER_SERVICE));
		screenLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
		screenLock.acquire();

		
		autoKiller.postDelayed(autoKill, TIME_TO_RUN_MILLIS);
		
		mediaPlayer = new MediaPlayer();
		try {
			String songFileString = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.ALARM_SOUND_KEY, AlarmSoundPreference.DEFAULT_ALARM_SOUND);
			TimetableLogger.error(songFileString);
			if (songFileString.equals(AlarmSoundPreference.DEFAULT_ALARM_SOUND)) {
				mediaPlayer = AlarmSoundPreference.getDefaulPlayer(this);
			} else {
				mediaPlayer.setDataSource(songFileString);
				mediaPlayer.prepare();
			}
		} catch (Exception e) {
			TimetableLogger.error("AlarmDialogActivity: Could not play alarm sound " + e.getMessage());
		}
		
	    mediaPlayer.setLooping(true);
		mediaPlayer.start();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setPositiveButton("Dismiss", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlarmDialogActivity.this.finish();
				
			}
		});
	    builder.setTitle(event.name).create().show();
	}
	@Override 
	public void onResume() {
		super.onResume();
		Window window = getWindow();
	    window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	    window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	    window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	@Override 
	public void onStop() {
		super.onStop();
		if (!ok) {
			return;
		}
		if(screenLock.isHeld()) {
		    screenLock.release();
		}
		
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		
		
		Intent broadcast = new Intent(AlarmService.ACTION_ALARM_UPDATED);
		broadcast.putExtras(eventData);
		sendBroadcast(broadcast);
	
		Intent intent = new Intent(AlarmDialogActivity.this, EventDayViewActivity.class);
		intent.putExtra(EventDayViewActivity.EXTRAS_DATE, 
						EventDayViewActivity.EXTRAS_DATE_FORMAT.format(event.alarm.getEventOccurrence(TimetableUtils.getCurrentTime())));
		AlarmDialogActivity.this.startActivity(intent);
		finish();
	}
	
}