package com.timetable.android.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	
	public static final long DAY_LONG = 1000*60*60*24;
	
	public static final long WEEK_LONG = 1000*60*60*24*7;
	
	public static final int BEFORE = -1;
	
	public static final int EQUALS = 0;
	
	public static final int AFTER = 1;
	
	/*
	 * Return new date with hours, minutes and second set to zero.
	 */
	public static Date extractDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/*
	 * Return new date with date set to zero.
	 */
	public static Date extractTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		return cal.getTime();
	}
	
	public static Date addDay(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}
	/* 
	 * Return true if dates are equal(time does not matter)
	 * If one or both dates are null, return false
	 */
	public static boolean areSameDates(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		return extractDate(date1).equals(extractDate(date2));
	}
	
	/*
	 * Compare first date to second.
	 * Time is not considered.
	 */
	public static int compareDates(Date first, Date second) {
		int ans = extractDate(first).compareTo(extractDate(second));
		if (ans < 0) return BEFORE;
		else if (ans > 0) return AFTER;
		else return EQUALS;
	}
	
	/* 
	 * Compare first time to second.
	 * Date is not considered.
	 */
	public static int compareTimes(Date first, Date second) {
		int ans = extractTime(first).compareTo(extractTime(second));
		if (ans < 0) return BEFORE;
		else if (ans > 0) return AFTER;
		else return EQUALS;
	}
}