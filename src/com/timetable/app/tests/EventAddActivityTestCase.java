package com.timetable.app.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.holoeverywhere.widget.EditText;

import android.content.Context;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.RenamingDelegatingContext;
import android.test.UiThreadTest;
import com.timetable.app.R;
import com.timetable.app.Event;
import com.timetable.app.EventAddActivity;
import com.timetable.app.IllegalEventDataException;
import com.timetable.app.TimetableDatabase;

public class EventAddActivityTestCase extends ActivityInstrumentationTestCase2<EventAddActivity> {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	
	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	private Calendar initDate;
	
	private Context mContext;
	
	private EventAddActivity mActivity;
	
	private TimetableDatabase db;
	
	public EventAddActivityTestCase() {
		super(EventAddActivity.class);
	}

	@Override
	public void setUp() {
		initDate = Calendar.getInstance();
		try {
			initDate.setTime(dateTimeFormat.parse("14.02.2014 23:27"));
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		
		Intent i = new Intent();
		i.putExtra(EventAddActivity.INTENT_EXTRA_DATE, EventAddActivity.INIT_DATE_FORMAT.format(initDate.getTime()));
		setActivityIntent(i);
		
		mContext = new RenamingDelegatingContext(getActivity(), "test_");
		mActivity = getActivity();
		
		
		
	}
	
	private String getFieldText(int fieldId) {
		return ((EditText) mActivity.findViewById(fieldId)).getText().toString();
	}
	
	public void testInitValues() {
		assertNotNull(mActivity);
		assertNotNull(mActivity.getIntent().getExtras());
		assertEquals("14.02.2014", getFieldText(R.id.event_add_date_val));
		assertEquals("00:00", getFieldText(R.id.event_add_start_time_val));
		assertEquals("01:00", getFieldText(R.id.event_add_end_time_val));
		assertEquals("", getFieldText(R.id.event_add_name_val));
		assertEquals("", getFieldText(R.id.event_add_place_val));
		assertEquals("", getFieldText(R.id.event_add_note_val));
		assertEquals(mContext.getResources().getString(R.string.event_add_interval_init_value), getFieldText(R.id.event_period_inteval_val));
	}
	
	public void testViewVisibility() {
	
	}
	/*@UiThreadTest
	public void testGetEvent() {
		try {
			
			try {
				mActivity.saveEvent();
				fail("Invalid event was saved.");
			} catch (IllegalEventDataException e) {	
			}
				Date date = dateFormat.parse("08.02.2014");
				mActivity.eventDateVal.setText(dateFormat.format(date));
			try {
				mActivity.saveEvent();
				fail("Invalid event was saved.");
			} catch (IllegalEventDataException e) {	
			
			}
			Event event = new Event();
			event.date = date;
			event.name = "Name";
			event.place = "Place";
			event.startTime = timeFormat.parse("20:29");
			mActivity.setEvent(event);
			assertEquals(true, event.equals(mActivity.getEvent()));
			mActivity.eventPeriodIntervalVal.setText(Integer.toString(2));
			assertEquals(true, event.equals(mActivity.getEvent()));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}*/
}
