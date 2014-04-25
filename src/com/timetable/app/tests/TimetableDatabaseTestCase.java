package com.timetable.app.tests;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import android.test.ActivityInstrumentationTestCase2;
import android.test.RenamingDelegatingContext;

import com.timetable.app.Event;
import com.timetable.app.EventAlarm;
import com.timetable.app.EventDayViewActivity;
import com.timetable.app.EventPeriod;
import com.timetable.app.TimetableDatabase;

public class TimetableDatabaseTestCase extends ActivityInstrumentationTestCase2<EventDayViewActivity> {

	public TimetableDatabaseTestCase() {
		super(EventDayViewActivity.class);
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
			
			Event event1 = new Event();
			event1.name = "event1";
			event1.date = searchDate;
			event1.startTime = timeFormat.parse("17:00:00");
			event1.period = new EventPeriod();
			event1.period.type = EventPeriod.Type.DAILY;
			event1.period.interval = 2;
			event1.period.endDate = dateFormat.parse("25.04.2014");
			foundEvents.add(event1);
			
			Event event2 = new Event();
			event2.name = "event2";
			event2.date = searchDate;
			event2.startTime = timeFormat.parse("16:00:00");
			event2.alarm = new EventAlarm();
			event2.alarm.time = EventAlarm.timeFormat.parse("24.04.2014 19:22");
			
			foundEvents.add(event1);
			
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
			Event curEvent = foundEvents.get(i), foundEvent = events.get(events.size() - i - 1);
			curEvent.id = foundEvent.id;
			if (curEvent.period != null) {
				curEvent.period.id = foundEvent.period.id;
			}
			
			if (curEvent.hasAlarm()) {
				curEvent.alarm.id = foundEvent.alarm.id;
				curEvent.alarm.eventId = curEvent.id;
			}
			assertEquals(true, foundEvents.get(i).equals(events.get(events.size() - 1 - i)));
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

