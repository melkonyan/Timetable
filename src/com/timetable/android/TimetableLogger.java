package com.timetable.android;

import android.util.Log;

/*
 * Class, that helps to log debug and error's information.
 */
public class TimetableLogger {
	
	public static final String logTag = "Timetable";
	public static boolean debugging = false;
	
	public static void verbose(String message) {
		if (debugging && message != null) {
			Log.v(logTag, message);
		}
	}
	
	public static void log(String message) {
		if (debugging && message != null) {
			Log.i(logTag, message);
		}
	}
	
	public static void error(String message) {
		if (debugging && message != null) {
			Log.e(logTag, message);
		}
	}
}
