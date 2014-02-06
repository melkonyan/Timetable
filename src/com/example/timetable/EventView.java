package com.example.timetable;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EventView extends RelativeLayout {

	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	
	public Event event;
	
	public EventView(Context context, Event event) {
		super(context);
		this.event = event;
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );	
		layoutInflater.inflate(R.layout.layout_event, this, true);
		//TODO: fix onclick(edit page is not showed)
		TextView textViewEventId = (TextView) findViewById(R.id.layout_event_id);
		TextView textViewEventName = (TextView) findViewById(R.id.layout_event_name); 
		TextView textViewEventPlace = (TextView) findViewById(R.id.layout_event_place); 
		TextView textViewEventNote = (TextView) findViewById(R.id.layout_event_note); 
		TextView textViewEventStartTime = (TextView) findViewById(R.id.layout_event_start_time);
		TextView textViewEventEndTime = (TextView) findViewById(R.id.layout_event_end_time);
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
		TimetableLogger.log("event " + event.id + " successfully drawed");
	}
	
}
