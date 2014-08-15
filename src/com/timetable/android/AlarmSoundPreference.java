package com.timetable.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.preference.ListPreference;
import android.provider.MediaStore;
import android.util.AttributeSet;

/*
 * Class, that allows user to choose alarm sound. Contains a list of all available sounds on device's SD card.
 * When user chooses sound in the list, it is played by MediaPlayer.
 * Than the preference is stored and used by AlarmDialogActivity, to play choosed sound.
 */
public class AlarmSoundPreference extends ListPreference {

	public static final String DEFAULT_ALARM_SOUND = "default_sound";
	
	public static final MediaPlayer getDefaulPlayer(Context context) {
		return MediaPlayer.create(context, R.raw.new_gitar);
	}
	
	//Activity, that has started preference.
	private Activity activity;
	
	private String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

	private String[] projection = {
	        MediaStore.Audio.Media.TITLE,
	        MediaStore.Audio.Media.DATA
	};
	
	private int mClickedDialogEntryIndex;
	
	MediaPlayer mPlayer = new MediaPlayer();
	
	ArrayList<String> songNames = new ArrayList<String>();
	ArrayList<String> songFiles = new ArrayList<String>();

	public AlarmSoundPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (Activity) context;
		Cursor cursor = activity.managedQuery(
		        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
		        projection,
		        selection,
		        null,
		        null);

		songNames.add("Default");
		songFiles.add(DEFAULT_ALARM_SOUND);
		if (cursor != null) {
			cursor.moveToFirst();
			while(cursor.moveToNext()) {
				songNames.add(cursor.getString(0));
				songFiles.add(cursor.getString(1));
			}
		}
	}
	
	public AlarmSoundPreference(Context context) {
		super(context);
		TimetableLogger.log("AlarmSouncPreference.onCreate(): preference is created.");
	}
	
	
	@Override
	public CharSequence[] getEntryValues() {
		return songFiles.toArray(new String[0]);
	}
	
	@Override
	public CharSequence[] getEntries() {
		return songNames.toArray(new String[0]);
	}
	
	@Override 
	protected void onPrepareDialogBuilder(Builder builder) {
		setEntries(getEntries());
		setEntryValues(getEntryValues());
		
	    mClickedDialogEntryIndex = getValueIndex();
	    	builder.setSingleChoiceItems(getEntries(), mClickedDialogEntryIndex, 
	    			new DialogInterface.OnClickListener() {
	                	public void onClick(DialogInterface dialog, int which) {
	                		mClickedDialogEntryIndex = which;
	                        //try to play selected sound
	                		if (mPlayer.isPlaying()) {
	                			mPlayer.reset();
	                		}	
	                		TimetableLogger.error("AlarmSoundPreference.onItemtSelected");
		                 	try {
		                 		mPlayer = new MediaPlayer();
		             	        String filename = songFiles.get(which);
		             	        if (filename == null || filename.equals(DEFAULT_ALARM_SOUND)) {
		             	        	mPlayer.release();
		             	        	mPlayer = getDefaulPlayer(activity);
		             	        } else {
		             	        	mPlayer.setDataSource(filename);
			                 		mPlayer.prepare();
		             	        }
		             	        
		             	        mPlayer.start();
		                 	} catch (Exception e) {
		                 		TimetableLogger.error("AlarmSoundPreference.onItemClick: " + e.toString());
		                 	}
	                	}
	         });
	    builder.setPositiveButton("Okey", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				AlarmSoundPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
				dialog.dismiss();
			}
		
	    });	
	}
	
	private int getValueIndex() {
		return findIndexOfValue(getValue());
	}
	
	@Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        TimetableLogger.error("Dialog is closed. PositiveResult: " + Boolean.toString(positiveResult));
        mPlayer.stop();
        if (positiveResult) {
            String value = songFiles.get(mClickedDialogEntryIndex);
            callChangeListener(value);
            TimetableLogger.error("Update alarm sound preference");
            setValue(value);
        }
    }

}
