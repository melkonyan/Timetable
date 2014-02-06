package com.example.timetable;

import java.util.EnumMap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class EventActionBar {
	
	private ActionBar actionBar;
	
	private EnumMap<Page,String> titles = new EnumMap<Page, String>(Page.class);
	
	public EventActionBar(ActionBarActivity activity) {
		actionBar = (activity).getSupportActionBar();
		setTitle(Page.EVENT_ADD,"Add event");
		setTitle(Page.EVENT_EDIT,"Edit event");
	}
	
	public void setTitle(Page page, String title) {
		titles.put(page, title);
	}
	
	public void showTitle(Page page) {
		if (titles.containsKey(page)) {
			actionBar.setTitle(titles.get(page));
		}
	}
	
}
