package com.example.timetable;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class EventActionBar {
	
	private ActionBar actionBar;
	
	private String title;
	
	public EventActionBar(ActionBarActivity activity) {
		actionBar = (activity).getSupportActionBar();
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void showTitle(Page page) {
		actionBar.setTitle(title);
	}
	
}
