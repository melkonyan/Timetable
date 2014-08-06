package com.timetable.android;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import com.timetable.android.alarm.EventAlarm;
import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.TimetableUtils;

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
	
	//Dates, on which repeated event has no occurrence, even though it should
	public Set<Date> exceptions = new TreeSet<Date>(new Comparator<Date>() {

		@Override
		public int compare(Date first, Date second) {
			return DateUtils.compareDates(first, second); 
		}
	});
	
	@Deprecated
	public Event(int id) {
		this.id = id;
		this.period = new EventPeriod();
	}
	
	@Deprecated
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
	
	public void addException(Date exception) {
		exceptions.add(exception);
	}
	
	public void deleteException(Date exception) {
		exceptions.remove(exception);
	}
	
	public boolean isException(Date today) {
		return exceptions != null && exceptions.contains(today);
	}
	
	public boolean isToday(Date today) {
		return period.hasOccurrenceOnDate(date, today)  && !isException(today);
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
				"\nDate: " + date.toString() + "\nNote: " + note + "\n" + period.toString()+ 
				(hasAlarm() ? "\n" + alarm.toString() : "No alarm") + "\n---------------\n";
	}
	
	public static class Builder {
		
		private Event event =  new Event();
		
		public Builder() {
			
		}
		
		public Builder setName(String name) {
			event.name = name;
			return this;
		}
		
		public Builder setPlace(String place) {
			event.place = place;
			return this;
		}
		
		public Builder setDate(Date date) {
			event.date = date;
			return this;
		}
		
		public Builder setStartTime(Date startTime) {
			event.startTime = startTime;
			return this;
		}
		
		public Builder setEndTime(Date endTime) {
			event.endTime = endTime;
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
		
		public Builder setPeriodEndDate(Date endDate) {
			event.period.endDate = endDate;
			return this;
		}
		
		public Builder setAlarmTime(Date alarmTime) {
			if (!event.hasAlarm()) {
				event.alarm = new EventAlarm(event);
			}
			event.alarm.time = alarmTime;
			return this;
		}
		
		public Event build() {
			return event;
		}
	}
}
