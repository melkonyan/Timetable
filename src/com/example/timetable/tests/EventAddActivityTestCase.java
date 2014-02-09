package com.example.timetable.tests;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.test.RenamingDelegatingContext;
import android.test.UiThreadTest;

import com.example.timetable.Event;
import com.example.timetable.EventAddActivity;
import com.example.timetable.IllegalEventDataException;
import com.example.timetable.TimetableDatabase;
import com.example.timetable.TimetableLogger;

public class EventAddActivityTestCase extends ActivityInstrumentationTestCase2<EventAddActivity> {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	
	private Context mContext;
	
	private EventAddActivity mActivity;
	
	private TimetableDatabase db;
	
	public EventAddActivityTestCase() {
		super(EventAddActivity.class);
	}

	@Override
	public void setUp() {
		mContext = new RenamingDelegatingContext(getActivity(), "test_");
		mActivity = getActivity();
	}
	
	@UiThreadTest
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
		
	}
}
