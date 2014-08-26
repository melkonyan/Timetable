package com.timetable.android.utils;

import java.util.Calendar;
import java.util.TimeZone;

public class SimpleTimeProvider extends TimeProvider {

	public Calendar getCurrDateTimeCal() {
		return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}

}
