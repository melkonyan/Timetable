package com.timetable.android.utils;

import java.util.Calendar;
import java.util.Date;


public class Utils {
	
	private static TimeProvider timeProvider = new SimpleTimeProvider();
	
	
	public static boolean areEqualOrNulls (Object obj1, Object obj2) {
		return (obj1 == null && obj2 == null || obj1 != null && obj2 != null && obj1.equals(obj2));
	}
	
	public static Date getCurrDateTime() {
		return timeProvider.getCurrDateTime();
	}
	
	public static Calendar getCurrDateTimeCal() {
		return timeProvider.getCurrDateTimeCal();
	}
	
	public static Date getCurrDate() {
		return timeProvider.getCurrDate();
	}
	
	public static Calendar getCurrTimeCal() {
		return timeProvider.getCurrTimeCal();
	}
	
	public static Date getCurrTime() {
		return timeProvider.getCurrTime();
	}
	
	public static void setTimeProvider(TimeProvider timeProvider) {
		Utils.timeProvider = timeProvider;
	}
	
	
}
