package com.timetable.android;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.acra.ACRA;

import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.Utils;

import android.util.Log;

/*
 * Class, that helps to log debug and error's information.
 */
public class Logger {
	
	public static final String logTag = "Timetable";
	
	//If set to false, logger will not write anything to logcat.
	public static boolean debugging = false;
	
	//If set to true, report will be send to server, when error is found.
	public static boolean sendReport = true;
	
	//If set to true, addition log to file will be saved.
	public static boolean logToFile = true;
	
	//If set to true, logger has logged error messages.
	private static boolean errorFound = false;
	
	
	private static final String FILE_NAME = "sdcard/Timetable.log";
	
	private static final String ERROR_TAG = "Error";
	
	private static final String DEBUG_TAG = "Debug";
	
	public static void verbose(String message) {
		if (debugging && message != null) {
			Log.v(logTag, message);
			logToFile(DEBUG_TAG, message);
		}
	}
	
	public static void log(String message) {
		if (debugging && message != null) {
			Log.i(logTag, message);
			logToFile(DEBUG_TAG, message);
		}
	}
	
	public static void error(String message) {
		if (debugging && message != null) {
			Log.e(logTag, message);
			logToFile(ERROR_TAG, message);
			errorFound = true;
		}
	}
	
	/*
	 * Send ACRA report.
	 */
	public static void sendReport() {
		if (debugging && errorFound && sendReport) {
			ACRA.getErrorReporter().handleException(null);
		}
	}
	
	/*
	 * Log message to file.
	 */
	public static void logToFile(String tag, String message) {
		if (!logToFile) {
			return;
		}
		File logFile = new File(FILE_NAME);
		if (!logFile.exists()) {
		      try {
		         logFile.createNewFile();
		      } 
		      catch (IOException e) {
		    	  //Log.e(logTag, e.getMessage());
				  //that's bad.
		      }
		   }
		   try {
			  String messageToLog = DateFormatFactory.getLongDateTimeFormat().format(Utils.getCurrDateTime());
			  messageToLog += "; " + message; 
		      //BufferedWriter for performance, true to set append to file flag
		      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
		      buf.append(messageToLog);
		      buf.newLine();
		      buf.close();
		   }
		   catch (IOException e)
		   {
			   //Log.e(logTag, e.getMessage());
			   //that's bad
		   }
		}
}
