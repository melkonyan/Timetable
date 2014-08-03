package com.timetable.android.utils;

import java.text.SimpleDateFormat;

public class DateFormatFactory {
	
	public static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat("dd.MM.yyyy");
	}
	
	public static SimpleDateFormat getTimeFormat() {
		return new SimpleDateFormat("HH:mm");
	}
	
	public static SimpleDateFormat getLongTimeFormat() {
		return new SimpleDateFormat("HH:mm:ss");
	}
	
	public static SimpleDateFormat getDateTimeFormat() {
		return new SimpleDateFormat("dd.MM.yyyy HH:mm");
	}
	
	public static SimpleDateFormat getLongDateTimeFormat() {
		return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	}
	
}
