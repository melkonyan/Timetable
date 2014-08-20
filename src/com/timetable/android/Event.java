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

/*
 * Class, that contains all information about event and methods to work with it.
 */
public class Event {
	
	public static final int MIN_NAME_LENGTH = 1;
	
	public static final int MAX_NAME_LENGTH = 45;

	public static final int MAX_PLACE_LENGTH = 45;
	
	public static final int MAX_NOTE_LENGTH = 200;
	
	public static final int INIT_EVENT_ID = -1;
	
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
	
	//number of milliseconds, that matches null time;
	public static final long NULL_TIME_MILLIS = -1;
	
	public static final SimpleDateFormat dateFormat = DateFormatFactory.getDateFormat();
	
	public static final SimpleDateFormat timeFormat = DateFormatFactory.getTimeFormat();
	
	public static final Comparator<Date> EXCEPTION_COMPARATOR = new Comparator<Date>() {
		@Override
		public int compare(Date first, Date second) {
			return DateUtils.compareDates(first, second); 
		}
	
	};
	@Deprecated
	public int id;
	
	@Deprecated
	public String name = "";
	
	@Deprecated
	public String place = ""; 
	
	@Deprecated
	public Date date;
	
	@Deprecated
	public Date startTime;
	
	@Deprecated
	public Date endTime; 
	
	//indicates, whether a device should be muted during the event
	@Deprecated
	public boolean muteDevice = false; 
	
	@Deprecated
	public EventAlarm alarm;
	
	@Deprecated
	public String note = "";
	
	@Deprecated
	public EventPeriod period;
	
	//Dates, on which repeated event has no occurrence, even though it should
	@Deprecated
	public Set<Date> exceptions = new TreeSet<Date>(EXCEPTION_COMPARATOR);
	
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
		this(INIT_EVENT_ID);
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
	
	/*
	 * Return true, if event is not inserted into database yet.
	 */
	public boolean isNew() {
		return id == INIT_EVENT_ID;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public Date getDate() {
		return date;
	}

	public String getDateString() {
		return date == null ? "" : dateFormat.format(date);
	}
	
	public long getDateMillis() {
		return date.getTime();
	}
	
	public void setDate(long millis) {
		date = new Date(millis);
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setDate(String dateString) throws ParseException {
		date = DateUtils.getDateFromString(dateFormat,dateString, false);
	}
	
	public boolean hasStartTime() {
		return startTime != null;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public long getStartTimeMillis() {
		return hasStartTime() ? startTime.getTime() : NULL_TIME_MILLIS;
	}
	
	public String getStartTimeString() {
		return hasStartTime() ?  timeFormat.format(startTime) : "";
	}
	
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public void setStartTime(long millis) {
		startTime = millis == NULL_TIME_MILLIS ? null : new Date(millis);
	}
	
	public void setStartTime(String startTimeString) throws ParseException {
		startTime = DateUtils.getDateFromString(timeFormat, startTimeString, false);
	}
	
	public boolean hasEndTime() {
		return endTime != null;
	}
	
	public Date getEndTime() {
		return endTime;
	}

	public long getEndTimeMillis() {
		return hasEndTime() ? endTime.getTime() : NULL_TIME_MILLIS;
	}
	
	public String getEndTimeString() {
		return hasEndTime() ? timeFormat.format(endTime) : "";
	}
	
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public void setEndTime(long millis) {
		endTime = millis == NULL_TIME_MILLIS ? null : new Date(millis);
	}
	
	public void setEndTime(String endTimeString) throws ParseException {
		endTime = DateUtils.getDateFromString(timeFormat, endTimeString);
	}
	
	public void deleteEndTime() {
		endTime = null;
	}
	
	/*
	 * Return true, if device should be muted during event.
	 */
	public boolean mutesDevice() {
		return muteDevice;
	}

	public void setMuteDevice(boolean muteDevice) {
		this.muteDevice = muteDevice;
	}

	public boolean hasAlarm() {
		return alarm != null;
	}
	
	public EventAlarm getAlarm() {
		return alarm;
	}

	public void setAlarm(EventAlarm alarm) {
		this.alarm = alarm;
		if (alarm != null) {
			this.alarm.event = this;
		}
	}

	public void deleteAlarm() {
		alarm = null;
	}
	
	public EventPeriod getPeriod() {
		return period;
	}

	public void setPeriod(EventPeriod period) {
		this.period = period;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Set<Date> getExceptions() {
		return exceptions;
	}
	
	public Date[] getExceptionDates() {
		return exceptions.toArray(new Date[0]);
	}
	
	public String[] getExceptionStrings() {
		Date[] exDates = getExceptionDates();
		String[] exStrings = new String[exDates.length];
		for (int i = 0; i < exStrings.length; i++) {
			exStrings[i] = dateFormat.format(exDates[i]);
		}
		return exStrings;
	}
	
	public void setExceptions(Set<Date> exceptions) {
		this.exceptions = exceptions;
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
	
	
	
	/*
	 * Return true, if event is repeatable.
	 */
	public boolean isRepeatable() {
		return period.isRepeatable();
	}
	
	/*
	 * Return true, if event has weekly period.
	 */
	public boolean isEveryWeek() {
		return period.isEveryWeek();
	}
	
	/*
	 * Return true, if event has period on given date.
	 */
	public boolean isToday(Date today) {
		return !isException(today) && period.hasOccurrenceOnDate(date, today);
	}
	
	/*
	 * Return true, if event has period on current date.
	 */
	public boolean isToday() {
		return isToday(TimetableUtils.getCurrentTime());
	}
	
	/*
	 * Return true, if event is taking place right now.
	 */
	public boolean isCurrent() {
		return isCurrent(TimetableUtils.getCurrentTime());
	}
	
	/* 
	 * Return true, if event takes place at specified time.
	 */
	public boolean isCurrent(Date currentTime) {
		return hasStartTime() && hasEndTime() && isToday(currentTime) 
					&& DateUtils.compareTimes(startTime, currentTime) != DateUtils.AFTER 
					&& DateUtils.compareTimes(endTime, currentTime) == DateUtils.AFTER;
	}
	
	/*
	 * Return nearest event's occurrence, that is later than current date.
	 */
	public Date getNextOccurrence() {
		return getNextOccurrence(TimetableUtils.getCurrentTime());
	}
	
	/*
	 * Return nearest event's occurrence, that is later than given date.
	 */
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
	
	/*
	 * Return nearest event's start time, that is later than current time.
	 */
	public Date getNextStartTime() {
		return getNextStartTime(TimetableUtils.getCurrentTime());
	}
	
	/*
	 * Return start time of next occurrence.
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
	    return this.getId() == that.getId()
	    	&& this.mutesDevice() == that.mutesDevice()
	    	&& TimetableUtils.areEqualOrNulls(this.getName(), that.getName())
	        && TimetableUtils.areEqualOrNulls(this.getPlace(), that.getPlace())
	        && TimetableUtils.areEqualOrNulls(this.getDate(), that.getDate())
	        && TimetableUtils.areEqualOrNulls(this.getStartTime(), that.getStartTime())
	        && TimetableUtils.areEqualOrNulls(this.getEndTime(), that.getEndTime())
	        && TimetableUtils.areEqualOrNulls(this.getNote(), that.getNote())
	        && TimetableUtils.areEqualOrNulls(this.getPeriod(), that.getPeriod())
	        && TimetableUtils.areEqualOrNulls(this.getAlarm(), that.getAlarm())
	    	&& TimetableUtils.areEqualOrNulls(this.getExceptions(), that.getExceptions());
	    	
	}
	
	
	
	@Override 
	public String toString() {
		return "---------------\nName: " + name + "\nPlace: " + place + 
				"\nDate: " + getDate().toString() + 
				"\nStart time: " + (hasStartTime() ? startTime.toString() : "none") + 
				"\nEnd time: " + (hasEndTime() ? endTime.toString(): "none") + 
				"\nMute device: " + Boolean.toString(muteDevice) + "\nNote: " + note + "\n" + period.toString()
				+ (hasAlarm() ? "\n" + alarm.toString() : "\nAlarm: none") + "\n---------------\n";
	}
	
	/*
	 * Class that should be used to create events.
	 */
	public static class Builder {
		
		private Event event =  new Event();
		
		public Builder() {
			
		}
		
		public Builder setId(int id) {
			event.setId(id);
			return this;
		}
		
		public Builder setName(String name) {
			event.setName(name);
			return this;
		}
		
		public Builder setPlace(String place) {
			event.setPlace(place);
			return this;
		}
		
		public Builder setDate(String dateString) throws ParseException {
			event.setDate(dateString);
			return this;
		}
		
		public Builder setDate(Date date) {
			event.setDate(date);
			return this;
		}
		
		public Builder setDate(long millis) {
			event.setDate(millis);
			return this;
		}
	
		public Builder setStartTime(long millis) {
			event.setStartTime(millis);
			return this;
		}
		
		public Builder setStartTime(String startTimeString) throws ParseException {
			event.setStartTime(startTimeString);
			return this;
		}
		
		public Builder setStartTime(Date startTime) {
			event.setStartTime(startTime);
			return this;
		}
		
		public Builder setEndTime(long millis) {
			event.setEndTime(millis);
			return this;
		}
		
		public Builder setEndTime(String endTimeString) throws ParseException {
			event.setEndTime(endTimeString);
			return this;
		}
		
		public Builder setEndTime(Date endTime) {
			event.setEndTime(endTime);
			return this;
		}
		
		public Builder setMuteDevice(boolean muteDevice) {
			event.setMuteDevice(muteDevice);
			return this;
		}
		
		public Builder setNote(String note) {
			event.setNote(note);
			return this;
		}
		
		public Builder setPeriod(EventPeriod period) {
			event.setPeriod(period);
			return this;
		}
		
		public Builder setPeriodType(EventPeriod.Type type) {
			event.period.setType(type);
			return this;
		}
		
		public Builder setPeriodInterval(int interval) {
			event.period.setInterval(interval);
			return this;
		}
		
		public Builder setPeriodEndDate(String endDateString) throws ParseException {
			event.period.setEndDate(endDateString);
			return this;
		}
		
		public Builder setPeriodEndDate(Date endDate) {
			event.period.setEndDate(endDate);
			return this;
		}
		
		public Builder setAlarm(EventAlarm alarm) {
			event.setAlarm(alarm);
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
			event.setExceptions(exceptions);
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
