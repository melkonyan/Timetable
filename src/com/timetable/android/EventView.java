package com.timetable.android;

import java.text.SimpleDateFormat;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import com.timetable.app.R;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class EventView extends RelativeLayout {

	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	
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
		
		textViewEventId.setText(Integer.toString(event.id));
		textViewEventName.setText(event.name);
		textViewEventPlace.setText(event.place);
		textViewEventNote.setText(event.note);
		textViewEventStartTime.setText(timeFormat.format(event.startTime));
		if (event.endTime != null) {
			textViewEventEndTime.setText("- " + timeFormat.format(event.endTime));
		}
		else {
			textViewEventEndTime.setText("");
		}
		
		imageRepeat.setVisibility(event.isRepeatable() ? View.VISIBLE : View.INVISIBLE);
		imageAlarm.setVisibility(event.hasAlarm() ? View.VISIBLE : View.INVISIBLE);
		
		TimetableLogger.log("event " + event.id + " successfully drawed");
	}
	
}
