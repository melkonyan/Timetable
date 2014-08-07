package com.timetable.android;

import java.text.SimpleDateFormat;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class EventView extends RelativeLayout {

	public static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("HH:mm");
	
	
	public Event event;
	
	public EventView(Context context, Event event) {
		super(context);
		this.event = event;
		LayoutInflater layoutInflater =  LayoutInflater.from(context);	
		layoutInflater.inflate(R.layout.layout_event, this, true);
		TextView textViewEventId = (TextView) findViewById(R.id.layout_event_id);
		TextView textViewEventName = (TextView) findViewById(R.id.layout_event_name); 
		TextView textViewEventPlace = (TextView) findViewById(R.id.layout_event_place); 
		TextView textViewEventNote = (TextView) findViewById(R.id.layout_event_note); 
		TextView textViewEventStartTime = (TextView) findViewById(R.id.layout_event_start_time);
		TextView textViewEventEndTime = (TextView) findViewById(R.id.layout_event_end_time);
		ImageView imageRepeat = (ImageView) findViewById(R.id.layout_event_image_repeat);
		ImageView imageAlarm = (ImageView) findViewById(R.id.layout_event_image_alarm);
		ImageView imageMuteDevice = (ImageView) findViewById(R.id.layout_event_image_mute_device);
		
		textViewEventId.setText(Integer.toString(event.id));
		textViewEventName.setText(event.name);
		textViewEventPlace.setText(event.place);
		textViewEventNote.setText(event.note);
		textViewEventStartTime.setText(START_TIME_FORMAT.format(event.startTime));
		if (event.endTime != null) {
			textViewEventEndTime.setText("- " + START_TIME_FORMAT.format(event.endTime));
		}
		else {
			textViewEventEndTime.setText("");
		}
		
		imageRepeat.setVisibility(event.isRepeatable() ? View.VISIBLE : View.INVISIBLE);
		imageAlarm.setVisibility(event.hasAlarm() ? View.VISIBLE : View.INVISIBLE);
		imageMuteDevice.setVisibility(event.muteDevice ? View.VISIBLE : View.INVISIBLE);
		
		TimetableLogger.log("event " + event.id + " successfully drawed");
		
		this.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				TimetableLogger.log("EventView: click performed");
			}
		});
		
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
	    if(event.getAction() == MotionEvent.ACTION_UP) {
	    	TimetableLogger.log("EventView: click performed");
	    }
	    return super.dispatchTouchEvent(event);
	}

}
