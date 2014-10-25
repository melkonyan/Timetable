package com.timetable.android.activities;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.Logger;

public class EventCopyActivity extends EventAddActivity {
	
	@Override 
	public void onCreate(Bundle savedBundleState) {
		super.onCreate(savedBundleState);
		
		try {
				Bundle extras = getIntent().getExtras();
				setEvent(new Event(extras.getBundle(EXTRA_COPY_EVENT)));
				initEventDate = Calendar.getInstance();
				initEventDate.setTime(INIT_DATE_FORMAT.parse(extras.getString(EventAddActivity.EXTRA_DATE)));
				setEventDate(getInitDate());
				Date copyDate = INIT_DATE_FORMAT.parse(extras.getString(EXTRA_DATE));
				
				
			} catch (ParseException e) {
				Logger.error("EventCopyActivity.onCreate. Incorrect data received");
				finish();
				return;
			} 
			getSupportActionBar().setTitle(getResources().getString(R.string.actionbar_copy_event));
		
		}
		

	
}
