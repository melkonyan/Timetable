package com.timetable.app;

import java.util.Date;

public class EventAlarm {
	
	public enum Type {
		ALARM
	}
	
	public int id = 0;
	
	public EventAlarm.Type type = EventAlarm.Type.ALARM;
	
	public Date time;

	//Id if event which has this alarm
	public int eventId = 0;
	
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
