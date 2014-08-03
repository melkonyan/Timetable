package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.timetable.android.Event;
import com.timetable.android.EventPeriod;
import com.timetable.android.TimetableLogger;
import com.timetable.android.alarm.EventAlarm;

public class EventAlarmTestCase extends TestCase {
	
	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private Date alarmTime;
	
	@Override
	public void setUp() {
		try {
		
		alarmTime = dateTimeFormat.parse("24.04.2014 14:15");
		} catch (ParseException e) {
			fail("Could not parse alarm time");
		}
	}
	
	public void testGetNextOccurrence() throws ParseException {
		
		Event event = new Event.Builder()
						.setAlarmTime(dateTimeFormat.parse("01.08.2014 14:00"))
						.setDate(dateFormat.parse("01.08.2014"))
						.build();
		
		Date today = dateTimeFormat.parse("01.08.2014 13:18"); 
		EventAlarm alarm = event.alarm;
		
		assertEquals(alarm.time, alarm.getNextOccurrence(today));
		assertEquals(event.date, alarm.getNextEventOccurrence(today));
		
		today = dateTimeFormat.parse("01.08.2014 15:00");
		
		assertNull(alarm.getNextOccurrence(today));
		
		today = dateTimeFormat.parse("01.08.2014 14:00");
		
		assertNull(alarm.getNextOccurrence(today));
		
		event.period.type = EventPeriod.Type.DAILY;
		event.period.interval = 1;
		
		assertEquals(dateTimeFormat.parse("02.08.2014 14:00"), alarm.getNextOccurrence(today));
		
		event.addException(dateFormat.parse("02.08.2014"));
		
		assertEquals(dateTimeFormat.parse("03.08.2014 14:00"), alarm.getNextOccurrence(today));
		
		event.deleteException(dateFormat.parse("02.08.2014"));
		
		alarm.time = dateTimeFormat.parse("30.07.2014 14:00");
		
		assertEquals(dateTimeFormat.parse("02.08.2014 14:00"), alarm.getNextOccurrence(today));
		
		event.period.endDate = dateFormat.parse("04.08.2014");
		
		assertNull(alarm.getNextOccurrence(today));
		
		event.period.endDate = dateFormat.parse("05.08.2014");
		
		assertEquals(dateTimeFormat.parse("02.08.2014 14:00"), alarm.getNextOccurrence(today));
		
	}	
	
	public void testGetEventOccurrence() throws ParseException {
		EventAlarm alarm = new EventAlarm();
		alarm.time = EventAlarm.timeFormat.parse("06.07.2014 15:47");
		Event event = new Event();
		event.date = dateFormat.parse("10.07.2014");
		alarm.event = event;
		assertEquals(dateFormat.parse("10.07.2014"), alarm.getEventOccurrence(alarm.time));
		
	}
	
	public void testIsOk() {
		EventAlarm alarm = new EventAlarm();
		assertEquals(false, alarm.isOk());
		alarm.time = alarmTime;
		assertEquals(true, alarm.isOk());
		alarm.type = null;
		assertEquals(false, alarm.isOk());
	}
	
	public void testEquals() {
		EventAlarm alarm1 = new EventAlarm(), alarm2 = new EventAlarm();
		assertEquals(true, alarm1.equals(alarm2));
		alarm1.time = alarmTime;
		assertEquals(false, alarm1.equals(alarm2));
		alarm2.time = alarm1.time;
		assertEquals(true, alarm1.equals(alarm2));
		alarm1.id = 10;
		assertEquals(false, alarm1.equals(alarm2));
		alarm2.id = alarm1.id;
		assertEquals(true, alarm1.equals(alarm2));
		alarm1.eventId = 20;
		assertEquals(false, alarm1.equals(alarm2));
		alarm2.eventId = alarm1.eventId;
		assertEquals(true, alarm1.equals(alarm2));
	}
	
}
