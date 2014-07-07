package com.timetable.android;

import com.timetable.android.alarm.EventAlarm;
import com.timetable.android.functional.TimetableFunctional;
import com.timetable.app.R;

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
	
	public EventAlarm alarm;
	
	public EventPeriod period;
	
	public String note = "";
	
	public Event(int id) {
		this.id = id;
		this.period = new EventPeriod();
	}
	
	public Event() {
		this(-1);
	}
	
	public boolean hasAlarm() {
		return alarm != null;
	}
	
	public boolean isRepeatable() {
		return period.isRepeatable();
	}
	
	public boolean isEveryWeek() {
		return period.isEveryWeek();
	}
	
	public boolean isToday(Date today) {
		return period.hasOccurrenceOnDate(date, today);
	}
	/*
	 * return true if event is valid
	 */
	public boolean isOk() {
		if (period == null || !period.isOk()  || hasAlarm() && !alarm.isOk() || date == null ||
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
	        && TimetableFunctional.areEqualOrNulls(this.period, that.period)
	        && TimetableFunctional.areEqualOrNulls(this.alarm, that.alarm);
	}
	
	@Override 
	public String toString() {
		return "---------------\nName: " + name + "\nPlace: " + place + 
				"\nDate: " + date.toString() + "\nNote: " + note + "\n" + period.toString()+ 
				(hasAlarm() ? "\n" + alarm.toString() : "No alarm") + "\n---------------\n";
	}
}
