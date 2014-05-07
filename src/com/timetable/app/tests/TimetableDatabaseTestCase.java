package com.timetable.app.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.timetable.app.Event;
import com.timetable.app.EventAlarm;
import com.timetable.app.EventDayViewActivity;
import com.timetable.app.EventPeriod;
import com.timetable.app.TimetableDatabase;
import com.timetable.app.TimetableLogger;

public class TimetableDatabaseTestCase extends AndroidTestCase {


	private RenamingDelegatingContext mContext;
	
	private TimetableDatabase db;
	
	private Vector<Event> foundEvents = new Vector<Event>(); //events that fit searchDate, in reversed order
	
	private Vector<Event> notFoundEvents = new Vector<Event>();
	
	private Date searchDate;
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	
	public void setUp() {
		mContext = new RenamingDelegatingContext(getContext(), "test_");
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
			
			foundEvents.add(event2);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	
	}

	public void testTimetableDatabase() {
		for(int i = 0; i < foundEvents.size(); i++) {
			foundEvents.setElementAt(db.insertEvent(foundEvents.get(i)), i);
		}
		
		Vector<Event> events = db.searchEventsByDate(searchDate);
		assertEquals(foundEvents.size(), events.size());
		
		int alarmsNum = 1;
		assertEquals(alarmsNum, db.getEventAlarmCount());
		
		for (int i = 0; i < foundEvents.size(); i++) {
			assertEquals(true, foundEvents.get(i).equals(events.get(events.size() - 1 - i)));
		}
		
		for (Event event: events) {
			db.deleteEvent(event);
		}
		
		events = db.searchEventsByDate(searchDate);
		
		assertEquals(0, events.size());
		
		
	}
	
	public void testUpdateEvent() {
		try {
		Event oldEvent = new Event();
		oldEvent.name = "old name";
		oldEvent.date = dateFormat.parse("25.04.2014");
		oldEvent.startTime = timeFormat.parse("22:46:00");
		oldEvent.period = new EventPeriod();
		oldEvent.period.type = EventPeriod.Type.DAILY;
		oldEvent.period.interval = 2;
		oldEvent = db.insertEvent(oldEvent);
		
		Event newEvent = db.insertEvent(oldEvent);
		newEvent.name = "new name";
		newEvent.period.interval = 3;
		newEvent.alarm = new EventAlarm();
		newEvent.alarm.time = EventAlarm.timeFormat.parse("25.04.2014 21:46");
		newEvent = db.updateEvent(newEvent);
		
		Event foundEvent = db.searchEventById(newEvent.id);
		
		assertEquals(true, foundEvent.hasAlarm());
		assertEquals(true, newEvent.equals(foundEvent));
		
		newEvent.alarm.time = EventAlarm.timeFormat.parse("25.04.2014 20:46");
		newEvent = db.updateEvent(newEvent);
		
		assertEquals(true, newEvent.equals(db.searchEventById(newEvent.id)));
		
		newEvent.alarm = null;
		newEvent = db.updateEvent(newEvent);
		
		assertEquals(true, newEvent.equals(db.searchEventById(newEvent.id)));
		assertNull(db.searchEventAlarmByEventId(newEvent.id));
		
		} catch (ParseException e) {
			fail(e.getMessage());
		}
	}
	
	
	public void testAlarm() {
		try {
			int eventId = 10;
			EventAlarm alarm = new EventAlarm();
			alarm.time = EventAlarm.timeFormat.parse("01.05.2014 15:17");
			alarm.eventId = eventId;
			alarm.id = (int) db.insertEventAlarm(alarm);
			assertNotSame(-1, alarm.id);
			assertEquals(true, alarm.equals(db.searchEventAlarmById(alarm.id)));
			assertEquals(true, alarm.equals(db.searchEventAlarmByEventId(eventId)));
			
			alarm.time = EventAlarm.timeFormat.parse("01.05.2014 15:22");
			assertEquals(1, db.updateEventAlarm(alarm));
			assertEquals(true, alarm.equals(db.searchEventAlarmById(alarm.id)));
			assertEquals(true, alarm.equals(db.searchEventAlarmByEventId(eventId)));
			
			EventAlarm alarm1 = new EventAlarm();
			alarm1.time = EventAlarm.timeFormat.parse("01.05.2014 17:28");
			alarm1.id = (int) db.insertEventAlarm(alarm1);
			
			assertNotSame(-1, alarm1.id);
			
			Vector<EventAlarm> insertedAlarms = new Vector<EventAlarm>();
			insertedAlarms.add(alarm);
			insertedAlarms.add(alarm1);
			Vector<EventAlarm> foundAlarms = db.getAllAlarms();
			
			assertEquals(insertedAlarms.size(), db.getEventAlarmCount());
			assertEquals(insertedAlarms.size(), foundAlarms.size());
			
			for (EventAlarm insertedAlarm: insertedAlarms) {
				boolean isFound = false;
				for (EventAlarm foundAlarm: foundAlarms) {
					if (insertedAlarm.equals(foundAlarm)) {
						isFound = true;
						break;
					}
				}
				assertEquals(true, isFound);
			}
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		
	}
	
	public void tearDown() {
		//db.close();
	}
}

