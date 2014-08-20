package com.timetable.android.alarm;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.timetable.android.AlarmSoundPreference;
import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.activities.SettingsActivity;
import com.timetable.android.utils.Utils;

/*
 * Activity, that is created when alarm is fired. Contains the single button, that allows user to disable alarm.
 * When alarm is disabled, it is recreated on it's next occurrence or deleted.
 */
public class AlarmDialogActivity extends Activity {
	
	public static final int DEFAULT_ALARM_SOUND = R.raw.new_gitar;
	
	//Time, that activity should run, until it will be automatically killed.
	private static final int TIME_TO_RUN_MILLIS = 3*60*1000;
	
	private Event event;
	
	private Bundle eventData;
	
	private MediaPlayer mediaPlayer;
	
	private boolean ok = true;
	
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
		
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		
		if (!db.existsEvent(event)) {
			TimetableLogger.error("AlarmDialogActivity.onReceive: event, that is not in the database received.");
			ok = false;
			return;
		}
		
		autoKiller.postDelayed(autoKill, TIME_TO_RUN_MILLIS);
		
		mediaPlayer = new MediaPlayer();
		try {
			String songFileString = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.ALARM_SOUND_KEY, AlarmSoundPreference.DEFAULT_ALARM_SOUND);
			
			if (songFileString.equals(AlarmSoundPreference.DEFAULT_ALARM_SOUND) || !(new File(songFileString).exists())) {
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
	    builder.setTitle(event.getName()).create().show();
	}

	
	@Override 
	public void onStop() {
		super.onStop();
		if (!ok) {
			return;
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
						EventDayViewActivity.EXTRAS_DATE_FORMAT.format(event.getAlarm().getEventOccurrence(Utils.getCurrDateTime())));
		AlarmDialogActivity.this.startActivity(intent);
	}
	
}