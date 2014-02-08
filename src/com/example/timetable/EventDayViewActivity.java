package com.example.timetable;


import java.util.Calendar;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

/*
 * Activity that displays all events of certain day
 * User can view events of next or previous day by shifting page right or left
 */
public class EventDayViewActivity extends ActionBarActivity {
	
	Menu menu; 
	
	private LinearLayout eventLayout;
	
	private EventPager eventPager;
	
	private EventAdder eventAdder;
	
	private EventEditor eventEditor;
	
	private EventActionBar eventActionBar;
	
	private Page currentPage = Page.EVENT_VIEW;
	
	private MenuItem menuSaveEvent;
    
	private MenuItem menuAddEvent;
	
	private MenuItem menuDeleteEvent;
	
	public EventAdder getEventAdder() {
		return eventAdder;
	}
	
	public void setEventAdder(EventAdder eventAdder) {
		this.eventAdder = eventAdder;
	}
	
	public EventEditor getEventEditor() {
		return eventEditor;
	}
	
	public void setEventEditor(EventEditor eventEditor) {
		this.eventEditor = eventEditor;
	}
	
	public EventPager getEventPager() {
		return eventPager;
	}
	
	public void setEventPager(EventPager eventPager) {
		this.eventPager = eventPager;
	}
	
	public Date getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		return cal.getTime();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		//enable debugging
		TimetableLogger.debugging = true;
		
		setContentView(R.layout.activity_event_day_view);
		
		eventLayout = (LinearLayout) findViewById(R.id.events_table);
		eventActionBar = new EventActionBar(this);
		setEventPager(new EventPager(this, eventActionBar, getCurrentTime()));
		eventLayout.addView(eventPager,0);
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
		this.menu = menu;
		//menuSaveEvent = menu.findItem(R.id.action_save_event);
		menuAddEvent = menu.findItem(R.id.action_add_event);
		//menuDeleteEvent = menu.findItem(R.id.action_delete_event);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_add_event:
	            Intent eventAddIntent = new Intent(this, EventAddActivity.class);
	            eventAddIntent.putExtra("date", EventAddActivity.INIT_DATE_FORMAT.format(getCurrentTime()));
	            startActivity(eventAddIntent);
	        	return true;
	       default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/*public void onEventViewClick(View v) {
		try {
			String idString = ((TextView) (((RelativeLayout) v).getChildAt(0))).getText().toString();
			TimetableDatabase db = new TimetableDatabase(this);
			Event event = db.searchEventById(Integer.parseInt(idString));
			db.close();
			setEventEditor(new EventEditor(this, event, getEventPager().getDate()));
			switchPage(Page.EVENT_EDIT);
		} catch (Exception e) {
			TimetableLogger.log(e.getMessage());
			return;
		}
	}*/
	
	
}
