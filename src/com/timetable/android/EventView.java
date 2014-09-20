package com.timetable.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.timetable.android.utils.DateFormatFactory;

/*
 * Class, that represents mEvent's information. Is used in EventDayViewActivity.
 */
public class EventView extends RelativeLayout {

	public static final SimpleDateFormat START_TIME_FORMAT = DateFormatFactory.getFormat("HH:mm");
	
	private Event mEvent;
	
	private Context mContext; 
	
	private Date mDisplayDate;
	
	private LinearLayout mEventContainer;
	
	private RelativeLayout mEventInfoContainer;
	 
	private LinearLayout mMenuContainer;
	
	private EventViewObserver mObserver;
	
	
	/*
	 * Constructor for EventView class.
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
		mEventContainer = (LinearLayout) findViewById(R.id.layout_event_container);
		mEventInfoContainer = (RelativeLayout) findViewById(R.id.layout_event_info_container);
		
		mMenuContainer = (LinearLayout) findViewById(R.id.layout_event_buttons_container);
		
		TextView textViewEventId = (TextView) findViewById(R.id.layout_event_id);
		TextView textViewEventName = (TextView) findViewById(R.id.layout_event_name); 
		TextView textViewEventPlace = (TextView) findViewById(R.id.layout_event_place); 
		TextView textViewEventNote = (TextView) findViewById(R.id.layout_event_note); 
		TextView textViewEventStartTime = (TextView) findViewById(R.id.layout_event_start_time);
		TextView textViewEventEndTime = (TextView) findViewById(R.id.layout_event_end_time);
		ImageView imageRepeat = (ImageView) findViewById(R.id.layout_event_image_repeat);
		ImageView imageAlarm = (ImageView) findViewById(R.id.layout_event_image_alarm);
		ImageView imageMuteDevice = (ImageView) findViewById(R.id.layout_event_image_mute_device);
		
		LinearLayout buttonCopy = (LinearLayout) findViewById(R.id.layout_event_button_copy);
		LinearLayout buttonEdit = (LinearLayout) findViewById(R.id.layout_event_button_edit);
		LinearLayout buttonDelete = (LinearLayout) findViewById(R.id.layout_event_button_delete);
		
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
		
		mMenuContainer.setVisibility(View.GONE);
		imageRepeat.setVisibility(event.isRepeatable() ? View.VISIBLE : View.INVISIBLE);
		imageAlarm.setVisibility(event.hasAlarm() ? View.VISIBLE : View.INVISIBLE);
		imageMuteDevice.setVisibility(event.mutesDevice() ? View.VISIBLE : View.INVISIBLE);
		
		TimetableLogger.verbose("Event " + event.getName() + " successfully drawed");
		
		buttonEdit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				TimetableLogger.verbose("EventView: edit button clicked");
				if (mObserver != null) {
					mObserver.onButtonEditClicked(EventView.this);
				}
			}
		});
		
		buttonCopy.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.verbose("EventView: copy button clicked.");
				if (mObserver != null) {
					mObserver.onButtonCopyClicked(EventView.this);
				}
			}
		});
		
		buttonDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.verbose("EventView: delete button clicked.");
				if (mObserver != null) {
					mObserver.onButtonDeleteClicked(EventView.this);
				}
			}
		});
		
		mEventInfoContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.verbose("EventView: event clicked.");
				if (mObserver != null) {
					mObserver.onEventViewClicked(EventView.this);
				}
			}
		});
		
		mEventInfoContainer.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				if (mObserver != null) {
					mObserver.onEventViewLongClicked(EventView.this);
					return true;
				}
				return false;
			}
		});
	}
	
	public Event getEvent() {
		return mEvent;
	}
	
	public void showMenu() {
		mMenuContainer.setVisibility(View.VISIBLE);
	}
	
	public void hideMenu() {
		mMenuContainer.setVisibility(View.GONE);
	}
	
	public void setEventViewObserver(EventViewObserver observer) {
		mObserver = observer;
	}
	
	public static interface EventViewObserver {
		
		public void onEventViewClicked(EventView eventView);
		
		public void onEventViewLongClicked(EventView eventView);
		
		public void onButtonDeleteClicked(EventView eventView);
		
		public void onButtonEditClicked(EventView eventView);
		
		public void onButtonCopyClicked(EventView eventView);
	}
}
