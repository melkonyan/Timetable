package com.timetable.android.tests;

import junit.framework.TestCase;

import com.timetable.android.EventController;
import com.timetable.android.utils.DateUtils;

public class EventControllerTestCase extends TestCase {
	
	public void testGetAlarmToastMessage() {
		long min = DateUtils.MINUTE_MILLIS;;
		long h = DateUtils.HOUR_MILLIS;
		
		long timeLeft = 0 * h + 1 * min;
		
		assertEquals("Alarm is set for 1 minute from now.", EventController.getAlarmToastMessage(timeLeft));
		
		timeLeft = 0 * h + 2 * min;
		
		assertEquals("Alarm is set for 2 minutes from now.", EventController.getAlarmToastMessage(timeLeft));
		
		timeLeft = 0;
		
		assertEquals("Alarm is set for less than minute from now.", EventController.getAlarmToastMessage(timeLeft));
		
		timeLeft = 1 * h + 0 * min;
		
		assertEquals("Alarm is set for 1 hour from now.", EventController.getAlarmToastMessage(timeLeft));
		
		timeLeft = 2 * h + 0 * min; 
		
		assertEquals("Alarm is set for 2 hours from now.", EventController.getAlarmToastMessage(timeLeft));
		
		timeLeft = 10 * h + 10 * min;
		
		assertEquals("Alarm is set for 10 hours and 10 minutes from now.", EventController.getAlarmToastMessage(timeLeft));
		
		timeLeft = EventController.MAX_TIME_TILL_NEXT_ALARM_OCCURRENCE + 1;
		
		assertNull(EventController.getAlarmToastMessage(timeLeft));
		
	}
}
