package com.timetable.android.utils;

import java.util.Calendar;

public class SimpleTimeProvider extends TimeProvider {

	public Calendar getCurrDateTimeCal() {
		//Calendar cal = Calendar.getInstance();
		//cal.setTime(new Date());
		//cal.add(Calendar.MILLISECOND, TimeZone.getDefault().getOffset(new Date().getTime()));
		//Logger.error("SimepleTimeProvider.getCurrDateTimeCal: " + cal.getTime().toString());
		//return cal;
		return Calendar.getInstance();
	}

}
