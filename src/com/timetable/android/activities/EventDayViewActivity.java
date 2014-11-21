package com.timetable.android.activities;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.datetimepicker.date.DatePickerDialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuItem;

import com.timetable.android.EventController;
import com.timetable.android.EventController.OnEventDeletedListener;
import com.timetable.android.EventPager;
import com.timetable.android.EventView;
import com.timetable.android.EventView.EventViewObserver;
import com.timetable.android.Logger;
import com.timetable.android.R;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.Utils;


/*
 * Activity that displays all events for certain day.
 * User can view events for next or previous day by shifting page right or left.
 */
public class EventDayViewActivity extends Activity implements EventViewObserver, OnEventDeletedListener {
	
	public static final SimpleDateFormat ACTION_BAR_DATE_FORMAT = DateFormatFactory.getFormat("EEE, dd.MM"); 
	
	public static final SimpleDateFormat ACTION_BAR_DATE_FORMAT_WITH_YEAR = DateFormatFactory.getFormat("EEE, dd.MM.yyyy");
	public static final String EXTRAS_DATE = "date";
	
	public static final SimpleDateFormat EXTRAS_DATE_FORMAT = DateFormatFactory.getDateFormat();
	
	public static final int EVENT_ADD_ACTIVITY_REQUEST_CODE = 10001;
	
	private Date mDisplayedDate;
	
	private LinearLayout eventLayout;
	
	private EventPager eventPager;
	
	private DatePickerDialog datePickerDialog;
	
	private EventPagerListener mListener = new EventPagerListener();
	
	private ActionBar mActionBar;
	
	//Event view, that shows it's menu.
	private EventView mSelectedEventView;
	
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
			Logger.error("EventDayViewActivity.getExtraDate: could not parse date");
			return null;
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.log("EventDayViewActivity. Creating activity");
		setContentView(R.layout.activity_event_day_view);
		
		eventLayout = (LinearLayout) findViewById(R.id.events_table);
		mActionBar = getSupportActionBar();
		
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
		
		initEventPager();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(getEventPager().getDisplayedDate());
		datePickerDialog = DatePickerDialog.newInstance(mOnDateSetListener, 
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		
		
	}
	
	/*
	 * Create event pager. 
	 * If intent contains extra date, set current displayed date.
	 */
	private void initEventPager() {
		Date startDate = getExtraDate();
		if (startDate != null) {
			Logger.log("Start EventDayViewActivity with extra date: " + startDate.toString());
			setEventPager(new EventPager(this,startDate));
			//eventPager.goToDate(startDate);
		}
	}


	@Override 
	public void onPause() {
		super.onPause();
		hideOpenedMenu();
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		Logger.log("EventDayViewAcwativity was restarted.");
		getEventPager().update();
	}
	
	@Override 
	public void onDestroy() {
	
		super.onDestroy();
		Logger.sendReport();
	}
	
	@Override 
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Logger.log("EventDayViewAvtivity: new intent received!");
		setIntent(intent);
		initEventPager();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EVENT_ADD_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
			long dateMillis = data.getLongExtra(EXTRAS_DATE, -1);
			if (dateMillis == -1) {
				Logger.error("EventDayViewActivity.onActivityResult: Invalid date returned.");
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
	        	currDate.setTime(mDisplayedDate);
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
		
		private int mCurrentYear;
		
		public EventPagerListener() {
			super();
			mCurrentYear = Utils.getCurrDateTimeCal().get(Calendar.YEAR);
			
			Logger.log("EventPagerListener successfully created.");
		}
		
		@Override
		public void  onPageSelected(int pageNumber) {
			Logger.log("EventPagerListener detected page # " + pageNumber + " selection.");
			mDisplayedDate = getEventPager().getDateByPageNumber(pageNumber);
			
			//update action bar
			
			updateActionBarTitile();
			hideOpenedMenu();
		}

		private void updateActionBarTitile() {
			String titleString;
			Calendar cal = Calendar.getInstance();
			cal.setTime(mDisplayedDate);
			int displayedYear = cal.get(Calendar.YEAR);
			if (displayedYear == mCurrentYear) {
				titleString = ACTION_BAR_DATE_FORMAT.format(mDisplayedDate);
			} else {
				titleString = ACTION_BAR_DATE_FORMAT_WITH_YEAR.format(mDisplayedDate);
			}
			
			String subtitleString = null;
			if (DateUtils.areSameDates(mDisplayedDate, Utils.getCurrDateTime())) {
				subtitleString = getResources().getString(R.string.actionbar_date_today);
			}
			mActionBar.setTitle(titleString);
			mActionBar.setSubtitle(subtitleString);
		}
	}

	/*
	 * Hide menu of currently selected EventView
	 */
	private void hideOpenedMenu() {
		if (mSelectedEventView != null) {
			mSelectedEventView.hideMenu();
			mSelectedEventView = null;
		}
	}
	
	@Override
	public void onEventViewClicked(EventView eventView) {
		if (mSelectedEventView == eventView) {
			eventView.hideMenu();
			mSelectedEventView = null;
		} else {
			hideOpenedMenu();
			eventView.showMenu();
			mSelectedEventView = eventView;
		}
	}

	@Override
	public void onButtonDeleteClicked(EventView eventView) {
		EventController eventController = new EventController(this);
		eventController.setOnEventDeletedListener(this);
		eventController.deleteEvent(eventView.getEvent(), eventPager.getDisplayedDate());
	}

	@Override
	public void onEventDeleted() {
		getEventPager().update();
	}

	@Override
	public void onButtonEditClicked(EventView eventView) {
		Intent eventEditIntent = new Intent(this, EventEditActivity.class);
		eventEditIntent.putExtra(EventEditActivity.EXTRA_EVENT_ID, eventView.getEvent().getId());
		//TODO: put date's millis into extra, instead of formatting and then parsing date string 
		eventEditIntent.putExtra(EventEditActivity.EXTRA_DATE, EventEditActivity.INIT_DATE_FORMAT.format(getEventPager().getDisplayedDate()));
		startActivity(eventEditIntent);
	
	}

	@Override
	public void onButtonCopyClicked(EventView eventView) {
		Intent eventCopyIntent = new Intent(this, EventCopyActivity.class);
		eventCopyIntent.putExtra(EventCopyActivity.EXTRA_COPY_EVENT, eventView.getEvent().convert());
		//TODO: put date's millis into extra, instead of formatting and then parsing date string 
		eventCopyIntent.putExtra(EventCopyActivity.EXTRA_DATE, EventCopyActivity.INIT_DATE_FORMAT.format(getEventPager().getDisplayedDate()));
		this.startActivity(eventCopyIntent);
	}

	@Override
	public void onEventViewLongClicked(EventView eventView) {
		onButtonEditClicked(eventView);
	}


	
}