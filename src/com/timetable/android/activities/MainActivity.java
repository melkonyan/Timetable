package com.timetable.android.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.datetimepicker.date.DatePickerDialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.timetable.android.IEventViewer;
import com.timetable.android.IEventViewerContainer;
import com.timetable.android.R;
import com.timetable.android.TimetableLogger;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.Utils;

public class MainActivity extends Activity implements IEventViewerContainer {

	private IEventViewer mEventViewer;
	
	public static final int EVENT_ADD_ACTIVITY_REQUEST_CODE = 10001;
	
	private DatePickerDialog mDatePickerDialog;
	
	private ActionBar mActionBar;
	
	public static final String EXTRAS_DATE = "date";
	
	public static final SimpleDateFormat EXTRAS_DATE_FORMAT = DateFormatFactory.getDateFormat();
	
	private Date mInitDate;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mActionBar = getSupportActionBar();
		setInitDate();
		DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePickerDialog dialog, int year,
					int monthOfYear, int dayOfMonth) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				cal.set(Calendar.MONTH, monthOfYear);
				mEventViewer.goToDate(cal.getTime());
			}
			
		};
		
		mDatePickerDialog = DatePickerDialog.newInstance(mOnDateSetListener, 0, 0, 0);
		DayViewFragment fragment = new DayViewFragment(this, mInitDate);
		mEventViewer = fragment;
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.main_container, fragment);
		fragmentTransaction.commit();
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		TimetableLogger.log("MainActivity was restarted.");
		mEventViewer.update();
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
	            eventAddIntent.putExtra(EventAddActivity.EXTRA_DATE, EventAddActivity.INIT_DATE_FORMAT.format(mEventViewer.getDisplayedDate()));
	            startActivityForResult(eventAddIntent, EVENT_ADD_ACTIVITY_REQUEST_CODE);
	        	return true;
	        case R.id.action_view_today:
	        	mEventViewer.goToDate(Utils.getCurrDate());
	        	//eventPager.goToDate(Utils.getCurrentTime());
	        	return true;
	        case R.id.action_go_to_date:
	        	Calendar currDate = Calendar.getInstance();
	        	currDate.setTime(mEventViewer.getDisplayedDate());
	        	mDatePickerDialog.setDate(currDate.get(Calendar.YEAR), currDate.get(Calendar.MONTH), currDate.get(Calendar.DAY_OF_MONTH));
	        	mDatePickerDialog.show(getSupportFragmentManager());
	        	return true;
	        case R.id.action_month_view:
	        	return true;
	        case R.id.action_settings:
	        	startActivity(new Intent(this, SettingsActivity.class));
	        
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/*
	 * Set init date from intent of current date.
	 */
	private void setInitDate() {
		mInitDate = getInitDateFromIntent();
		if (mInitDate == null) {
			mInitDate = Utils.getCurrDate();
		}
	}
	
	/*
	 * Try to get init date from intent.
	 */
	private Date getInitDateFromIntent() {
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
			TimetableLogger.error("DayViewFragment.getExtraDate: could not parse date");
			return null;
		}
	}
	
	@Override 
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		TimetableLogger.log("EventDayViewAvtivity: new intent received!");
		setIntent(intent);
		setInitDate();
		mEventViewer.goToDate(mInitDate);
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		TimetableLogger.sendReport();
	}
	
	
	/*
	 * Method that is called, when EventAddActivity adds event to database. 
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EVENT_ADD_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			long dateMillis = data.getLongExtra(EXTRAS_DATE, -1);
			if (dateMillis == -1) {
				TimetableLogger.error("DayViewFragment.onActivityResult: Invalid date returned.");
				return;
			}
			Date date = new Date(dateMillis);
			mEventViewer.goToDate(date);
		}
	}

	@Override
	public void setActionBarTitle(String title) {
		mActionBar.setTitle(title);
	}

	@Override
	public void setActionBarSubTitle(String subTitle) {
		mActionBar.setSubtitle(subTitle);
	}

	
}
