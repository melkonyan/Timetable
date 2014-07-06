package com.timetable.app.functional;

import java.util.Calendar;
import java.util.Date;

public class SimpleTimeProvider implements TimeProvider {

	public Date getCurrentTime() {
		return Calendar.getInstance().getTime();
	}
	
}
