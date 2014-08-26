package com.timetable.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.timetable.android.activities.EventEditActivity;
import com.timetable.android.utils.DateFormatFactory;

/*
 * Class, that represents mEvent's information. Is used in EventDayViewActivity.
 */
public class EventView extends RelativeLayout {

	public static final SimpleDateFormat START_TIME_FORMAT = DateFormatFactory.getFormat("HH:mm");
	
	private Event mEvent;
	
	private Context mContext; 
	
	private Date mDisplayDate;
	/*
	 * Constuctor for EventView class.
	 * @mEvent - mEvent to display.
	 * @displayDate - date, on which mEvent is displayed.
	 */
	public EventView(Context context, Event event, Date displayDate) {
		super(context);
		mEvent = event;
		mContext = context;
		mDisplayDate = displayDate;
		LayoutInflater layoutInflater =  LayoutInflater.from(context);	
		layoutInflater.inflate(R.layout.layout_event, this, true);
		RelativeLayout mContainer = (RelativeLayout) findViewById(R.id.layout_event_container);
		TextView textViewEventId = (TextView) findViewById(R.id.layout_event_id);
		TextView textViewEventName = (TextView) findViewById(R.id.layout_event_name); 
		TextView textViewEventPlace = (TextView) findViewById(R.id.layout_event_place); 
		TextView textViewEventNote = (TextView) findViewById(R.id.layout_event_note); 
		TextView textViewEventStartTime = (TextView) findViewById(R.id.layout_event_start_time);
		TextView textViewEventEndTime = (TextView) findViewById(R.id.layout_event_end_time);
		ImageView imageRepeat = (ImageView) findViewById(R.id.layout_event_image_repeat);
		ImageView imageAlarm = (ImageView) findViewById(R.id.layout_event_image_alarm);
		ImageView imageMuteDevice = (ImageView) findViewById(R.id.layout_event_image_mute_device);
		
		textViewEventId.setText(Integer.toString(event.getId()));
		textViewEventName.setText(event.getName());
		textViewEventPlace.setText(event.getPlace());
		textViewEventNote.setText(event.getNote());
		textViewEventStartTime.setText(START_TIME_FORMAT.format(event.getStartTime()));
		
		if (event.hasEndTime()) {
			textViewEventEndTime.setText("- " + START_TIME_FORMAT.format(event.getEndTime()));
		}
		else {
			textViewEventEndTime.setText("");
		}
		
		imageRepeat.setVisibility(event.isRepeatable() ? View.VISIBLE : View.INVISIBLE);
		imageAlarm.setVisibility(event.hasAlarm() ? View.VISIBLE : View.INVISIBLE);
		imageMuteDevice.setVisibility(event.mutesDevice() ? View.VISIBLE : View.INVISIBLE);
		
		TimetableLogger.verbose("Event " + event.getName() + " successfully drawed");
		
		mContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				TimetableLogger.log("EventView: click performed");
				Intent eventEditIntent = new Intent(mContext, EventEditActivity.class);
				eventEditIntent.putExtra(EventEditActivity.EXTRA_EVENT_ID, EventView.this.mEvent.getId());
				//TODO: put date's millis into extra, instead of formatting and then parsing date string 
				eventEditIntent.putExtra(EventEditActivity.EXTRA_DATE, EventEditActivity.INIT_DATE_FORMAT.format(mDisplayDate));
				mContext.startActivity(eventEditIntent);
			
			}
		});
		
	}
}
