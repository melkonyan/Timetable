package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.DateUtils;

public class DateUtilsTestCase extends TestCase{
	
	SimpleDateFormat dateFormat = DateFormatFactory.getDateFormat();
	
	SimpleDateFormat timeFormat = DateFormatFactory.getTimeFormat();
	
	SimpleDateFormat dateTimeFormat = DateFormatFactory.getDateTimeFormat();
	
	SimpleDateFormat longDateTimeFormat = DateFormatFactory.getLongDateTimeFormat();
	

	public void testExctractDate() throws ParseException {
		
		Date date = dateFormat.parse("01.08.2014");
		
		assertEquals(date, new Date(date.getTime()));
		assertEquals(date, DateUtils.extractDate(dateTimeFormat.parse("01.08.2014 13:25")));
		assertEquals(date, DateUtils.extractDate(longDateTimeFormat.parse("01.08.2014 13:25:59")));
	}
	
	public void testExctractTime() throws ParseException {
		
		Date time = timeFormat.parse("13:29");
		
		assertEquals(time, DateUtils.extractTime(dateTimeFormat.parse("01.02.2014 13:29")));
		
		time = timeFormat.parse("00:30");
		
		assertEquals(time, DateUtils.extractTime(dateTimeFormat.parse("01.02.2014 00:30")));
		
	}
	
	public void testSetTime() throws ParseException {
		
		Date date = dateFormat.parse("06.08.2014");
		Date time = timeFormat.parse("23:40");
		
		assertEquals(dateTimeFormat.parse("06.08.2014 23:40"), DateUtils.setTime(date, time));
		
		Date dateTime = dateTimeFormat.parse("06.08.2014 11:07");
		
		assertEquals(dateTimeFormat.parse("06.08.2014 23:40"), DateUtils.setTime(dateTime, time));
	}
}
