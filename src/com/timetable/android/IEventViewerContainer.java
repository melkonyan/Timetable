package com.timetable.android;

import java.util.Date;

public interface IEventViewerContainer {

	public Date getInitDate();
	
	public void setActionBarTitle(String title);
	
	public void setActionBarSubTitle(String subTitle);
}
