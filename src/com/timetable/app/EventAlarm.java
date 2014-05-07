package com.timetable.app;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventAlarm {
	
	public enum Type {
		ALARM
	}
	
	public static int INITIAL_ALARM_ID = -1;
	
	public int id = INITIAL_ALARM_ID;
	
	public EventAlarm.Type type = EventAlarm.Type.ALARM;
	
	public Date time;

	//Id if event which has this alarm
	public int eventId = 0;

	public static final SimpleDateFormat timeFormat = EventChecker.alarmTimeFormat;
	
	public EventAlarm() {
		
	}
	
	/*
	 * return true if alarm's type and time are both set
	 */
	public boolean isOk() {
		return type != null && time != null;	
	}
	
	@Override 
	public String toString() { 
		return "Alarm:\nType: " + type.toString() + "\nTime: " + time.toString() + "\n";  
	}
	
	/*
	 * return true if two alarm have the same type and time
	 * */
	@Override 
	public boolean equals(Object other) {
		if (!(other instanceof EventAlarm)) {
	        return false;
	    }
	    EventAlarm that = (EventAlarm) other;
	    return TimetableFunctional.areEqualOrNulls(this.time, that.time) && this.type == that.type
	    		&& this.eventId == that.eventId && this.id == that.id;
	}
}
