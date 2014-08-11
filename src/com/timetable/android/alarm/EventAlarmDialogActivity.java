package com.timetable.android.alarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.TimetableLogger;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.utils.TimetableUtils;

/*
 * Activity, that is created when event alarm is fired. Contains the single button, that allows user to disable alarm.
 * When alarm is disabled, it is deleted from the database.
 */
public class EventAlarmDialogActivity extends Activity {
	
	public static final int DEFAULT_ALARM_SOUND = R.raw.new_gitar;
	
	private Event event;
	
	private Bundle eventData;
	
	private MediaPlayer mediaPlayer;
	
	private boolean ok = true;
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    
	    eventData = getIntent().getExtras();
		if (eventData == null) {
			TimetableLogger.error("EventAlarmDialogActiovity.onCreate: intent with no data received");
			return;
		}
		
		try {
			event = new Event(eventData);
		} catch (Exception e) {
			TimetableLogger.error("EventAlarmDialogActivity.onReceive: unable to create event from received data. " + e.getMessage());
			ok = false;
			return;
		}
		
	    
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
				EventAlarmDialogActivity.this.finish();
				
			}
		});
	    builder.setTitle(event.name).create().show();
	    
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		if (!ok) {
			return;
		}
		
		Intent broadcast = new Intent(AlarmService.ACTION_ALARM_UPDATED);
		broadcast.putExtras(eventData);
		sendBroadcast(broadcast);
	
		Intent intent = new Intent(EventAlarmDialogActivity.this, EventDayViewActivity.class);
		intent.putExtra(EventDayViewActivity.EXTRAS_DATE, 
						EventDayViewActivity.EXTRAS_DATE_FORMAT.format(event.alarm.getEventOccurrence(TimetableUtils.getCurrentTime())));
		EventAlarmDialogActivity.this.startActivity(intent);
		
	}
	
}