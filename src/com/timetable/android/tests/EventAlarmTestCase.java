package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.timetable.android.Event;
import com.timetable.android.EventPeriod;
import com.timetable.android.alarm.EventAlarm;
import com.timetable.android.utils.DateFormatFactory;

public class EventAlarmTestCase extends TestCase {
	
	private SimpleDateFormat dateTimeFormat = DateFormatFactory.getDateTimeFormat();
	
	private SimpleDateFormat dateFormat = DateFormatFactory.getDateFormat();
	
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
		EventAlarm alarm = event.getAlarm();
		
		assertEquals(alarm.time, alarm.getNextOccurrence(today));
		assertEquals(event.getDate(), alarm.getNextEventOccurrence(today));
		
		today = dateTimeFormat.parse("01.08.2014 15:00");
		
		assertNull(alarm.getNextOccurrence(today));
		
		today = dateTimeFormat.parse("01.08.2014 14:00");
		
		assertNull(alarm.getNextOccurrence(today));
		
		event.getPeriod().setType(EventPeriod.DAILY);
		event.getPeriod().setInterval(1);
		
		assertEquals(dateTimeFormat.parse("02.08.2014 14:00"), alarm.getNextOccurrence(today));
		
		event.addException(dateFormat.parse("02.08.2014"));
		
		assertEquals(dateTimeFormat.parse("03.08.2014 14:00"), alarm.getNextOccurrence(today));
		
		event.deleteException(dateFormat.parse("02.08.2014"));
		
		alarm.time = dateTimeFormat.parse("30.07.2014 14:00");
		
		assertEquals(dateTimeFormat.parse("02.08.2014 14:00"), alarm.getNextOccurrence(today));
		
		event.getPeriod().setEndDate("04.08.2014");
		
		assertNull(alarm.getNextOccurrence(today));
		
		event.getPeriod().setEndDate("05.08.2014");
		
		assertEquals(dateTimeFormat.parse("02.08.2014 14:00"), alarm.getNextOccurrence(today));
		
		event.getPeriod().deleteEndDate();
		
		alarm.setTime("10.08.2014 21:15");
		event.getPeriod().setType(EventPeriod.NONE);
		event.setDate("10.08.2014");
		today = dateTimeFormat.parse("11.08.2014 00:15");
		alarm.getNextOccurrence(today);
		assertNull(alarm.getNextOccurrence(today));
	}	
	
	public void testGetEventOccurrence() throws ParseException {
		Event event = new Event.Builder()
					.setDate(dateFormat.parse("10.07.2014"))
					.setAlarmTime(EventAlarm.timeFormat.parse("06.07.2014 15:47"))
					.build();
		EventAlarm alarm = event.getAlarm();
		assertEquals(dateFormat.parse("10.07.2014"), alarm.getEventOccurrence(alarm.time));
		
		event.setDate("10.08.2014");
		alarm.setTime("10.08.2014 21:15");
		Date today = dateTimeFormat.parse("11.08.2014 00:15");
		assertEquals(dateFormat.parse("11.08.2014"), alarm.getEventOccurrence(today));
	}
	
	public void testGetTimeTillNextOccurrence() throws ParseException {
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Event event = new Event.Builder()
					.setDate(dateFormat.parse("21.09.2014"))
					.setAlarmTime("21.09.2014 16:00")
					.build();
		
		EventAlarm alarm = event.getAlarm();
		Date today = dateTimeFormat.parse("21.09.2014 12:00");
		
		assertEquals(timeFormat.parse("04:00"), alarm.getTimeTillNextOccurrence(today));
		
		today = dateTimeFormat.parse("20.09.2014 15:59");
		
		assertEquals(timeFormat.parse("24:01"), alarm.getTimeTillNextOccurrence(today));
	}
	public void testGetAlarmOccurrence() throws ParseException {
		Event event = new Event.Builder()
						.setDate("11.08.2014")
						.setAlarmTime("11.08.2014 9:00")
						.build();
		Date today = dateTimeFormat.parse("11.08.2014 11:51");
		
		assertEquals(event.getAlarm().time, event.getAlarm().getAlarmOccurrence(today));
	}
	
	public void testIsOk() {
		Event event = new Event.Builder().build();
		EventAlarm alarm = new EventAlarm(event);
		assertFalse( alarm.isOk());
		alarm.time = alarmTime;
		assertTrue( alarm.isOk());
		alarm.type = null;
		assertFalse( alarm.isOk());
	}
	
	public void testEquals() {
		Event event = new Event.Builder().build();
		EventAlarm alarm1 = new EventAlarm(event), alarm2 = new EventAlarm(event);
		
		assertTrue( alarm1.equals(alarm2));
		alarm1.time = alarmTime;
		assertFalse( alarm1.equals(alarm2));
		alarm2.time = alarm1.time;
		assertTrue( alarm1.equals(alarm2));
		alarm1.id = 10;
		assertFalse( alarm1.equals(alarm2));
		alarm2.id = alarm1.id;
		assertTrue( alarm1.equals(alarm2));
	}
	
}
