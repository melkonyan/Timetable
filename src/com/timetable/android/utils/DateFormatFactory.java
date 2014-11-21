package com.timetable.android.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateFormatFactory {
	
	public static SimpleDateFormat getFormat(String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
		//dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat;
	}
	
	public static SimpleDateFormat getDateFormat() {
		return getFormat("dd.MM.yyyy");
	}
	
	public static SimpleDateFormat getTimeFormat() {
		return getFormat("HH:mm");
	}
	
	public static SimpleDateFormat getLongTimeFormat() {
		return getFormat("HH:mm:ss");
	}
	
	public static SimpleDateFormat getDateTimeFormat() {
		return getFormat("dd.MM.yyyy HH:mm");
	}
	
	public static SimpleDateFormat getLongDateTimeFormat() {
		return getFormat("dd.MM.yyyy HH:mm:ss");
	}
	
}
