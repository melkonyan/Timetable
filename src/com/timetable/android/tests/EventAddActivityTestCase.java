package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Spinner;

import android.content.Context;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.RenamingDelegatingContext;
import android.test.UiThreadTest;
import android.view.View;

import com.timetable.android.TimetableDatabase;
import com.timetable.android.activities.EventAddActivity;
import com.timetable.android.R;

public class EventAddActivityTestCase extends ActivityInstrumentationTestCase2<EventAddActivity> {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	
	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	private Calendar initDate;
	
	private Context mContext;
	
	private EventAddActivity mActivity;
	
	private TimetableDatabase db;
	
	
	public EventAddActivityTestCase() {
		super(EventAddActivity.class);
	}

	@Override
	public void setUp() {
		initDate = Calendar.getInstance();
		try {
			initDate.setTime(dateTimeFormat.parse("14.02.2014 23:27"));
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		
		Intent i = new Intent();
		i.putExtra(EventAddActivity.INTENT_EXTRA_DATE, EventAddActivity.INIT_DATE_FORMAT.format(initDate.getTime()));
		setActivityIntent(i);
		
		mContext = new RenamingDelegatingContext(getActivity(), "test_");
		mActivity = getActivity();
		
		
		
	}
	
	private String getFieldText(int fieldId) {
		return ((EditText) mActivity.findViewById(fieldId)).getText().toString();
	}
	
	public void testInitValues() {
		assertNotNull(mActivity);
		assertNotNull(mActivity.getIntent().getExtras());
		assertEquals("14.02.2014", getFieldText(R.id.event_add_date_val));
		assertEquals("00:00", getFieldText(R.id.event_add_start_time_val));
		assertEquals("01:00", getFieldText(R.id.event_add_end_time_val));
		assertEquals("", getFieldText(R.id.event_add_name_val));
		assertEquals("", getFieldText(R.id.event_add_place_val));
		assertEquals("", getFieldText(R.id.event_add_note_val));
		assertEquals(mContext.getResources().getString(R.string.event_add_interval_init_value), getFieldText(R.id.event_period_interval_val));
		boolean [] initWeekOccurances = mActivity.getEventPeriodWeekOccurrences(); 
		assertEquals(true, initWeekOccurances[initDate.get(Calendar.DAY_OF_WEEK) - 1]);
	
		
	}
	
	@UiThreadTest
	public void testViewVisibility() {
		assertEquals(View.GONE, mActivity.findViewById(R.id.event_period_interval_text_left).getVisibility());
		assertEquals(View.GONE, mActivity.findViewById(R.id.event_period_interval_text_right).getVisibility());
		assertEquals(View.GONE, mActivity.findViewById(R.id.event_period_interval_val).getVisibility());
		assertEquals(View.GONE, mActivity.findViewById(R.id.event_period_end_date_spinner).getVisibility());
		assertEquals(View.GONE, mActivity.findViewById(R.id.event_period_end_date_val).getVisibility());
		
	}
	
}
