package com.example.timetable;

import java.util.Date;

public class EventPeriod {
	
	public enum Type {
		NONE,
		DAILY,
		WEEKLY,
		MONTHLY,
		YEARLY
	}
	
	public int id;
	
	public EventPeriod.Type type = EventPeriod.Type.NONE;
	
	public int interval;
	
	public boolean [] weekOccurrences;
	
	public Date endDate;
	
	public int numberOfRepeats;
	
	public EventPeriod(int id) {
		this.id = id;
	}
	
	public EventPeriod() {
		this(-1);
	}
	
	public void setWeekOccurrences(int val) {
		if (weekOccurrences == null) {
			weekOccurrences = new boolean[7];
		}
		for (int i = 0; i < 7; i++) {
			weekOccurrences[7 - i - 1] = (val % 2) != 0;
			val = val >> 1;
		}
	}
	
	public int getWeekOccurrences() {
		if (weekOccurrences == null) {
			return 0;
		}
		int val = 0;
		for (int i = 0; i < 7; i++) {
			val = (val << 1) + (weekOccurrences[i] ? 1 : 0);
		}
		return val;
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
}
