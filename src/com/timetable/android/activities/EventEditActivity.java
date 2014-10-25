package com.timetable.android.activities;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;

import com.timetable.android.Event;
import com.timetable.android.EventController;
import com.timetable.android.EventController.OnEventDeletedListener;
import com.timetable.android.EventController.OnEventUpdatedListener;
import com.timetable.android.EventPeriod;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.Logger;

/*
 * Activity provides user interface to edit oldEvent
 * Should be started with extra field 'event_id' that contains id of oldEvent,
 * that should be edited and field 'date', that contains editDate, when oldEvent was edited.
 */
public class EventEditActivity extends EventAddActivity implements OnEventUpdatedListener, OnEventDeletedListener {

	public static final String EXTRA_EVENT_ID = "event_id";
	
	//oldEvent that should be edited
	private Event oldEvent;
	
	//new data
	private Event editedEvent;
	
	//editDate when oldEvent is edited
	private Date editDate;
	
	private TimetableDatabase db; 
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			Bundle extras = getIntent().getExtras();
			int eventId = extras.getInt(EXTRA_EVENT_ID);
			db = TimetableDatabase.getInstance(this);
			oldEvent = db.searchEventById(eventId);
			if (oldEvent == null) {
				Logger.error("EventEditActivity.onCreate: oldEvent not found. " + Integer.toString(eventId));
				finish();
				return;
			}
			getSupportActionBar().setTitle(getResources().getString(R.string.actionbar_edit_event));
			
			try {
				editDate = INIT_DATE_FORMAT.parse(extras.getString(EXTRA_DATE));
			} catch(ParseException e) {
				Logger.error("EventEditActivity.onCreate: could not parse date of editing. " + e.getMessage());
				finish();
				return;
			}
			
			setEvent(oldEvent);
			showEventPeriod();
			showEventAlarm();
			eventDateVal.removeTextChangedListener(eventAddTextWatcher);
			eventDateVal.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					try {
						String dateString = s.toString();
						Calendar date = Calendar.getInstance();
						date.setTime(dateFormat.parse(dateString));
						int weekDay = date.get(Calendar.DAY_OF_WEEK) - 1;
						for (int i = EventPeriod.SUNDAY; i <= EventPeriod.SATURDAY; i++) {
							if (!eventPeriodWeekDayCheckBoxes[i].isEnabled()) {
								eventPeriodWeekDayCheckBoxes[i].setEnabled(true);
								eventPeriodWeekDayCheckBoxes[i].setChecked(oldEvent.getPeriod().isWeekOccurrence(i));
							}
						eventPeriodWeekDayCheckBoxes[weekDay].setEnabled(false);
						eventPeriodWeekDayCheckBoxes[weekDay].setChecked(true);
						}
					} catch (ParseException e) {
						return;
					}
				}
			});
			
			eventDateVal.setText(dateFormat.format(editDate));
			
		Logger.log("EventEditActivity successfully created.");
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_event_edit, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_save_event:
	        	Logger.log("EventEditActivity: try to save oldEvent.");
	        	saveEvent();
	        	return true;
	        case R.id.action_delete_event:
	        	Logger.log("EventEditActivity: try to delete oldEvent.");
	        	deleteEvent();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override 
	public boolean saveEvent()  {
		editedEvent = getEvent();
		if (editedEvent == null) {
			return false;
		}
		
		EventController eventController = new EventController(this);
		eventController.setOnEventUpdatedListener(this);
		eventController.updateEvent(editedEvent, oldEvent, editDate);
		return true;
	}
	
	public void deleteEvent() {
		EventController eventController = new EventController(this);
		eventController.setOnEventDeletedListener(this);
		eventController.deleteEvent(oldEvent, editDate);
	}
	
	@Override
	public void onEventUpdated(Event updatedEvent) {
		finish();
	}


	@Override
	public void onEventDeleted() {
		finish();
	}
	
}
