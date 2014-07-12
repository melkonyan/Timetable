package com.timetable.android.alarm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.timetable.android.Event;
import com.timetable.android.EventChecker;
import com.timetable.android.EventPeriod;
import com.timetable.android.TimetableLogger;
import com.timetable.android.functional.TimetableFunctional;

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
	
	public Event event;
	
	public EventPeriod period;
	
	public static final SimpleDateFormat timeFormat = EventChecker.alarmTimeFormat;
	
	public EventAlarm() {
		period = new EventPeriod();
	}
	
	public Date getEventOccurrence(Date alarmOccurrence) {
		if (event == null) {
			return null;
		}
		return new Date(alarmOccurrence.getTime() + (event.date.getTime() - time.getTime()));
	}
	
	public Date getNextOccurrence(Date today) {
		if (time.after(today)) {
			if (period.type != EventPeriod.Type.WEEKLY) {
				return time;
			}
		}
		
		Calendar todayCal = Calendar.getInstance();
		todayCal.setTime(today);
		Calendar dateCal = Calendar.getInstance();
		dateCal.setTime(time);
		Calendar ansCal = Calendar.getInstance();
		
		if (todayCal.get(Calendar.HOUR_OF_DAY)*60 + todayCal.get(Calendar.MINUTE) >= dateCal.get(Calendar.HOUR_OF_DAY)*60 + dateCal.get(Calendar.MINUTE)) {
			todayCal.add(Calendar.DATE, 1);
		}
		
		long dateLong = time.getTime(), todayLong = todayCal.getTime().getTime(); 
		long day = 1000*60*60*24, week = 1000*60*60*24*7;
		
		int diff;
		switch(period.type) {
			case NONE:
				return null;
			case DAILY:
				diff = period.interval - (int) (todayLong / day - dateLong / day) % period.interval;
				System.out.println(todayLong + " " + dateLong + " " + todayLong / day + " " + dateLong / day);
				
				if (diff == period.interval) diff = 0;
				TimetableLogger.log(Integer.toString(diff));
				ansCal.setTime(todayCal.getTime());
				ansCal.add(Calendar.DATE, diff);
				System.out.println(diff);
				break;
			case WEEKLY:
				if (today.before(time)) {
					todayCal.setTime(time);
				}
				
				ansCal.setTime(todayCal.getTime());
				ansCal.set(Calendar.HOUR_OF_DAY, dateCal.get(Calendar.HOUR_OF_DAY));
				ansCal.set(Calendar.MINUTE,	dateCal.get(Calendar.MINUTE));
				
				while(!period.hasOccurrenceOnDate(time, ansCal.getTime())) {
					ansCal.add(Calendar.DATE, 1);
					if (period.endDate != null && ansCal.getTime().after(period.endDate)) {
						return null;
					}
				}
				break;
			case MONTHLY:
				diff = period.interval - ((todayCal.get(Calendar.YEAR)- dateCal.get(Calendar.YEAR))*12 
										+ todayCal.get(Calendar.MONTH) - dateCal.get(Calendar.MONTH)) % period.interval;
				if (diff == period.interval && todayCal.get(Calendar.DAY_OF_MONTH) < dateCal.get(Calendar.DAY_OF_MONTH)) diff = 0;
				ansCal.setTime(todayCal.getTime());
				ansCal.add(Calendar.MONTH, diff);
				ansCal.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
				break;
				
			case YEARLY:
				diff = period.interval - (todayCal.get(Calendar.YEAR) - dateCal.get(Calendar.YEAR)) % period.interval;
				if (diff == period.interval && todayCal.get(Calendar.DAY_OF_YEAR) >  dateCal.get(Calendar.DAY_OF_YEAR)) diff = 0;
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
		
		if (period.endDate != null && ansCal.getTime().after(period.endDate)) {
			return null;
		}
		
		return ansCal.getTime();
	}
	

	/*
	 * return true if alarm's type and time are both set
	 */
	public boolean isOk() {
		return type != null && time != null;	
	}
	
	@Override 
	public String toString() { 
		return "Alarm:\nType: " + type.toString() + "\nTime: " + time.toString() + "\nPeriod: " + period.toString();  
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
