package com.example.timetable;


import java.util.Calendar;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/*
 * Activity that displays all events of certain day
 * User can view events of next or previous day by shifting page right or left
 */
public class EventDayViewActivity extends ActionBarActivity {
	
	private LinearLayout eventLayout;
	
	private EventPager eventPager;
	
	public EventPager getEventPager() {
		return eventPager;
	}
	
	public void setEventPager(EventPager eventPager) {
		if (eventPager != null) {
			eventLayout.removeView(eventPager);
		}
		this.eventPager = eventPager;
		eventLayout.addView(eventPager,0);
		
	}
	
	public Date getCurrentTime() {
		return Calendar.getInstance().getTime();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_event_day_view);
		
		//enable debugging
		TimetableLogger.debugging = true;
		eventLayout = (LinearLayout) findViewById(R.id.events_table);
		setEventPager(new EventPager(this, getCurrentTime()));
	}


	@Override
	public void onRestart() {
		super.onRestart();
		getEventPager().update();
		TimetableLogger.log("EventDayViewActivity was restarted.");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_event_view, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_add_event:
	            Intent eventAddIntent = new Intent(this, EventAddActivity.class);
	            eventAddIntent.putExtra("date", EventAddActivity.INIT_DATE_FORMAT.format(getEventPager().getDate()));
	            startActivity(eventAddIntent);
	        	return true;
	        case R.id.action_view_today:
	        	setEventPager(new EventPager(this, getCurrentTime()));
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onEventViewClick(View v) {
		try {
			String idString = ((TextView) (((RelativeLayout) v).getChildAt(0))).getText().toString();
			Intent eventEditIntent = new Intent(this, EventEditActivity.class);
			eventEditIntent.putExtra("event_id", Integer.parseInt(idString));
			eventEditIntent.putExtra("date", EventEditActivity.INIT_DATE_FORMAT.format(getEventPager().getDate()));
			startActivity(eventEditIntent);
		} catch (Exception e) {
			TimetableLogger.log(e.getMessage());
			return;
		}
	}
}
