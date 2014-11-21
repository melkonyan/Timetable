package com.timetable.android.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
	
	public static final long DAY_MILLIS = 1000*60*60*24;
	
	public static final long WEEK_MILLIS = 1000*60*60*24*7;
	
	public static final long HOUR_MILLIS = 1000 * 60 * 60;
	
	public static final long MINUTE_MILLIS = 1000 * 60;
	
	public static final int BEFORE = -1;
	
	public static final int EQUALS = 0;
	
	public static final int AFTER = 1;
	
	
	public static long getOffset(long time) {
		return TimeZone.getDefault().getOffset(time);
	}
	
	public static long addOffset(long time) {
		return time + getOffset(time);
	}
	
	public static Date addOffset(Date time) {
		return new Date(addOffset(time.getTime()));
	}
	
	public static long removeOffset(long time) {
		return time - getOffset(time);
	}
	
	public static Date removeOffset(Date time) {
		return new Date(removeOffset(time.getTime()));
	}
	
	/*
	 * Try to parse date from string. If string is empty, return null;
	 */
	public static Date getDateFromString(SimpleDateFormat format, String dateString) throws ParseException {
		return getDateFromString(format, dateString, true);
	}
	
	/*
	 * Try to parse date from string. If string is empty and date can be null, return null.
	 */
	public static Date getDateFromString(SimpleDateFormat format, String dateString, boolean canBeNull) throws ParseException {
		if (dateString.isEmpty() && canBeNull) {
			return null;
		}
		return format.parse(dateString);
	}
	
	public static Calendar extractDate(Calendar dateTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(extractDate(dateTime.getTime()));
		return cal;
	}
	
	/*
	 * Return new date with hours, minutes and second set to zero.
	 */
	public static Date extractDate(Date dateTime) {
		/*Calendar cal = Calendar.getInstance();
		cal.setTime(dateTime);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	*/
		long localTime = addOffset(dateTime.getTime());
		return removeOffset(new Date(localTime - localTime % DAY_MILLIS));
	}
	
	public static Calendar extractTime(Calendar dateTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(extractTime(dateTime.getTime()));
		return cal;
	}
	
	/*
	 * Return new date with date set to zero.
	 */
	public static Date extractTime(Date dateTime) {
		/*Calendar cal = Calendar.getInstance();
		cal.setTime(dateTime);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		return cal.getTime();
	*/
		return new Date(removeOffset(addOffset(dateTime.getTime()) % DAY_MILLIS));
	}
	
	public static Date setTime(Date date, Date time) {
		Calendar ans1 = Calendar.getInstance();
		Calendar ans2 = Calendar.getInstance();
		ans1.setTime(date);
		ans2.setTime(time);
		ans1.set(Calendar.HOUR_OF_DAY, ans2.get(Calendar.HOUR_OF_DAY));
		ans1.set(Calendar.MINUTE, ans2.get(Calendar.MINUTE));
		ans1.set(Calendar.SECOND, ans2.get(Calendar.SECOND));
	
		return ans1.getTime();
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
