package com.example.timetable;

import android.util.Log;

public class TimetableLogger {
	public static final String logTag = "Timetable";
	public static boolean debugging = false;
	
	public static void log(String message) {
		if (debugging && message != null) {
			Log.i(logTag, message);
		}
	}
}
