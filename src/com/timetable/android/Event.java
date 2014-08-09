package com.timetable.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import android.os.Bundle;

import com.timetable.android.alarm.EventAlarm;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.TimetableUtils;

public class Event {
	
	public static final int MIN_NAME_LENGTH = 1;
	
	public static final int MAX_NAME_LENGTH = 45;

	public static final int MAX_PLACE_LENGTH = 45;
	
	public static final int MAX_NOTE_LENGTH = 200;
	
	public static final int MAX_PERIOD_INTERVAL_LENGTH = 2;

	public static final String BUNDLE_EVENT_ID = "evt_id";
	
	public static final String BUNDLE_EVENT_NAME = "evt_name";
	
	public static final String BUNDLE_EVENT_PLACE = "evt_place";
	
	public static final String BUNDLE_EVENT_DATE = "evt_date";
	
	public static final String BUNDLE_EVENT_START_TIME = "evt_start_time";
	
	public static final String BUNDLE_EVENT_END_TIME = "evt_end_time";
	
	public static final String BUNDLE_EVENT_MUTE_DEVICE = "evt_mute_device";
	
	public static final String BUNDLE_EVENT_NOTE = "evt_note";
	
	public static final String BUNDLE_EVENT_EXCEPTIONS = "evt_exceptions";
	
	public static final SimpleDateFormat dateFormat = DateFormatFactory.getDateFormat();
	
	public static final SimpleDateFormat timeFormat = DateFormatFactory.getTimeFormat();
	
	public int id;
	
	public String name = "";
	
	public String place = ""; 
	
	public Date startTime;
	
	public Date endTime; 
	
	public Date date;
	
	//indicates, whether a device should be muted during the event
	public boolean muteDevice = false; 
	
	public EventAlarm alarm;
	
	public EventPeriod period;
	
	public String note = "";
	
	//Dates, on which repeated event has no occurrence, even though it should
	public Set<Date> exceptions = new TreeSet<Date>(new Comparator<Date>() {

		@Override
		public int compare(Date first, Date second) {
			return DateUtils.compareDates(first, second); 
		}
	});
	
	public Event(Bundle data) throws ParseException {
		this();
		id = data.getInt(BUNDLE_EVENT_ID);
		name = data.getString(BUNDLE_EVENT_NAME);
		place = data.getString(BUNDLE_EVENT_PLACE);
		setDate(data.getString(BUNDLE_EVENT_DATE));
		setStartTime(data.getString(BUNDLE_EVENT_START_TIME));
		setEndTime(data.getString(BUNDLE_EVENT_END_TIME));
		muteDevice = data.getBoolean(BUNDLE_EVENT_MUTE_DEVICE);
		note = data.getString(BUNDLE_EVENT_NOTE);
		addExceptions(data.getStringArray(BUNDLE_EVENT_EXCEPTIONS));
		period = new EventPeriod(data);
		if (data.containsKey(EventAlarm.BUNDLE_ALARM_ID)) {
			alarm = new EventAlarm(data, this);
		}
	}
	
	public Event(int id) {
		this.id = id;
		this.period = new EventPeriod();
	}
	
	public Event() {
		this(-1);
	}
	
	/*
	 * Save event information into ContentValues
	 */
	public Bundle convert() {
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_EVENT_ID, id);
		bundle.putString(BUNDLE_EVENT_NAME, name);
		bundle.putString(BUNDLE_EVENT_PLACE, place);
		bundle.putString(BUNDLE_EVENT_DATE, getDateString());
		bundle.putString(BUNDLE_EVENT_START_TIME, getStartTimeString());
		bundle.putString(BUNDLE_EVENT_END_TIME, getEndTimeString());
		bundle.putBoolean(BUNDLE_EVENT_MUTE_DEVICE, muteDevice);
		bundle.putString(BUNDLE_EVENT_NOTE, note);
		bundle.putStringArray(BUNDLE_EVENT_EXCEPTIONS, getExceptionStrings());
		bundle.putAll(period.convert());
		if (hasAlarm()) {
			bundle.putAll(alarm.convert());
		}
		return bundle;
	}
	
	
	public void setDate(String dateString) throws ParseException {
		date = DateUtils.getDateFromString(dateFormat,dateString, false);
	}
	
	public String getDateString() {
		return date == null ? "" : dateFormat.format(date);
	}
	
	public boolean hasStartTime() {
		return startTime != null;
	}
	
	public void setStartTime(String startTimeString) throws ParseException {
		startTime = DateUtils.getDateFromString(timeFormat, startTimeString, false);
	}
	
	public String getStartTimeString() {
		return hasStartTime() ?  timeFormat.format(startTime) : "";
	}
	
	public boolean hasEndTime() {
		return endTime != null;
	}
	
	public void setEndTime(String endTimeString) throws ParseException {
		endTime = DateUtils.getDateFromString(timeFormat, endTimeString);
	}
	
	public String getEndTimeString() {
		return hasEndTime() ? timeFormat.format(endTime) : "";
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
	
	public void addExceptions(String[] exceptionDates) throws ParseException {
		for (int i = 0; i < exceptionDates.length; i++) {
			addException(exceptionDates[i]);
		}
	}
	
	public void addException(String exceptionDate) throws ParseException {
		exceptions.add(DateUtils.getDateFromString(dateFormat, exceptionDate, false));
	}
	
	public void addException(Date exception) {
		exceptions.add(exception);
	}
	
	public void deleteException(Date exception) {
		exceptions.remove(exception);
	}
	
	public boolean isException(Date today) {
		return exceptions != null && exceptions.contains(today);
	}
	
	public Date[] getExceptions() {
		return exceptions.toArray(new Date[0]);
	}
	
	public String[] getExceptionStrings() {
		Date[] exDates = getExceptions();
		String[] exStrings = new String[exDates.length];
		for (int i = 0; i < exStrings.length; i++) {
			exStrings[i] = dateFormat.format(exDates[i]);
		}
		return exStrings;
	}
	
	public boolean isToday(Date today) {
		return period.hasOccurrenceOnDate(date, today)  && !isException(today);
	}
	
	public boolean isCurrent() {
		return isCurrent(TimetableUtils.getCurrentTime());
	}
	
	/* 
	 * Return true, if event has occurrence at specified time.
	 */
	public boolean isCurrent(Date currentTime) {
		return hasStartTime() && hasEndTime() && isToday(currentTime) 
					&& DateUtils.compareTimes(startTime, currentTime) != DateUtils.AFTER 
					&& DateUtils.compareTimes(endTime, currentTime) == DateUtils.AFTER;
	}
	
	public Date getNextOccurrence() {
		return getNextOccurrence(TimetableUtils.getCurrentTime());
	}
	
	public Date getNextOccurrence(Date today) {
		Date nextEventDate = today;
		while (true) {
			nextEventDate = period.getNextOccurrence(date, nextEventDate);

			if (nextEventDate == null  || period.isFinished(nextEventDate)) {
				return null;
			}
			if (!isException(nextEventDate)) {
				break;
			}
			nextEventDate = DateUtils.addDay(nextEventDate, 1);
		}
		return nextEventDate;
	}
	
	
	public Date getNextStartTime() {
		return getNextStartTime(TimetableUtils.getCurrentTime());
	}
	
	/*
	 * Return start time of next occurrence
	 */
	public Date getNextStartTime(Date currentTime) {
		if (!hasStartTime()) {
			return null;
		}
		Date nextStartTime = new Date();
		nextStartTime.setTime(currentTime.getTime());
		if (DateUtils.compareTimes(startTime, currentTime) != DateUtils.AFTER) {
			nextStartTime = DateUtils.addDay(nextStartTime, 1);
		}
		nextStartTime = getNextOccurrence(nextStartTime);
		if (nextStartTime == null) {
			return null;
		}
		return DateUtils.setTime(nextStartTime, startTime);
	}
	
	public Date getNexEndTime() {
		return getNextEndTime(TimetableUtils.getCurrentTime());
	}
	
	/*
	 * Return end time of next occurrence
	 */
	public Date getNextEndTime(Date currentTime) {
		if (!hasEndTime()) {
			return null;
		}
		 
		Date nextEndTime = new Date();
		nextEndTime.setTime(currentTime.getTime());
		
		if (DateUtils.compareTimes(endTime, currentTime) != DateUtils.AFTER) {
			nextEndTime = DateUtils.addDay(nextEndTime, 1);
		}
		
		nextEndTime = getNextOccurrence(nextEndTime);
		if (nextEndTime == null) {
			return null;
		}
		return DateUtils.setTime(nextEndTime, endTime);
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
	    	&& this.muteDevice == that.muteDevice
	    	&& TimetableUtils.areEqualOrNulls(this.name, that.name)
	        && TimetableUtils.areEqualOrNulls(this.place, that.place)
	        && TimetableUtils.areEqualOrNulls(this.date, that.date)
	        && TimetableUtils.areEqualOrNulls(this.startTime, that.startTime)
	        && TimetableUtils.areEqualOrNulls(this.endTime, that.endTime)
	        && TimetableUtils.areEqualOrNulls(this.note, that.note)
	        && TimetableUtils.areEqualOrNulls(this.period, that.period)
	        && TimetableUtils.areEqualOrNulls(this.alarm, that.alarm)
	    	&& TimetableUtils.areEqualOrNulls(this.exceptions, that.exceptions);
	    	
	}
	
	
	
	@Override 
	public String toString() {
		return "---------------\nName: " + name + "\nPlace: " + place + 
				"\nDate: " + date.toString() + 
				"\nStart time: " + (hasStartTime() ? startTime.toString() : "none") + 
				"\nEnd time: " + (hasEndTime() ? endTime.toString(): "none") + 
				"\nMute device: " + Boolean.toString(muteDevice) + "\nNote: " + note + "\n" + period.toString()
				+ (hasAlarm() ? "\n" + alarm.toString() : "\nAlarm: none") + "\n---------------\n";
	}
	
	public static class Builder {
		
		private Event event =  new Event();
		
		public Builder() {
			
		}
		
		public Builder setId(int id) {
			event.id = id;
			return this;
		}
		
		public Builder setName(String name) {
			event.name = name;
			return this;
		}
		
		public Builder setPlace(String place) {
			event.place = place;
			return this;
		}
		
		public Builder setDate(String dateString) throws ParseException {
			event.setDate(dateString);
			return this;
		}
		
		public Builder setDate(Date date) {
			event.date = date;
			return this;
		}
		
		public Builder setStartTime(String startTimeString) throws ParseException {
			event.setStartTime(startTimeString);
			return this;
		}
		public Builder setStartTime(Date startTime) {
			event.startTime = startTime;
			return this;
		}
		
		public Builder setEndTime(String endTimeString) throws ParseException {
			event.setEndTime(endTimeString);
			return this;
		}
		
		public Builder setEndTime(Date endTime) {
			event.endTime = endTime;
			return this;
		}
		
		public Builder setMuteDevice(boolean muteDevice) {
			event.muteDevice = muteDevice;
			return this;
		}
		
		public Builder setNote(String note) {
			event.note = note;
			return this;
		}
		
		public Builder setPeriod(EventPeriod period) {
			event.period = period;
			return this;
		}
		
		public Builder setPeriodType(EventPeriod.Type type) {
			event.period.type = type;
			return this;
		}
		
		public Builder setPeriodInterval(int interval) {
			event.period.interval = interval;
			return this;
		}
		
		public Builder setPeriodEndDate(String endDateString) throws ParseException {
			event.period.setEndDate(endDateString);
			return this;
		}
		
		public Builder setPeriodEndDate(Date endDate) {
			event.period.endDate = endDate;
			return this;
		}
		
		public Builder setAlarm(EventAlarm alarm) {
			event.alarm = alarm;
			return this;
		}
		
		public Builder setAlarmTime(String timeString) throws ParseException {
			if (!event.hasAlarm()) {
				event.alarm = new EventAlarm(event);
			}
			event.alarm.setTime(timeString);
			return this;
		}
		
		public Builder setAlarmTime(Date alarmTime) {
			if (!event.hasAlarm()) {
				event.alarm = new EventAlarm(event);
			}
			event.alarm.time = alarmTime;
			return this;
		}
		
		public Builder setExceptions(Set<Date> exceptions) {
			event.exceptions = exceptions;
			return this;
		}
		
		public Builder addException(String exString) throws ParseException {
			event.addException(exString);
			return this;
		}
		public Event build() {
			return event;
		}
	
	}
}
