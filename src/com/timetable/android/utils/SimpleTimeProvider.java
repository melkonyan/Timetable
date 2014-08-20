package com.timetable.android.utils;

import java.util.Calendar;

public class SimpleTimeProvider extends TimeProvider {

	public Calendar getCurrDateTimeCal() {
		return Calendar.getInstance();
	}

}
