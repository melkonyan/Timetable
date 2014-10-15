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
 * Class, that represents event's information. Is used in EventDayViewActivity.
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

	private TextView mEventNameText;

	private TextView mEventPlaceText;

	private TextView mEventNoteText;

	private TextView mEventStartTimeText;

	private TextView mEventEndTimeText;

	private ImageView mIsRepeatedIcon;

	private ImageView mHasAlarmIcon;

	private ImageView mMutesDeviceIcon;

	private LinearLayout mCopyButton;

	private LinearLayout mEditButton;

	private LinearLayout mDeleteButton;
	
	
	
	/*
	 * Constructor for EventView class.
	 * @mEvent - mEvent to display.
	 * @displayDate - date, on which mEvent is displayed.
	 */
	public EventView(Context context, Event event, Date displayDate) {
		this(context);
		populate(event, displayDate);
	}
	
	
	public EventView(Context context) {
		super(context);
		inflate(context);
	}
	
	/*
	 * Inflate view from xml layout and find views.
	 */
	private void inflate(Context context) {
		mContext = context;
		
		LayoutInflater layoutInflater =  LayoutInflater.from(mContext);	
		layoutInflater.inflate(R.layout.layout_event, this, true);
		mEventContainer = (LinearLayout) findViewById(R.id.layout_event_container);
		mEventInfoContainer = (RelativeLayout) findViewById(R.id.layout_event_info_container);
		
		mMenuContainer = (LinearLayout) findViewById(R.id.layout_event_buttons_container);
		
		//TextView textViewEventId = (TextView) findViewById(R.id.layout_event_id);
		mEventNameText = (TextView) findViewById(R.id.layout_event_name); 
		mEventPlaceText = (TextView) findViewById(R.id.layout_event_place); 
		mEventNoteText = (TextView) findViewById(R.id.layout_event_note); 
		mEventStartTimeText = (TextView) findViewById(R.id.layout_event_start_time);
		mEventEndTimeText = (TextView) findViewById(R.id.layout_event_end_time);
		mIsRepeatedIcon = (ImageView) findViewById(R.id.layout_event_image_repeat);
		mHasAlarmIcon = (ImageView) findViewById(R.id.layout_event_image_alarm);
		mMutesDeviceIcon = (ImageView) findViewById(R.id.layout_event_image_mute_device);
		
		mCopyButton = (LinearLayout) findViewById(R.id.layout_event_button_copy);
		mEditButton = (LinearLayout) findViewById(R.id.layout_event_button_edit);
		mDeleteButton = (LinearLayout) findViewById(R.id.layout_event_button_delete);
	}
	
	/*
	 * Populate view with event's information.
	 */
	public void populate(Event event, Date displayDate) {
		mDisplayDate = displayDate;
		mEvent = event;
		//textViewEventId.setText(Integer.toString(event.getId()));
		mEventNameText.setText(event.getName());
		mEventPlaceText.setText(event.getPlace());
		mEventNoteText.setText(event.getNote());
		mEventStartTimeText.setText(START_TIME_FORMAT.format(event.getStartTime()));
		
		if (event.hasEndTime()) {
			mEventEndTimeText.setText("- " + START_TIME_FORMAT.format(event.getEndTime()));
		}
		else {
			mEventEndTimeText.setText("");
		}
		
		mMenuContainer.setVisibility(View.GONE);
		mIsRepeatedIcon.setVisibility(event.isRepeatable() ? View.VISIBLE : View.INVISIBLE);
		mHasAlarmIcon.setVisibility(event.hasAlarm() ? View.VISIBLE : View.INVISIBLE);
		mMutesDeviceIcon.setVisibility(event.mutesDevice() ? View.VISIBLE : View.INVISIBLE);
		
		//TimetableLogger.verbose("Event " + event.getName() + " successfully drawed");
	}
	public Event getEvent() {
		return mEvent;
	}
	
	public void showMenu() {
		mMenuContainer.setVisibility(View.VISIBLE);
		TimetableLogger.error(Boolean.toString(mMenuContainer.requestFocusFromTouch()));
		TimetableLogger.error(Boolean.toString(mMenuContainer.isFocusable()));
		TimetableLogger.error(Boolean.toString(mMenuContainer.isFocusableInTouchMode()));
		
	}
	
	public void hideMenu() {
		mMenuContainer.clearFocus();
		mMenuContainer.setVisibility(View.GONE);
	}
	
	/*
	 * Set observer, that implement EventView.EventViewObserver interface.
	 * His methods will be called, when corresponding elements are clicked.
	 */
	public void setEventViewObserver(EventViewObserver observer) {
		mObserver = observer;

		mEditButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				TimetableLogger.verbose("EventView: edit button clicked");
				if (mObserver != null) {
					mObserver.onButtonEditClicked(EventView.this);
				}
			}
		});
		
		mCopyButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.verbose("EventView: copy button clicked.");
				mObserver.onButtonCopyClicked(EventView.this);
			}
		});
		
		mDeleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.verbose("EventView: delete button clicked.");
				mObserver.onButtonDeleteClicked(EventView.this);
			}
		});
		
		mEventInfoContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.verbose("EventView: event clicked.");
				mObserver.onEventViewClicked(EventView.this);
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
	
	public static interface EventViewObserver {
		
		public void onEventViewClicked(EventView eventView);
		
		public void onEventViewLongClicked(EventView eventView);
		
		public void onButtonDeleteClicked(EventView eventView);
		
		public void onButtonEditClicked(EventView eventView);
		
		public void onButtonCopyClicked(EventView eventView);
	}
}
