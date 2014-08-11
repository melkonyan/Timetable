package com.timetable.android.utils;

import java.util.Date;

public class SimpleTimeProvider implements TimeProvider {

	public Date getCurrentTime() {
		return new Date();
	}
	
}
