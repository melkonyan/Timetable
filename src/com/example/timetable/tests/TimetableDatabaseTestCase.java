package com.example.timetable.tests;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import com.example.timetable.Event;
import com.example.timetable.TimetableActivity;
import com.example.timetable.TimetableDatabase;
import android.test.ActivityInstrumentationTestCase2;
import android.test.RenamingDelegatingContext;

public class TimetableDatabaseTestCase extends ActivityInstrumentationTestCase2<TimetableActivity> {

	public TimetableDatabaseTestCase() {
		super(TimetableActivity.class);
	}

	private RenamingDelegatingContext mContext;
	
	private TimetableDatabase db;
	
	private Vector<Event> foundEvents = new Vector<Event>(); //events that fit searchDate, in reversed order
	
	private Vector<Event> notFoundEvents = new Vector<Event>();
	
	private Date searchDate;
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	
	
	public void setUp() {
		mContext = new RenamingDelegatingContext(getActivity(), "test_");
		db = new TimetableDatabase(mContext);
		try {
			searchDate = dateFormat.parse("27.12.2013");
			
			Event event = new Event();
			event.name = "event1";
			event.date = searchDate;
			event.startTime = timeFormat.parse("17:00:00");
			foundEvents.add(event);
			
			event.name = "event2";
			event.date = searchDate;
			event.startTime = timeFormat.parse("16:00:00");
			
			foundEvents.add(event);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	
	}

	public void testTimetableDatabase() {
		for(Event event: foundEvents) {
			db.insertEvent(event);
		}
		
		for (Event event: notFoundEvents) {
			db.insertEvent(event);
		}
		Vector<Event> events = db.searchEventsByDate(searchDate);
		
		assertEquals(foundEvents.size(), events.size());
		
		for (int i = 0; i < events.size(); i++) {
			assertEquals(true, foundEvents.get(i).name.equals(events.get(events.size() - 1 - i).name));
		}
		
		for (Event event: events) {
			db.deleteEvent(event);
		}
		
		events = db.searchEventsByDate(searchDate);
		
		assertEquals(0, events.size());
		
		
	}
	
	public void tearDown() {
		db.close();
	}
}

