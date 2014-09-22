package com.timetable.android.alarm;

import java.io.File;
import java.text.SimpleDateFormat;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.TextView;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;

import com.timetable.android.AlarmSoundPreference;
import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.activities.SettingsActivity;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.Utils;

/*
 * Activity, that is created when alarm is fired. Contains the single button, that allows user to disable alarm.
 * When alarm is disabled, it is recreated on it's next occurrence or deleted.
 */
public class AlarmDialogActivity extends Activity {
	
	public static final int DEFAULT_ALARM_SOUND = R.raw.new_gitar;
	
	//Time, that activity should run, until it will be automatically killed.
	private static final int TIME_TO_RUN_MILLIS = 3*60*1000;
	
	private static final SimpleDateFormat TITLE_TIME_FORMAT = DateFormatFactory.getTimeFormat();
	
	private Event event;
	
	private Bundle eventData;
	
	private MediaPlayer mediaPlayer;
	
	private boolean ok = true;
	
	//flag, that indicates weather alarm is dismissed by user.
	private boolean isDismissed = false;
	
	//handler, that will finish activity after a given amount of time.
	private Handler autoKiller = new Handler();
	
	private Runnable autoKill = new Runnable() {
		
		@Override
		public void run() {
			TimetableLogger.log("AlarmDialogActivity.autoKill: user has not dissmised alarm. Stopping alarm self.");
			dismiss();
			AlarmDialogActivity.this.finish();
		}
	};
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    TimetableLogger.log("AlarmDialogActivity.onCreate: creating activity.");
	    //remove title
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
	    setContentView(R.layout.activity_alarm_alert);
	    
	    TextView alertTitle = (TextView) findViewById(R.id.alert_title);
	    Button dismissButton = (Button) findViewById(R.id.alert_button_dismiss);
	    TextView eventNameText = (TextView) findViewById(R.id.alert_event_name);
	    
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
		
		dismissButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				AlarmDialogActivity.this.finish();
			}
		});
		
		alertTitle.setText(TITLE_TIME_FORMAT.format(Utils.getCurrDateTime()) + " Reminder");
		eventNameText.setText(event.getName());
	 
	}
	
	@Override
	public void onUserLeaveHint() {
		super.onUserLeaveHint();
		if (!isDismissed) {
			dismiss();
			finish();
		}
	}
	
	@Override 
	public void onRestart() {
		super.onRestart();
		TimetableLogger.log("AlarmDialogActivity.onRestart: restarting activity.");
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		if (!isDismissed && ok) {
			dismiss();
		}
	}
	
	/*
	 * Dismiss alarm.
	 */
	private void dismiss() {
		TimetableLogger.log("AlarmDialogActivity.dismiss: dismissing alarm.");
		
		try {
			mediaPlayer.stop();
			mediaPlayer.release();
		} catch (Exception e) {
			//ignore.
		}
		
		isDismissed = true;
		
		Intent broadcast = new Intent(AlarmService.ACTION_ALARM_UPDATED);
		broadcast.putExtras(eventData);
		sendBroadcast(broadcast);
	
		Intent intent = new Intent(AlarmDialogActivity.this, EventDayViewActivity.class);
		intent.putExtra(EventDayViewActivity.EXTRAS_DATE, 
						EventDayViewActivity.EXTRAS_DATE_FORMAT.format(event.getAlarm().getEventOccurrence(Utils.getCurrDateTime())));
		
		AlarmDialogActivity.this.startActivity(intent);
		
	}
	
	
}
