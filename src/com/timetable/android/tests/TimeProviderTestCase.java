package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import junit.framework.TestCase;

import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.FakeTimeProvider;
import com.timetable.android.utils.TimeProvider;

public class TimeProviderTestCase extends TestCase {
	
	private SimpleDateFormat dateTimeFormat = DateFormatFactory.getDateTimeFormat();
	
	private SimpleDateFormat dateFormat = DateFormatFactory.getDateFormat();
	
	private SimpleDateFormat timeFormat = DateFormatFactory.getTimeFormat();
	
	public void testTimeProvider() throws ParseException {
		TimeProvider timeProvider = new FakeTimeProvider(dateTimeFormat.parse("20.08.2014 20:15"));
		
		assertEquals(dateFormat.parse("20.08.2014"), timeProvider.getCurrDate());
		assertEquals(timeFormat.parse("20:15"), timeProvider.getCurrTime());
		
	}
}
