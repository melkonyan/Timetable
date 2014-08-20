package com.timetable.android.utils;

import java.util.Calendar;
import java.util.Date;

public abstract class TimeProvider {
	
	/*
	 * Return calendar, set to current time.
	 */
	public abstract Calendar getCurrDateTimeCal();

	public final Date getCurrDateTime() {
		return getCurrDateTimeCal().getTime();
	}
	
	/*
	 * Return calendar, set to current date, time is 00:00;
	 */
	public final Calendar getCurrDateCal() {
		return DateUtils.extractDate(getCurrDateTimeCal());
	}
	
	public final Date getCurrDate() {
		return getCurrDateCal().getTime();
	}
	
	/*
	 * Return calendar, set to current time, date is 01.01.1970;
	 */
	public final Calendar getCurrTimeCal() {
		return DateUtils.extractTime(getCurrDateTimeCal());
	}
	
	public final Date getCurrTime() {
		return getCurrTimeCal().getTime();
	}
	
	
	
}
