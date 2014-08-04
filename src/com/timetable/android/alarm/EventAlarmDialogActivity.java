package com.timetable.android.alarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.utils.TimetableUtils;

/*
 * Activity, that is created when event alarm is fired. Contains the single button, that allows user to disable alarm.
 * When alarm is disabled, it is deleted from the database.
 */
public class EventAlarmDialogActivity extends Activity {
	
	public static final int DEFAULT_ALARM_SOUND = R.raw.new_gitar;
	private TimetableDatabase db;
	
	private EventAlarm alarm;
	
	private AlarmServiceManager mManager;
	
	private MediaPlayer mediaPlayer;
	
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    
	    db = TimetableDatabase.getInstance(this);
		alarm = getEventAlarmFromIntent();
		alarm.event = db.searchEventById(alarm.eventId);
		if (db.isException(alarm.event, alarm.getEventOccurrence(TimetableUtils.getCurrentTime()))) {
			finish();
		}
		mManager = new AlarmServiceManager(this);
		mManager.bindService();
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
				AlarmService alarmService = mManager.getService();
				alarmService.updateAlarm(alarm);
				mediaPlayer.stop();
				mediaPlayer.release();
				Intent intent = new Intent(EventAlarmDialogActivity.this, EventDayViewActivity.class);
				intent.putExtra(EventDayViewActivity.EXTRAS_DATE, 
								EventDayViewActivity.EXTRAS_DATE_FORMAT.format(alarm.getEventOccurrence(TimetableUtils.getCurrentTime())));
				EventAlarmDialogActivity.this.startActivity(intent);
				EventAlarmDialogActivity.this.finish();
				
			}
		});
	    builder.setTitle(alarm.event.name).create().show();
	    
	}
	
	private EventAlarm getEventAlarmFromIntent() {
		int alarmId = getIntent().getExtras().getInt(AlarmService.EXTRA_ALARM_ID_STRING);
		return db.searchEventAlarmById(alarmId);
	}
}