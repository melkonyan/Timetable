package com.timetable.android.utils;

import java.util.Date;


public class TimetableUtils {
	
	private static TimeProvider timeProvider = new SimpleTimeProvider();
	
	
	public static boolean areEqualOrNulls (Object obj1, Object obj2) {
		return (obj1 == null && obj2 == null || obj1 != null && obj2 != null && obj1.equals(obj2));
	}
	
	public static Date getCurrentTime() {
		return timeProvider.getCurrentTime();
	}
	
	public static void setTimeProvider(TimeProvider timeProvider) {
		TimetableUtils.timeProvider = timeProvider;
	}
}
