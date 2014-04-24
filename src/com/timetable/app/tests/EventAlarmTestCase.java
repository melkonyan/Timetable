package com.timetable.app.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.timetable.app.EventAlarm;

public class EventAlarmTestCase extends TestCase {
	
	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	private Date alarmTime;
	
	@Override
	public void setUp() {
		try {
		
		alarmTime = dateTimeFormat.parse("24.04.2014 14:15");
		} catch (ParseException e) {
			fail("Could not parse alarm time");
		}
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
