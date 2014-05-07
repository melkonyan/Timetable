package com.timetable.app;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;

/*
 * Activity, that is created when event alarm is fired. Contains the single button, that allows user to disable alarm.
 * When alarm is disabled, it is deleted from the database.
 */
public class EventAlarmDialogActivity extends Activity {
	
	public static final int DEFAULT_ALARM_SOUND = R.raw.new_gitar;
	private TimetableDatabase db;
	
	private EventAlarm eventAlarm;
	
	private EventAlarmManager eventAlarmManager;
	
	private MediaPlayer mediaPlayer;
	
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    
	    db = new TimetableDatabase(this);
		eventAlarmManager = new EventAlarmManager(this);
		eventAlarm = getEventAlarmFromIntent();
		Event event = db.searchEventById(eventAlarm.eventId);
	    mediaPlayer = MediaPlayer.create(this, DEFAULT_ALARM_SOUND);
		try {
			mediaPlayer.prepare();
		} catch (Exception e) {
			TimetableLogger.log("EventAlarmDialogActivity: Could not play alarm sound");
		}
	    mediaPlayer.setLooping(true);
		mediaPlayer.start();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setPositiveButton("Dismiss", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				eventAlarmManager.deleteAlarm(eventAlarm);
				mediaPlayer.stop();
				mediaPlayer.release();
				EventAlarmDialogActivity.this.finish();
				
			}
		});
	    builder.setTitle(event.name).create().show();
	    
	}
	
	private EventAlarm getEventAlarmFromIntent() {
		int alarmId = getIntent().getExtras().getInt(EventAlarmManager.EXTRA_ALARM_ID_STRING);
		return db.searchEventAlarmById(alarmId);
	}
}