package com.timetable.android.alarm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;

import com.timetable.android.Event;
import com.timetable.android.Logger;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.Utils;

/*
 * Class, that contains all alarm's data, and has method's to work with this data.
 */
public class EventAlarm {
	
	public enum Type {
		ALARM
	}
	
	public static int INITIAL_ALARM_ID = -1;
	
	public static final String BUNDLE_ALARM_ID = "alm_id";
	
	public static final String BUNDLE_ALARM_TYPE = "alm_type";
	
	public static final String BUNDLE_ALARM_TIME = "alm_time";
	
	public int id = INITIAL_ALARM_ID;
	
	public EventAlarm.Type type = EventAlarm.Type.ALARM;
	
	public Date time;

	public Event event;

	public static final SimpleDateFormat timeFormat = DateFormatFactory.getDateTimeFormat();
	
	/*
	 * Create alarm for given @event with given @data.
	 */
	public EventAlarm(Bundle data, Event event) throws ParseException {
		this(event);
		id = data.getInt(BUNDLE_ALARM_ID);
		type = EventAlarm.Type.values()[data.getInt(BUNDLE_ALARM_TYPE)];
		setTime(data.getString(BUNDLE_ALARM_TIME));
	}
	
	/*
	 * Construct alarm for given @event.
	 */
	public EventAlarm(Event event) {
		this();
		this.event = event;
	}
	
	@Deprecated
	public EventAlarm() {
	}
	
	/*
	 * Create bundle, that contains all information about alarm.
	 */
	public Bundle convert() {
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_ALARM_ID, id);
		bundle.putInt(BUNDLE_ALARM_TYPE, type.ordinal());
		bundle.putString(BUNDLE_ALARM_TIME, getTimeString());
		return bundle;
	}

	/*
	 * Return true if this alarm has not been inserted into the database yet.
	 */
	public boolean isNew() {
		return id == INITIAL_ALARM_ID;
	}
	
	/*
	 * Return true, if alarm time is set.
	 */
	public boolean hasTime() {
		return time != null;
	}
	
	public long getTimeMillis() {
		return hasTime() ? time.getTime() : 0;
	}
	
	/*
	 * Get alarm time, formated to string.
	 */
	public String getTimeString() {
		return hasTime() ? timeFormat.format(time) : "";
	}
	
	public void setTime(long millis) {
		time = new Date(millis);
	}
	
	/*
	 * Set alarm time, given a string, that suits alarm's time format.
	 */
	public void setTime(String timeString) throws ParseException {
		time = DateUtils.getDateFromString(timeFormat, timeString, false);
	}
	
	
	/*
	 * If alarm is on given @alarmOccurrence, compute the date of appropriate event, to which this alarm is set.
	 */
	public Date getEventOccurrence(Date alarmOccurrence) {
		if (event == null) {
			return null;
		}
		return new Date(DateUtils.extractDate(alarmOccurrence).getTime() + (event.getDate().getTime() - DateUtils.extractDate(time).getTime()));
	}
	
	/*
	 * If event is on given @eventOccurrence, compute the time of it's alarm.
	 */
	public Date getAlarmOccurrence(Date eventOccurrence) {
		if (event == null) {
			return null;
		}
		return new Date(DateUtils.extractDate(eventOccurrence).getTime() + (time.getTime() - event.getDate().getTime()));
	}
	
	
	/*
	 * Get event occurrence of next alarm occurrence for given date.
	 */
	public Date getNextEventOccurrence(Date today) {
		return getEventOccurrence(getNextOccurrence(today));
	}
	
	
	/*
	 * Get next occurrence of alarm for current date.
	 */
	public Date getNextOccurrence() {
		return getNextOccurrence(Utils.getCurrDateTime());
	
	}
	
	/*
	 * Get next alarm occurrence for given date.
	 */
	public Date getNextOccurrence(Date today) {
		if (event == null) {
			Logger.error("EventAlarm.getNextOccurrence: Alarm has no reference to event;");
			return null;
		}
		
		Date todayDate = today;
		if (DateUtils.compareTimes(time, today) != DateUtils.AFTER) {
			todayDate = DateUtils.addDay(todayDate, 1);
		}
		
		Date nextEventOccurrence = event.getNextOccurrence(getEventOccurrence(todayDate));
		if (nextEventOccurrence == null) {
			return null;
		}
		return getAlarmOccurrence(nextEventOccurrence);
	}
	
	
	public Date getTimeTillNextOccurrence() {
		return getTimeTillNextOccurrence(Utils.getCurrDateTime());
	}
	
	/*
	 * Compute time till next alarm occurrence.
	 */
	public Date getTimeTillNextOccurrence(Date today) {
		Date nextOccurrence = getNextOccurrence(today);
		if (nextOccurrence == null) {
			return null;
		}
		return new Date(nextOccurrence.getTime() - today.getTime());
	}
	
	/*
	 * Return true, if alarm's type and time are both set.
	 * Alarm should also has a reference to event, to which it is set.
	 */
	public boolean isOk() {
		return type != null && time != null && event != null;	
	}
	
	/*
	 * Convert alarm to string. 
	 */
	@Override 
	public String toString() { 
		return "Alarm. Type: " + type.toString() + "; Time: " + time.toString() + "\n";  
	}
	
	/*
	 * Return true if two alarm have the same type, time and id.
	 */
	@Override 
	public boolean equals(Object other) {
		if (!(other instanceof EventAlarm)) {
	        return false;
	    }
	    EventAlarm that = (EventAlarm) other;
	    return Utils.areEqualOrNulls(this.time, that.time) && this.type == that.type && this.id == that.id;
	}
}
