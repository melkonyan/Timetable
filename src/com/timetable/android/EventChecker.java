package com.timetable.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.timetable.android.utils.DateFormatFactory;

import android.content.Context;
import android.content.res.Resources;

/*
 * Class, that checks event data, and throws exception, if it is incorrect
 */
public class EventChecker {
	
	public static final SimpleDateFormat dateFormat =  DateFormatFactory.getDateFormat();
	
	public static final SimpleDateFormat timeFormat = DateFormatFactory.getTimeFormat();
	
	public static final SimpleDateFormat alarmTimeFormat = DateFormatFactory.getTimeFormat();
	
	private Resources resources;
	
	public EventChecker(Context context) {
		resources = context.getResources();
	}
	
	public void checkEvent(Event event) throws IllegalEventEndTimeException, IllegalEventPeriodEndDateException {
		if (event.hasStartTime() && event.hasEndTime() && event.getStartTime().compareTo(event.getEndTime()) >= 0) {
			throw new IllegalEventEndTimeException(resources.getString(R.string.event_checker_start_time_before_end_time));
		}
		if (event.getPeriod().hasEndDate() && event.getDate().compareTo(event.getPeriod().getEndDate()) >= 0) {
			throw new IllegalEventPeriodEndDateException(resources.getString(R.string.event_checker_end_date_before_start_date));
		}
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

	public Date getAlarmTimeFromString(String alarmTimeString) throws IllegalEventAlarmTimeException {
		try {
			return alarmTimeFormat.parse(alarmTimeString);
		} catch (ParseException e) {
			throw new IllegalEventAlarmTimeException(resources.getString(R.string.event_checker_invalid_alarm_time));
		}
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
	
	
	public class IllegalEventNameException extends IllegalEventDataException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7908021028670133000L;

		IllegalEventNameException(String message) {
			super(message);
		}
	}
	
	public class IllegalEventPlaceException extends IllegalEventDataException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7481909256053692448L;

		IllegalEventPlaceException(String message) {
			super(message);
		}
	}
	
	public class IllegalEventDateException extends IllegalEventDataException {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = 3219562656583347849L;

		IllegalEventDateException(String message) {
			super(message);
		}
	}
	
	public class IllegalEventStartTimeException extends IllegalEventDataException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1031240263102288540L;

		IllegalEventStartTimeException(String message) {
			super(message);
		}
	}
	
	public class IllegalEventEndTimeException extends IllegalEventDataException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3036668173745013630L;

		IllegalEventEndTimeException(String message) {
			super(message);
		}
	}
	
	public class IllegalEventAlarmTimeException extends IllegalEventDataException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7849748397479123194L;
		
		public IllegalEventAlarmTimeException(String message) {
			super(message);
		}
	}
	public class IllegalEventPeriodIntervalException extends IllegalEventDataException {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = -5698714595824081580L;
		
		IllegalEventPeriodIntervalException(String message) {
			super(message);
		}
	
	}
	
	public class IllegalEventPeriodEndDateException extends IllegalEventDataException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -548533272618594920L;

		IllegalEventPeriodEndDateException(String message) {
			super(message);
		}
	}	
	
}