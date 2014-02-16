package com.example.timetable;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;

import com.timetable.app.R;

public class EventChecker {
	
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	
	private Resources resources;
	
	EventChecker(Context context) {
		resources = context.getResources();
	}
	
	public void checkEvent(Event event) {
	}
	
	public String getNameFromString(String nameString) throws IllegalEventNameException {
		if (nameString == null || nameString.length() == 0) {
			throw new IllegalEventNameException(resources.getString(R.string.event_checker_empty_name));
		}
		return nameString;
	}
	
	public String getPlaceFromString(String placeString) throws IllegalEventPlaceException {
		return placeString;
	}
	
	public Date getDateFromString(String dateString) throws IllegalEventDataException {
		try {
			return dateFormat.parse(dateString);
		}
		catch (Exception e) {
			throw new IllegalEventDateException(resources.getString(R.string.event_checker_invalid_date));
		}
	}
	
	public Date getStartTimeFromString(String startTimeString) throws IllegalEventStartTimeException {
		try {
			return timeFormat.parse(startTimeString);
		}
		catch (Exception e) {
			throw new IllegalEventStartTimeException(resources.getString(R.string.event_checker_invalid_start_time));
		}
	}
	
	public Date getEndTimeFromString(String endTimeString) throws IllegalEventEndTimeException {
		if (endTimeString == null || endTimeString.length() == 0) {
			return null;
		}
		try {
			return timeFormat.parse(endTimeString);
		}
		catch (Exception e) {
			throw new IllegalEventEndTimeException(resources.getString(R.string.event_checker_invalid_end_time));
		}
	}
	
	public String getNoteFromString(String string) {
		return string;
	}

	public int getPeriodIntervalFromString(String periodIntervalString) throws IllegalEventPeriodIntervalException {
		try {
			int interval = Integer.parseInt(periodIntervalString);
			if (interval == 0) {
				throw new IllegalEventPeriodIntervalException(resources.getString(R.string.event_checker_interval_less_than_zero));
			}
			return interval;
		} catch (Exception e) {
			throw new IllegalEventPeriodIntervalException(resources.getString(R.string.event_checker_invalid_interval));
		}
	}
	
	public Date getPeriodEndDateFromString(String periodEndDateString) throws IllegalEventPeriodEndDateException {
		if (periodEndDateString == null || periodEndDateString.length() == 0) {
			throw new IllegalEventPeriodEndDateException(resources.getString(R.string.event_checker_empty_period_end_date));
		}
		try {
			return dateFormat.parse(periodEndDateString);
		} catch (Exception e) {
			throw new IllegalEventPeriodEndDateException(resources.getString(R.string.event_checker_invalid_period_end_date));
		}
	}
	
	
	class IllegalEventNameException extends IllegalEventDataException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7908021028670133000L;

		IllegalEventNameException(String message) {
			super(message);
		}
	}
	
	class IllegalEventPlaceException extends IllegalEventDataException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7481909256053692448L;

		IllegalEventPlaceException(String message) {
			super(message);
		}
	}
	
	class IllegalEventDateException extends IllegalEventDataException {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = 3219562656583347849L;

		IllegalEventDateException(String message) {
			super(message);
		}
	}
	
	class IllegalEventStartTimeException extends IllegalEventDataException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1031240263102288540L;

		IllegalEventStartTimeException(String message) {
			super(message);
		}
	}
	
	class IllegalEventEndTimeException extends IllegalEventDataException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3036668173745013630L;

		IllegalEventEndTimeException(String message) {
			super(message);
		}
	}
	
	class IllegalEventPeriodIntervalException extends IllegalEventDataException {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = -5698714595824081580L;
		
		IllegalEventPeriodIntervalException(String message) {
			super(message);
		}
	
	}
	
	class IllegalEventPeriodEndDateException extends IllegalEventDataException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -548533272618594920L;

		IllegalEventPeriodEndDateException(String message) {
			super(message);
		}
	}	
	
}