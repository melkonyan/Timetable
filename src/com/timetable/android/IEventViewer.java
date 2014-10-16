package com.timetable.android;

import java.util.Date;

/*
 * Interface for class, that shows events. It should be able to navigate to certain date.
 */
public interface IEventViewer {

	public void goToDate(Date date);
	
	public Date getDisplayedDate();
	
	public void update();
}
