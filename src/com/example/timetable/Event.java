package com.example.timetable;

import java.util.Calendar;
import java.util.Date;

public class Event {
	
	
	public static final int MIN_NAME_LENGTH = 1;
	
	public static final int MAX_NAME_LENGTH = 45;

	public static final int MAX_PLACE_LENGTH = 45;
	
	public static final int MAX_NOTE_LENGTH = 200;
	
	public static final int MAX_PERIOD_INTERVAL_LENGTH = 2;
	
	public int id;
	
	public String name = "";
	
	public String place = ""; 
	
	public Date startTime;
	
	public Date endTime; 
	
	public Date date;
	
	public EventPeriod period;
	
	public String note = "";
	
	public Event(int id) {
		this.id = id;
		this.period = new EventPeriod();
	}
	
	public Event() {
		this(-1);
	}
	
	/*
	 * returns true if event has occurrence on parameter date
	 */
	public boolean isToday(Date date) {
		
		Calendar thisDate = Calendar.getInstance();
		thisDate.setTime(this.date);
		Calendar thatDate = Calendar.getInstance();
		thatDate.setTime(date);
		long thisDateInt = this.date.getTime(), thatDateInt = date.getTime(); 
		
		if (period.endDate != null &&  (thatDateInt - period.endDate.getTime()) / (1000*60*60*24) > 0 || date.compareTo(this.date) < 0) {
			return false;
		}
				
		switch (period.type) {
			case NONE:
				return (thatDateInt - thisDateInt) / (1000*60*60*24) == 0;
			case DAILY: 
				return (thatDateInt - thisDateInt) / (1000*60*60*24) % period.interval == 0;
			case WEEKLY:
				if (period.weekOccurrences == null) {
					return false;
				}
				return period.weekOccurrences[thatDate.get(Calendar.DAY_OF_WEEK) - 1] 
						&& (thatDateInt - thisDateInt) / (1000*60*60*24*7) % period.interval == 0;
			case MONTHLY:
				return thatDate.get(Calendar.DAY_OF_MONTH) == thisDate.get(Calendar.DAY_OF_MONTH)
						&& ((thatDate.get(Calendar.YEAR) - thisDate.get(Calendar.YEAR))*12 
						+ thatDate.get(Calendar.MONTH) - thisDate.get(Calendar.MONTH)) % period.interval == 0; 
			case YEARLY:
				return  thatDate.get(Calendar.DAY_OF_YEAR) == thisDate.get(Calendar.DAY_OF_YEAR)
						&& (thatDate.get(Calendar.YEAR) - thisDate.get(Calendar.YEAR)) % period.interval == 0;
		}
		return false;
	}
	
	public boolean isRepeatable() {
		return period.isRepeatable();
	}
	
	public boolean isEveryWeek() {
		return period.isEveryWeek();
	}
	
	/*
	 * return true if event is valid
	 */
	public boolean isOk() {
		if (period == null || !period.isOk() || date == null ||
			name == null || place == null || note == null || 
			name.length() > Event.MAX_NAME_LENGTH || name.length() < Event.MIN_NAME_LENGTH || 
			note.length() > Event.MAX_NOTE_LENGTH || place.length() > Event.MAX_PLACE_LENGTH) {
			
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public boolean equals(Object other) {
	    if (!(other instanceof Event)) {
	        return false;
	    }
	    Event that = (Event) other;
	    return this.id == that.id
	    	&& TimetableFunctional.areEqualOrNulls(this.name, that.name)
	        && TimetableFunctional.areEqualOrNulls(this.place, that.place)
	        && TimetableFunctional.areEqualOrNulls(this.date, that.date)
	        && TimetableFunctional.areEqualOrNulls(this.startTime, that.startTime)
	        && TimetableFunctional.areEqualOrNulls(this.endTime, that.endTime)
	        && TimetableFunctional.areEqualOrNulls(this.note, that.note)
	        && TimetableFunctional.areEqualOrNulls(this.period, that.period);
	}
	
	@Override 
	public String toString() {
		return "---------------\nName: " + name + "\nPlace: " + place + 
				"\nDate: " + date.toString() + "\nNote: " + note + "\n" + period.toString()+ "\n---------------\n";
	}
}
