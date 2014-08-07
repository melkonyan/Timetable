package com.timetable.android.activities;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.datetimepicker.date.DatePickerDialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.timetable.android.EventPager;
import com.timetable.android.EventService;
import com.timetable.android.R;
import com.timetable.android.TimetableLogger;
import com.timetable.android.alarm.AlarmServiceManager;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.TimetableUtils;


/*
 * Activity that displays all events of certain day
 * User can view events of next or previous day by shifting page right or left
 */
public class EventDayViewActivity extends Activity {
	
	public static final SimpleDateFormat ACTION_BAR_DATE_FORMAT = new SimpleDateFormat("EEE, dd.MM", Locale.US); 
	
	public static final String EXTRAS_DATE = "date";
	
	public static final SimpleDateFormat EXTRAS_DATE_FORMAT = DateFormatFactory.getDateFormat();
	
	private LinearLayout eventLayout;
	
	private EventPager eventPager;
	
	private DatePickerDialog datePickerDialog;
	
	private EventPagerListener mListener = new EventPagerListener();
	
	public EventPager getEventPager() {
		return eventPager;
	}
	
	public void setEventPager(EventPager eventPager) {
		if (this.eventPager != null) {
			eventLayout.removeView(this.eventPager);
		}
		
		this.eventPager = eventPager;
		this.eventPager.prepare();
		eventLayout.addView(eventPager,0);
		
	}

	public EventPagerListener getEventPagerListener() {
		return mListener;
	}
	
	private Date getExtraDate() {
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return null;
		}
		String dateString = extras.getString(EXTRAS_DATE);
		if (dateString == null) {
			return null;
		}
		try {
			return EXTRAS_DATE_FORMAT.parse(dateString);
		} catch (ParseException e) {
			TimetableLogger.error("EventDayViewActivity.getExtraDate: could not parse date");
			return null;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.activity_event_day_view);
		
		//TimetableDatabase.getInstance(this).clear();
		
		//enable debugging
		TimetableLogger.debugging = true;
		
		//start AlarmService
		AlarmServiceManager.startService(this);
		
		//start EventService
		EventService.startService(this);
		
		eventLayout = (LinearLayout) findViewById(R.id.events_table);
		setEventPager(new EventPager(this, TimetableUtils.getCurrentTime()));
		DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePickerDialog dialog, int year,
					int monthOfYear, int dayOfMonth) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				cal.set(Calendar.MONTH, monthOfYear);
				//getEventPager().goToDate(cal.getTime());
				setEventPager(new EventPager(EventDayViewActivity.this, cal.getTime()));
				
			}
			
		};
		
		Date startDate = getExtraDate();
		if (startDate != null) {
			TimetableLogger.error("Start EventDayViewActivity with extra date: " + startDate.toString());
			setEventPager(new EventPager(this,startDate));
			//eventPager.goToDate(startDate);
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(getEventPager().getDisplayedDate());
		datePickerDialog = DatePickerDialog.newInstance(mOnDateSetListener, 
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

	}


	@Override
	public void onRestart() {
		TimetableLogger.log("EventDayViewAcwativity was restarted.");
		super.onRestart();
		getEventPager().update();
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
	            eventAddIntent.putExtra(EventAddActivity.INTENT_EXTRA_DATE, EventAddActivity.INIT_DATE_FORMAT.format(getEventPager().getDisplayedDate()));
	            startActivity(eventAddIntent);
	        	return true;
	        case R.id.action_view_today:
	        	setEventPager(new EventPager(this, TimetableUtils.getCurrentTime()));
	        	//eventPager.goToDate(TimetableUtils.getCurrentTime());
	        	return true;
	        case R.id.action_go_to_date:
	        	datePickerDialog.show(getSupportFragmentManager());
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
			eventEditIntent.putExtra("date", EventEditActivity.INIT_DATE_FORMAT.format(getEventPager().getDisplayedDate()));
			startActivity(eventEditIntent);
		} catch (Exception e) {
			TimetableLogger.error(e.toString());
			return;
		}
	}

	
	private class EventPagerListener extends SimpleOnPageChangeListener {
		
		
		public EventPagerListener() {
			super();
			TimetableLogger.log("EventPagerListener successfully created.");
		}
		
		@Override
		public void  onPageSelected(int pageNumber) {
			TimetableLogger.log("EventPagerListener detected page # " + pageNumber + " selection.");
			Date currentDate = getEventPager().getDateByPageNumber(pageNumber);
			
			//update action bar
			String dateString = ACTION_BAR_DATE_FORMAT.format(currentDate);
			if (DateUtils.areSameDates(currentDate, TimetableUtils.getCurrentTime())) {
				dateString = getResources().getString(R.string.actionbar_date_today);
			}
			getSupportActionBar().setTitle(dateString);
		}
	}
}