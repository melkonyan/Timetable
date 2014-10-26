package com.timetable.android.alarm;

import java.io.File;
import java.text.ParseException;
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
import com.timetable.android.Logger;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.activities.SettingsActivity;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.Utils;

/*
 * Activity, that is created when alarm is fired. Contains the single button, that allows user to disable alarm.
 * When alarm is disabled, it is recreated on it's next occurrence or deleted.
 */
public class AlarmDialogActivity extends Activity {
	
	public static boolean checkEvent(Context context, Bundle eventData) {
		Event event;
		try {
			event = new Event(eventData);
		} catch (Exception e) {
			Logger.error("AlarmDialogActivity.checkEvent: unable to create mEvent from received data. " + e.getMessage());
			return false;
			
		}
		
		TimetableDatabase db = TimetableDatabase.getInstance(context);
		
		if (!db.existsEvent(event)) {
			Logger.error("AlarmDialogActivity.checkEvent: mEvent, that is not in the database received.");
			return false;
		}
		
		return true;
		
	}
	public static final int DEFAULT_ALARM_SOUND = R.raw.new_gitar;
	
	//Time, that activity should run, until it will be automatically killed.
	private static final long TIME_TO_RUN_MILLIS = 3 * DateUtils.MINUTE_MILLIS;
	
	private static final SimpleDateFormat TITLE_TIME_FORMAT = DateFormatFactory.getTimeFormat();
	
	private Event mEvent;
	
	private Bundle mEventData;
	
	private MediaPlayer mMediaPlayer;
	
	private PowerManager.WakeLock mWakeLock;
	
	private KeyguardManager.KeyguardLock mKeyguardLock;
	
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
			Logger.log("AlarmDialogActivity.autoKill: user has not dissmised alarm. Stopping alarm self.");
			snooze();
			AlarmDialogActivity.this.finish();
		}
	};
	
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    Logger.log("AlarmDialogActivity.onCreate: creating activity.");
	    
	    //remove title
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
	    setContentView(R.layout.activity_alarm_alert);
		
		try {
			mEventData = getIntent().getExtras();
			mEvent = new Event(mEventData);
		} catch (ParseException e) {
			Logger.error("AlarmDialogActivity.onCreate: mEvent, that is not in the database received.");
		}
		
		turnOnScreen();
		startPlayer();
        TextView alertTitle = (TextView) findViewById(R.id.alert_title);
	    Button dismissButton = (Button) findViewById(R.id.alert_button_dismiss);
	    Button snoozeButton = (Button) findViewById(R.id.alert_button_snooze);
	    
	    TextView eventNameText = (TextView) findViewById(R.id.alert_event_name);
	    
	    
		autoKiller.postDelayed(autoKill, TIME_TO_RUN_MILLIS);
		
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
		eventNameText.setText(mEvent.getName());
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
		Logger.log("AlarmDialogActivity.onRestart: restarting activity.");
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		if (!isDismissed && !isSnoozed && ok) {
			snooze();
			turnOffScreen();
		}
	}
	
	void turnOnScreen() {
		 	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		 	mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
	                | PowerManager.ON_AFTER_RELEASE, "My Tag");
	        mWakeLock.acquire();
	        
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
		    mKeyguardLock = km.newKeyguardLock("MyKeyguardLock");
		    mKeyguardLock.disableKeyguard();
		    
	}
	
	void turnOffScreen() {
		try {
			if (mWakeLock != null) {
				mWakeLock.release();
			}
			if (mKeyguardLock != null) {
				mKeyguardLock.reenableKeyguard();
			}
		} catch (Exception e) {
			//ignore
		}
	}
	
	void startPlayer() {
		mMediaPlayer = new MediaPlayer();
		try {
			String songFileString = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.ALARM_SOUND_KEY, AlarmSoundPreference.DEFAULT_ALARM_SOUND);
			
			if (songFileString.equals(AlarmSoundPreference.DEFAULT_ALARM_SOUND) || !(new File(songFileString).exists())) {
				mMediaPlayer = AlarmSoundPreference.getDefaulPlayer(this);
			} else {
				mMediaPlayer.setDataSource(songFileString);
				mMediaPlayer.prepare();
			}
		} catch (Exception e) {
			Logger.error("AlarmDialogActivity: Could not play alarm sound " + e.getMessage());
		}
		
	    mMediaPlayer.setLooping(true);
		mMediaPlayer.start();
		
	}
	
	void stopPlayer() {
		try {
			mMediaPlayer.stop();
			mMediaPlayer.release();
		} catch (Exception e) {
			//ignore.
		}
	}
	
	/*
	 * Snooze alarm.
	 */
	private void snooze() {
		Logger.log("AlarmDialogActity.snooze: snoozing alarm.");
		stopPlayer();
		isSnoozed = true;
		Intent broadcast = new Intent(AlarmService.ACTION_ALARM_SNOOZED);
		broadcast.putExtras(mEventData);
		sendBroadcast(broadcast);
	}
	
	/*
	 * Dismiss alarm.
	 */
	private void dismiss() {
		Logger.log("AlarmDialogActivity.dismiss: dismissing alarm.");
		
		stopPlayer();
		
		isDismissed = true;
		
		Intent broadcast = new Intent(AlarmService.ACTION_ALARM_DISMISSED);
		broadcast.putExtras(mEventData);
		sendBroadcast(broadcast);
	
		Intent intent = new Intent(AlarmDialogActivity.this, EventDayViewActivity.class);
		intent.putExtra(EventDayViewActivity.EXTRAS_DATE, 
						EventDayViewActivity.EXTRAS_DATE_FORMAT.format(mEvent.getAlarm().getEventOccurrence(Utils.getCurrDateTime())));
		
		AlarmDialogActivity.this.startActivity(intent);
		
	}
	
	
}
