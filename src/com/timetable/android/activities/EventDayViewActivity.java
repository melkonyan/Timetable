package com.timetable.android.activities;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.datetimepicker.date.DatePickerDialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;

import com.timetable.android.EventPager;
import com.timetable.android.R;
import com.timetable.android.TimetableLogger;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.Utils;


/*
 * Activity that displays all events for certain day.
 * User can view events for next or previous day by shifting page right or left.
 */
public class EventDayViewActivity extends Activity {
	
	public static final SimpleDateFormat ACTION_BAR_DATE_FORMAT = new SimpleDateFormat("EEE, dd.MM", Locale.US); 
	
	public static final String EXTRAS_DATE = "date";
	
	public static final SimpleDateFormat EXTRAS_DATE_FORMAT = DateFormatFactory.getDateFormat();
	
	public static final int EVENT_ADD_ACTIVITY_REQUEST_CODE = 10001;
	
	private Date displayedDate;
	
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
		
		TimetableLogger.error(Long.toString(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis()));
		
		eventLayout = (LinearLayout) findViewById(R.id.events_table);
		setEventPager(new EventPager(this, Utils.getCurrDateTime()));
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
			TimetableLogger.log("Start EventDayViewActivity with extra date: " + startDate.toString());
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
		super.onRestart();
		TimetableLogger.log("EventDayViewAcwativity was restarted.");
		getEventPager().update();
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		TimetableLogger.sendReport();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EVENT_ADD_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
			long dateMillis = data.getLongExtra(EXTRAS_DATE, -1);
			if (dateMillis == -1) {
				TimetableLogger.error("EventDayViewActivity.onActivityResult: Invalid date returned.");
				return;
			}
			Date date = new Date(dateMillis);
			setEventPager(new EventPager(this, date));
		}
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
	            eventAddIntent.putExtra(EventAddActivity.EXTRA_DATE, EventAddActivity.INIT_DATE_FORMAT.format(getEventPager().getDisplayedDate()));
	            startActivityForResult(eventAddIntent, EVENT_ADD_ACTIVITY_REQUEST_CODE);
	        	return true;
	        case R.id.action_view_today:
	        	setEventPager(new EventPager(this, Utils.getCurrDateTime()));
	        	//eventPager.goToDate(Utils.getCurrentTime());
	        	return true;
	        case R.id.action_go_to_date:
	        	Calendar currDate = Calendar.getInstance();
	        	currDate.setTime(displayedDate);
	        	datePickerDialog.setDate(currDate.get(Calendar.YEAR), currDate.get(Calendar.MONTH), currDate.get(Calendar.DAY_OF_MONTH));
	        	datePickerDialog.show(getSupportFragmentManager());
	        	return true;
	        case R.id.action_settings:
	        	startActivity(new Intent(this, SettingsActivity.class));
	        default:
	            return super.onOptionsItemSelected(item);
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
			displayedDate = getEventPager().getDateByPageNumber(pageNumber);
			
			//update action bar
			String dateString = ACTION_BAR_DATE_FORMAT.format(displayedDate);
			if (DateUtils.areSameDates(displayedDate, Utils.getCurrDateTime())) {
				dateString = getResources().getString(R.string.actionbar_date_today);
			}
			getSupportActionBar().setTitle(dateString);
		}
	}


	
}