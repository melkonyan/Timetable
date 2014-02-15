package com.example.timetable;


import java.util.Calendar;
import java.util.Date;

import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;


/*
 * Activity that displays all events of certain day
 * User can view events of next or previous day by shifting page right or left
 */
public class EventDayViewActivity extends ActionBarActivity {
	
	private LinearLayout eventLayout;
	
	private EventPager eventPager;
	
	private DatePickerDialog datePickerDialog;
	
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
		
		DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				cal.set(Calendar.MONTH, monthOfYear);
				setEventPager(new EventPager(EventDayViewActivity.this, cal.getTime()));
			}
			
		};
		Calendar cal = Calendar.getInstance();
		cal.setTime(getEventPager().getDate());
		datePickerDialog = new DatePickerDialog(EventDayViewActivity.this, mOnDateSetListener, 
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

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
	        case R.id.action_go_to_date:
	        	datePickerDialog.show();
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
			TimetableLogger.error(e.toString());
			return;
		}
	}
}
