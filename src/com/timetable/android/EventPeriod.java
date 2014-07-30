package com.timetable.android;

import java.util.Calendar;
import java.util.Date;

import com.timetable.android.functional.TimetableFunctional;

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
	
	public int id;
	
	public EventPeriod.Type type = EventPeriod.Type.NONE;
	
	public int interval;
	
	public boolean [] weekOccurrences = new boolean[7];
	
	public Date endDate;
	
	public int numberOfRepeats;
	
	public EventPeriod(int id) {
		this.id = id;
	}
	
	public EventPeriod() {
		this(-1);
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
	 * return true, if weekly event has occurrence on the given day of week.  
	 */
	public boolean isWeekOccurrence(int day) {
		if (day > 6 || type != Type.WEEKLY) {
			return false;
		}
		
		return weekOccurrences[day];
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
	    		!TimetableFunctional.areEqualOrNulls(this.endDate, that.endDate) || 
	    		this.numberOfRepeats != that.numberOfRepeats) ||
	    	this.type == Type.WEEKLY && this.getWeekOccurrences() != that.getWeekOccurrences()) {
	    	
	    	return false; 
	    }
	    return true;
	   }
	
	@Override 
	public String toString() {
		return "Period: \nType: " + this.type.toString() + "\ninterval: " + Integer.toString(interval) + 
				"\nweek days: " + Integer.toString(getWeekOccurrences()) + "\nend date:" 
				+ (endDate != null ? endDate.toString() : "null"); 
	}
	
	/*
	 * Return true, if period, that was started on given @startDate, has occurrence on given @date.
	 */
	public boolean hasOccurrenceOnDate(Date startDate, Date date) {
		
		Calendar thisDate = Calendar.getInstance();
		thisDate.setTime(startDate);
		Calendar thatDate = Calendar.getInstance();
		thatDate.setTime(date);
		long thisDateInt = startDate.getTime(), thatDateInt = date.getTime(); 
		
		if (endDate != null &&  (thatDateInt - endDate.getTime()) / (1000*60*60*24) > 0 || date.compareTo(startDate) < 0) {
			return false;
		}
				
		switch (type) {
			case NONE:
				return (thatDateInt - thisDateInt) / (1000*60*60*24) == 0;
			case DAILY: 
				return (thatDateInt - thisDateInt) / (1000*60*60*24) % interval == 0;
			case WEEKLY:
				if (weekOccurrences == null) {
					return false;
				}
				return weekOccurrences[thatDate.get(Calendar.DAY_OF_WEEK) - 1] 
						&& (thatDateInt - thisDateInt) / (1000*60*60*24*7) % interval == 0;
			case MONTHLY:
				return thatDate.get(Calendar.DAY_OF_MONTH) == thisDate.get(Calendar.DAY_OF_MONTH)
						&& ((thatDate.get(Calendar.YEAR) - thisDate.get(Calendar.YEAR))*12 
						+ thatDate.get(Calendar.MONTH) - thisDate.get(Calendar.MONTH)) % interval == 0; 
			case YEARLY:
				return  thatDate.get(Calendar.DAY_OF_YEAR) == thisDate.get(Calendar.DAY_OF_YEAR)
						&& (thatDate.get(Calendar.YEAR) - thisDate.get(Calendar.YEAR)) % interval == 0;
		}
		return false;
	}
	
	/*
	 * Return the nearest occurrence of period in the future.
	 * Return null of there is no occurrences in the future.
	 * Only date is considered, time plays no role.
	 * If startDate and today are the same, today will be returned. 
	 */
	public Date getNextOccurrence(Date startDate, Date today) {
		if (startDate.after(today)) {
			if (type != EventPeriod.Type.WEEKLY) {
				return startDate;
			}
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
				TimetableLogger.log(Integer.toString(diff));
				ansCal.setTime(todayCal.getTime());
				ansCal.add(Calendar.DATE, diff);
				System.out.println(diff);
				break;
			case WEEKLY:
				if (today.before(startDate)) {
					todayCal.setTime(startDate);
				}
				
				ansCal.setTime(todayCal.getTime());
				ansCal.set(Calendar.HOUR_OF_DAY, dateCal.get(Calendar.HOUR_OF_DAY));
				ansCal.set(Calendar.MINUTE,	dateCal.get(Calendar.MINUTE));
				
				while(!this.hasOccurrenceOnDate(startDate, ansCal.getTime())) {
					ansCal.add(Calendar.DATE, 1);
					if (this.endDate != null && ansCal.getTime().after(this.endDate)) {
						return null;
					}
				}
				break;
			case MONTHLY:
				diff = this.interval - ((todayCal.get(Calendar.YEAR)- dateCal.get(Calendar.YEAR))*12 
										+ todayCal.get(Calendar.MONTH) - dateCal.get(Calendar.MONTH)) % this.interval;
				if (diff == this.interval && todayCal.get(Calendar.DAY_OF_MONTH) < dateCal.get(Calendar.DAY_OF_MONTH)) diff = 0;
				ansCal.setTime(todayCal.getTime());
				ansCal.add(Calendar.MONTH, diff);
				ansCal.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
				break;
				
			case YEARLY:
				diff = this.interval - (todayCal.get(Calendar.YEAR) - dateCal.get(Calendar.YEAR)) % this.interval;
				if (diff == this.interval && todayCal.get(Calendar.DAY_OF_YEAR) >  dateCal.get(Calendar.DAY_OF_YEAR)) diff = 0;
				ansCal.setTime(todayCal.getTime());
				ansCal.add(Calendar.YEAR, diff);
				ansCal.set(Calendar.MONTH, dateCal.get(Calendar.MONTH));
				ansCal.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
				break;
			default:
				return null;
		}
		ansCal.set(Calendar.HOUR_OF_DAY, dateCal.get(Calendar.HOUR_OF_DAY));
		ansCal.set(Calendar.MINUTE,	dateCal.get(Calendar.MINUTE));
		
		if (this.endDate != null && !this.endDate.after(ansCal.getTime())) {
			return null;
		}
		
		return ansCal.getTime();
	}

}
