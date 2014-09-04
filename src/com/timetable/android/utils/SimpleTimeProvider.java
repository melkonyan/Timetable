package com.timetable.android.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.timetable.android.TimetableLogger;

public class SimpleTimeProvider extends TimeProvider {

	public Calendar getCurrDateTimeCal() {
		//Calendar cal = Calendar.getInstance();
		//cal.setTime(new Date());
		//cal.add(Calendar.MILLISECOND, TimeZone.getDefault().getOffset(new Date().getTime()));
		//TimetableLogger.error("SimepleTimeProvider.getCurrDateTimeCal: " + cal.getTime().toString());
		//return cal;
		return Calendar.getInstance();
	}

}
