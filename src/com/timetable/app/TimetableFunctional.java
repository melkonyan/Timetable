package com.timetable.app;

import com.timetable.app.R;
import java.util.Calendar;
import java.util.Date;

public class TimetableFunctional {
	
	public static boolean areEqualOrNulls (Object obj1, Object obj2) {
		return (obj1 == null && obj2 == null || obj1 != null && obj2 != null && obj1.equals(obj2));
	}
	
	/* 
	 * returns true if dates are equal(time does not matter)
	 */
	public static boolean areSameDates(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		
		cal1.setTime(date1);
		cal2.setTime(date2);
		
		return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && 
			   cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
	}
	
	
}
