package com.timetable.android.alarm;

import java.io.File;
import java.text.SimpleDateFormat;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.TextView;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.timetable.android.AlarmSoundPreference;
import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.activities.SettingsActivity;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.TestAlarmStarter;
import com.timetable.android.utils.Utils;

/*
 * Activity, that is created when alarm is fired. Contains the single button, that allows user to disable alarm.
 * When alarm is disabled, it is recreated on it's next occurrence or deleted.
 */
public class AlarmDialogActivity extends Activity {
	
	public static final int DEFAULT_ALARM_SOUND = R.raw.new_gitar;
	
	//Time, that activity should run, until it will be automatically killed.
	private static final int TIME_TO_RUN_MILLIS = 20*1000;
	
	private static final SimpleDateFormat TITLE_TIME_FORMAT = DateFormatFactory.getTimeFormat();
	
	private Event event;
	
	private Bundle eventData;
	
	private MediaPlayer mediaPlayer;
	
	private boolean ok = true;
	
	//flag, that indicates whether alarm is dismissed by user.
	private boolean isDismissed = false;
	
	//flag, that indicates whether alarm is snoozed by user. 
	private boolean isSnoozed = false;
	
	//handler, that will finish activity after a given amount of time.
	private Handler autoKiller = new Handler();
	
	private Runnable autoKill = new Runnable() {
		
		@Override
		public void run() {
			if (isSnoozed || isDismissed) {
				return;
			}
			TimetableLogger.log("AlarmDialogActivity.autoKill: user has not dissmised alarm. Stopping alarm self.");
			snooze();
			AlarmDialogActivity.this.finish();
		}
	};
	
	void stopPlayer() {
		try {
			mediaPlayer.stop();
			mediaPlayer.release();
		} catch (Exception e) {
			//ignore.
		}
	}
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    TimetableLogger.log("AlarmDialogActivity.onCreate: creating activity.");
	    
	    //remove title
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
	    setContentView(R.layout.activity_alarm_alert);
	    
	    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "My Tag");
        wl.acquire();
        
        //turn on screen when alarm is fired.
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | 
	    	    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
	    	    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	    	    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
	    	    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
	    	    WindowManager.LayoutParams.FLAG_FULLSCREEN | 
	    	    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
	    	    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	    	    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
	    	    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
	    //disable keyguard
	    KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
	    final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
	    kl.disableKeyguard();
	    
        TextView alertTitle = (TextView) findViewById(R.id.alert_title);
	    Button dismissButton = (Button) findViewById(R.id.alert_button_dismiss);
	    Button snoozeButton = (Button) findViewById(R.id.alert_button_snooze);
	    
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
			//ok = false;
			//return;
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
		
		snoozeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				snooze();
				AlarmDialogActivity.this.finish();
			}
		});
		alertTitle.setText(TITLE_TIME_FORMAT.format(Utils.getCurrDateTime()) + " Reminder");
		eventNameText.setText(event.getName());
	}
	
	@Override
	public void onUserLeaveHint() {
		super.onUserLeaveHint();
		if (!isDismissed && !isSnoozed && ok) {
			snooze();
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
		if (!isDismissed && !isSnoozed && ok) {
			snooze();
		}
	}
	
	/*
	 * Snooze alarm.
	 */
	private void snooze() {
		TimetableLogger.log("AlarmDialogActity.snooze: snoozing alarm.");
		stopPlayer();
		isSnoozed = true;
		Intent broadcast = new Intent(AlarmService.ACTION_ALARM_SNOOZED);
		broadcast.putExtras(eventData);
		sendBroadcast(broadcast);
	}
	
	/*
	 * Dismiss alarm.
	 */
	private void dismiss() {
		TimetableLogger.log("AlarmDialogActivity.dismiss: dismissing alarm.");
		
		stopPlayer();
		
		isDismissed = true;
		
		Intent broadcast = new Intent(AlarmService.ACTION_ALARM_DISMISSED);
		broadcast.putExtras(eventData);
		sendBroadcast(broadcast);
	
		Intent intent = new Intent(AlarmDialogActivity.this, EventDayViewActivity.class);
		intent.putExtra(EventDayViewActivity.EXTRAS_DATE, 
						EventDayViewActivity.EXTRAS_DATE_FORMAT.format(event.getAlarm().getEventOccurrence(Utils.getCurrDateTime())));
		
		AlarmDialogActivity.this.startActivity(intent);
		
	}
	
	
}
