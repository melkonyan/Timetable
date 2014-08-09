package com.timetable.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;

import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.TimetableUtils;

public class EventPeriod {
	
	public enum Type {
		NONE,
		DAILY,
		WEEKLY,
		MONTHLY,
		YEARLY
	}
	
	public static final int SUNDAY = 0;
	
	public static final int MONDAY = 1;
	
	public static final int TUESDAY = 2;
	
	public static final int WEDNESDAY = 3;
	
	public static final int THURSDAY = 4;
	
	public static final int FRIDAY = 5;

	public static final int SATURDAY = 6;
	
	public static final Type NONE = Type.NONE;
	
	public static final Type DAILY = Type.DAILY;
	
	public static final Type WEEKLY = Type.WEEKLY;
	
	public static final Type MONTHLY = Type.MONTHLY;
	
	public static final Type YEARLY = Type.YEARLY;
	
	public static final String BUNDLE_PERIOD_ID = "per_id";
	
	public static final String BUNDLE_PERIOD_TYPE = "per_type";
	
	public static final String BUNDLE_PERIOD_INTERVAL = "per_interval";
	
	public static final String BUNDLE_PERIOD_WEEK_OCCURRENCES = "per_week_occurences";
	
	public static final String BUNDLE_PERIOD_NUM_OF_REPEATS = "per_num_of_repeats";
	
	public static final String BUNDLE_PERIOD_END_DATE = "per_end_date";
	
	public static final SimpleDateFormat dateFormat = DateFormatFactory.getDateFormat();
	
	public int id;
	
	public EventPeriod.Type type = EventPeriod.Type.NONE;
	
	public int interval;
	
	public boolean [] weekOccurrences = new boolean[7];
	
	public Date endDate;
	
	public int numberOfRepeats;
	
	public EventPeriod(Bundle data) throws ParseException {
		id = data.getInt(BUNDLE_PERIOD_ID);
		interval = data.getInt(BUNDLE_PERIOD_INTERVAL);
		numberOfRepeats = data.getInt(BUNDLE_PERIOD_NUM_OF_REPEATS);
		setWeekOccurrences(data.getInt(BUNDLE_PERIOD_WEEK_OCCURRENCES));
		type = EventPeriod.Type.values()[data.getInt(BUNDLE_PERIOD_TYPE)];
		setEndDate(data.getString(BUNDLE_PERIOD_END_DATE));
	}
	
	public EventPeriod(int id) {
		this.id = id;
	}
	
	public EventPeriod() {
		this(-1);
	}
	
	public Bundle convert() {
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_PERIOD_ID, id);
		bundle.putInt(BUNDLE_PERIOD_INTERVAL, interval);
		bundle.putInt(BUNDLE_PERIOD_NUM_OF_REPEATS, numberOfRepeats);
		bundle.putInt(BUNDLE_PERIOD_TYPE, type.ordinal());
		bundle.putInt(BUNDLE_PERIOD_WEEK_OCCURRENCES, getWeekOccurrences());
		bundle.putString(BUNDLE_PERIOD_END_DATE, getEndDateString());
		return bundle;
	}
	
	public void setWeekOccurrences(int val) {
		for (int i = 0; i < 7; i++) {
			weekOccurrences[7 - i - 1] = (val % 2) != 0;
			val = val >> 1;
		}
	}
	
	public int getWeekOccurrences() {
		if (type != Type.WEEKLY) {
			return 0;
		}
		int val = 0;
		for (int i = 0; i < 7; i++) {
			val = (val << 1) + (weekOccurrences[i] ? 1 : 0);
		}
		return val;
	}
	
	/*
	 * Add session of weekly period on given day of the week.
	 */
	public void addWeekOccurrence(int day) {
		if (day > 6) {
			return;
		}
		weekOccurrences[day] = true;
	}
	
	/*
	 * Delete session of weekly period on given day of the week.
	 */
	public void deleteWeekOccurrence(int day) {
		if (day > 6) {
			return;
		}
		weekOccurrences[day] = false;
	}
	
	/*
	 * Return true, if weekly event has occurrence on the given day of week.  
	 */
	public boolean isWeekOccurrence(int day) {
		if (day > 6 || type != Type.WEEKLY) {
			return false;
		}
		
		return weekOccurrences[day];
	}
	
	public void setEndDate(String endDateString) throws ParseException {
		endDate = DateUtils.getDateFromString(dateFormat,endDateString);
	}
	
 	public String getEndDateString() {
		return hasEndDate() ? dateFormat.format(endDate) : "";
	}
	
	public boolean hasEndDate() {
		return endDate != null;
	}
	
	public boolean isFinished(Date today) {
		return hasEndDate() && DateUtils.compareDates(today, endDate) != DateUtils.BEFORE;
	}
	
	/*
	 * returns true if period is valid
	 */
	public boolean isOk() {
		if (type == null || type != Type.NONE && interval <= 0 || type == Type.WEEKLY && weekOccurrences == null) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public boolean isRepeatable() {
		return type != Type.NONE;
	}
	
	public boolean isEveryWeek() {
		return type == Type.WEEKLY;
	}
	
	/*
	 * returns true if two periods are equal. 
	 * Only fields that are important for current period.type should be equal 
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof EventPeriod)) {
	        return false;
	    }
	    EventPeriod that = (EventPeriod) other;
	    
	    if (this.type != that.type || this.id != that.id ||
	    	this.type != Type.NONE && 
	    	(this.interval != that.interval || 
	    		!TimetableUtils.areEqualOrNulls(this.endDate, that.endDate) || 
	    		this.numberOfRepeats != that.numberOfRepeats) ||
	    	this.type == Type.WEEKLY && this.getWeekOccurrences() != that.getWeekOccurrences()) {
	    	
	    	return false; 
	    }
	    return true;
	}
	
	@Override 
	public String toString() {
		return "Period. Type: " + this.type.toString() + "; Interval: " + Integer.toString(interval) + 
				"; Week days: " + Integer.toString(getWeekOccurrences()) + "; End date: " 
				+ (endDate != null ? endDate.toString() : "null"); 
	}
	
	/*
	 * Return true, if period, that was started on given @startDate, has occurrence on given @date.
	 */
	public boolean hasOccurrenceOnDate(Date startDate, Date date) {
		return DateUtils.areSameDates(date, getNextOccurrence(startDate, date));
	}
	
	/*
	 * Return the nearest occurrence of period in the future.
	 * Return null of there is no occurrences in the future.
	 * Only date is considered, time plays no role.
	 * If startDate and today are the same, today will be returned. 
	 * Assume, that if period is weekly, it has occurrence on it's startDate
	 */
	public Date getNextOccurrence(Date startDate, Date today) {
		if (DateUtils.compareDates(startDate, today) != DateUtils.BEFORE && 
			//TODO: move comparison with endDate to new method
			(endDate == null || endDate != null && DateUtils.compareDates(today, endDate) == DateUtils.BEFORE)){
			return startDate;
		}
		
		Calendar todayCal = Calendar.getInstance();
		todayCal.setTime(today);
		Calendar dateCal = Calendar.getInstance();
		dateCal.setTime(startDate);
		Calendar ansCal = Calendar.getInstance();
		
		long dateLong = startDate.getTime(), todayLong = todayCal.getTime().getTime(); 
		long day = 1000*60*60*24, week = 1000*60*60*24*7;
		
		int diff;
		switch(type) {
			case NONE:
				return null;
			case DAILY:
				diff = this.interval - (int) (todayLong / day - dateLong / day) % this.interval;
				System.out.println(todayLong + " " + dateLong + " " + todayLong / day + " " + dateLong / day);
				
				if (diff == this.interval) diff = 0;
				ansCal.setTime(todayCal.getTime());
				ansCal.add(Calendar.DATE, diff);
				System.out.println(diff);
				break;
			case WEEKLY:
				diff = interval - (int) ((todayLong - dateLong) / week) % interval;
				if (diff == interval) diff = 0;
				
				ansCal.setTime(todayCal.getTime());
				ansCal.add(Calendar.WEEK_OF_YEAR, diff);
				
				boolean isFound = false;
				int count = 0;
				for (int i = todayCal.get(Calendar.DAY_OF_WEEK); i <= Calendar.SATURDAY; i++) {
					if (isWeekOccurrence(i - 1)) {
						ansCal.add(Calendar.DATE, count);	
						isFound = true;
						break;
					} else {
						count ++;
					}
				}
				if (isFound) {
					break;
				}
					
				ansCal.add(Calendar.WEEK_OF_YEAR, interval);
				ansCal.add(Calendar.DATE, dateCal.get(Calendar.DAY_OF_WEEK) - todayCal.get(Calendar.DAY_OF_WEEK));
				break;
			case MONTHLY:
				diff = this.interval - ((todayCal.get(Calendar.YEAR)- dateCal.get(Calendar.YEAR))*12 
										+ todayCal.get(Calendar.MONTH) - dateCal.get(Calendar.MONTH)) % this.interval;
				if (diff == this.interval && todayCal.get(Calendar.DAY_OF_MONTH) <= dateCal.get(Calendar.DAY_OF_MONTH)) diff = 0;
				ansCal.setTime(todayCal.getTime());
				ansCal.add(Calendar.MONTH, diff);
				ansCal.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
				break;
				
			case YEARLY:
				diff = this.interval - (todayCal.get(Calendar.YEAR) - dateCal.get(Calendar.YEAR)) % this.interval;
				if (diff == this.interval && todayCal.get(Calendar.DAY_OF_YEAR) <=  dateCal.get(Calendar.DAY_OF_YEAR)) diff = 0;
				ansCal.setTime(todayCal.getTime());
				ansCal.add(Calendar.YEAR, diff);
				ansCal.set(Calendar.MONTH, dateCal.get(Calendar.MONTH));
				ansCal.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
				break;
			default:
				return null;
		}
		
		if (endDate != null && DateUtils.compareDates(ansCal.getTime(), endDate) != DateUtils.BEFORE) {
			return null;
		}
		return ansCal.getTime();
	}

}
