package com.timetable.android;

import java.util.ArrayList;

import org.holoeverywhere.app.AlertDialog.Builder;
import org.holoeverywhere.preference.ListPreference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;

/*
 * Class, that allows user to choose alarm sound. Contains a list of all available sounds on device's SD card.
 * When user chooses sound in the list, it is played by MediaPlayer.
 * Than the preference is stored and used by AlarmDialogActivity, to play chosen sound.
 */
public class AlarmSoundPreference extends ListPreference {

	public static final String DEFAULT_ALARM_SOUND = "default_sound";
	
	public static final MediaPlayer getDefaulPlayer(Context context) {
		return MediaPlayer.create(context, R.raw.new_gitar);
	}
	
	//Activity, that has started preference.
	private Context mContext;
	
	private static final long MINIMAL_SOUND_DURATION = 1000L;
	
	private String selection = String.format(" %s != 0 and %s > %d", 
												MediaStore.Audio.Media.IS_MUSIC, 
												MediaStore.Audio.Media.DURATION,
												MINIMAL_SOUND_DURATION);
	private String order = MediaStore.Audio.Media.TITLE;
	
	private String[] projection = {
	        MediaStore.Audio.Media.TITLE,
	        MediaStore.Audio.Media.DATA
	};
	
	private Resources mResources;
	
	private int mClickedDialogEntryIndex;
	
	MediaPlayer mPlayer = new MediaPlayer();
	
	ArrayList<String> songNames = new ArrayList<String>();
	ArrayList<String> songFiles = new ArrayList<String>();

	private void loadMedia(Uri uri) {
		Cursor cursor = mContext.getContentResolver().query(
		        uri,
				projection,
		        selection,
		        null,
		        order);
		if (cursor != null) {
			cursor.moveToFirst();
			while(cursor.moveToNext()) {
				songNames.add(cursor.getString(0));
				songFiles.add(cursor.getString(1));
			}
		}
	

	}
	public AlarmSoundPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mResources = mContext.getResources();
		//Add default ringtone.
		songNames.add("Default");
		songFiles.add(DEFAULT_ALARM_SOUND);
		//Add media from SD card
		loadMedia(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
		//Add media from phone.
		loadMedia(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
	}
	
	public AlarmSoundPreference(Context context) {
		super(context);
		Logger.log("AlarmSouncPreference.onCreate(): preference is created.");
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
	                		try {
		                 		mPlayer = new MediaPlayer();
		             	        String filename = songFiles.get(which);
		             	        if (filename == null || filename.equals(DEFAULT_ALARM_SOUND)) {
		             	        	mPlayer.release();
		             	        	mPlayer = getDefaulPlayer(mContext);
		             	        } else {
		             	        	mPlayer.setDataSource(filename);
			                 		mPlayer.prepare();
		             	        }
		             	        
		             	        mPlayer.start();
		                 	} catch (Exception e) {
		                 		Logger.error("AlarmSoundPreference.onItemClick: " + e.toString());
		                 	}
	                	}
	         });
	    builder.setPositiveButton(mResources.getString(R.string.alarm_sound_preference_dialog_button_positive), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				AlarmSoundPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
				dialog.dismiss();
			}
		
	    });	
	    
	    builder.setNegativeButton(mResources.getString(R.string.alarm_sound_preference_dialog_button_negative), null);
	}
	
	private int getValueIndex() {
		return findIndexOfValue(getValue());
	}
	
	@Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        mPlayer.stop();
        if (positiveResult) {
            if (mClickedDialogEntryIndex < 0) {
            	return;
            }
        	String value = songFiles.get(mClickedDialogEntryIndex);
            callChangeListener(value);
            setValue(value);
        }
    }

}
