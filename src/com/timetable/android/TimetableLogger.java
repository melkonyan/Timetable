package com.timetable.android;

import org.acra.ACRA;

import android.util.Log;

/*
 * Class, that helps to log debug and error's information.
 */
public class TimetableLogger {
	
	public static final String logTag = "Timetable";
	
	//If set to false, logger will not write anything to logcat.
	public static boolean debugging = false;
	
	//If set to true, report will be send to server, when error is found.
	public static boolean sendReport = true;
	
	//If set to true, logger habe logged error messages.
	private static boolean errorFound = false;
	
	
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
			errorFound = true;
		}
	}
	
	public static void sendReport() {
		if (debugging && errorFound && sendReport) {
			ACRA.getErrorReporter().handleException(null);
		}
	}
}
