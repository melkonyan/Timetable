package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.timetable.android.Event;
import com.timetable.android.EventPeriod;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.alarm.EventAlarm;
import com.timetable.android.utils.DateFormatFactory;

public class TimetableDatabaseTestCase extends AndroidTestCase {


	private RenamingDelegatingContext mContext;
	
	private TimetableDatabase db;
	
	private Vector<Event> foundEvents = new Vector<Event>(); //events that fit searchDate, in reversed order
	
	private Date searchDate;
	
	SimpleDateFormat dateFormat = DateFormatFactory.getDateFormat();
	
	
	public void setUp() throws ParseException {
		mContext = new RenamingDelegatingContext(getContext(), "TimetableDatabaseTestCase_");
		db = TimetableDatabase.getInstance(mContext);
		searchDate = dateFormat.parse("27.12.2013");
		Event event1 = new Event.Builder()
						.setName("event1")
						.setDate(searchDate)
						.setStartTime("17:00")
						.setPeriodType(EventPeriod.DAILY)
						.setPeriodInterval(2)
						.setPeriodEndDate("25.04.2014")
						.build();
		
		foundEvents.add(event1);
		
		Event event2 = new Event.Builder()
						.setName("event2")
						.setDate(searchDate)
						.setStartTime("16:00")
						.setEndTime("18:00")
						.setMuteDevice(true)
						.setAlarmTime("24.04.2014 19:22")
						.setPeriodType(EventPeriod.DAILY)
						.setPeriodInterval(1)
						.build();
		
		foundEvents.add(event2);
	}

	public void testTimetableDatabase() {
		for(int i = 0; i < foundEvents.size(); i++) {
			foundEvents.setElementAt(db.insertEvent(foundEvents.get(i)), i);
		}
		
		Vector<Event> events = db.searchEventsByDate(searchDate);
		assertEquals(foundEvents.size(), events.size());
		
		int alarmsNum = 1;
		assertEquals(alarmsNum, db.getAlarmsCount());
		
		for (int i = 0; i < foundEvents.size(); i++) {
			assertEquals( foundEvents.get(i), events.get(events.size() - 1 - i));
		}
		
		Vector<Event> eventsWithAlarm = db.searchEventsWithAlarm();
		assertEquals(alarmsNum, eventsWithAlarm.size());
		
		assertEquals(foundEvents.get(1), eventsWithAlarm.get(0));
		
		Vector<Event> eventsThatMuteDevice = db.searchEventsThatMuteDevice();
		
		assertEquals(1, eventsThatMuteDevice.size());
		assertEquals(foundEvents.get(1), eventsThatMuteDevice.get(0));
		
		for (Event event: events) {
			db.deleteEvent(event);
		}
		
		events = db.searchEventsByDate(searchDate);
		
		assertEquals(0, events.size());
		db.clear();
		
	}
	
	public void testSearchEventById() throws ParseException {
		Event event = new Event.Builder()
						.setName("event1")
						.setDate("31.07.2014")
						.setStartTime("11:24")
						.build();
		
		assertTrue(event.isOk());
		
		event = db.insertEvent(event);
		
		assertEquals(event, db.searchEventById(event.getId()));
		Date exception =  dateFormat.parse("02.08.2014");
		
		db.insertException(event, exception);
		event.addException(exception);
			
		assertEquals(event, db.searchEventById(event.getId()));
		assertTrue(event.isException(exception));
		db.clear();
	}
	
	public void testUpdateEvent() throws ParseException {
		Event oldEvent = new Event.Builder()
							.setName("old name")
							.setDate("25.04.2014")
							.setStartTime("22:46")
							.setPeriodType(EventPeriod.DAILY)
							.setPeriodInterval(2)
							.build();
		
		oldEvent = db.insertEvent(oldEvent);
		
		Event newEvent = oldEvent;
		
		newEvent.setName("new name");
		newEvent.getPeriod().setInterval(3);
		newEvent.setAlarm(new EventAlarm(newEvent));
		newEvent.getAlarm().setTime("25.04.2014 21:46");
		newEvent = db.updateEvent(newEvent);
		
		Event foundEvent = db.searchEventById(newEvent.getId());
		
		assertTrue(foundEvent.hasAlarm());
		assertTrue(newEvent.equals(foundEvent));
		
		newEvent.getAlarm().setTime("25.04.2014 20:46");
		newEvent = db.updateEvent(newEvent);
		
		assertEquals(newEvent, db.searchEventById(newEvent.getId()));
		
		newEvent.deleteAlarm();
		newEvent = db.updateEvent(newEvent);
		
		assertEquals(newEvent, db.searchEventById(newEvent.getId()));
		db.clear();
	}
	
	
	public void testAlarm() throws ParseException {
		
		Event event1 = new Event.Builder()
							.setDate("14.08.2014")
							.setStartTime("19:07")
							.setAlarmTime("01.05.2014 15:17")
							.build();
		
		event1 = db.insertEvent(event1);
		assertNotSame(-1, event1.getAlarm().id);
		assertEquals(event1.getAlarm(), db.getEventAlarm(event1));
		
		
		event1.getAlarm().setTime("01.05.2014 15:22");
		event1 = db.updateEvent(event1);
		assertEquals(event1.getAlarm(), db.getEventAlarm(event1));
		
		Event event2 = new Event.Builder()
						.setDate("14.08.2014")
						.setStartTime("19:07")
						.setAlarmTime("01.05.2014 18:28")
						.build();
		
		event2 = db.insertEvent(event2);
		assertNotSame(-1, event2.getAlarm().id);
		
		Vector<Event> eventsWithAlarm = new Vector<Event>();
		eventsWithAlarm.add(event1);
		eventsWithAlarm.add(event2);
		Vector<Event> foundEventsWithAlarm = db.searchEventsWithAlarm();
		
		assertEquals(eventsWithAlarm.size(), foundEventsWithAlarm.size());
		
		for (Event insertedEventWithAlarm: eventsWithAlarm) {
			boolean isFound = false;
			for (Event foundEventWithAlarm: foundEventsWithAlarm) {
				if (insertedEventWithAlarm.equals(foundEventWithAlarm)) {
					isFound = true;
					break;
				}
			}
			assertEquals(true, isFound);
		}
		db.clear();
	}
	
	public void tearDown() {
		db.clear();
	}
	
}

