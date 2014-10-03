package com.timetable.android.uitests;

import java.text.ParseException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.RenamingDelegatingContext;

import com.timetable.android.Event;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.alarm.AlarmDialogActivity;

/*
 * Class for testing AlarmDialogAcitivty. Just run this test and activity will be started with test event. 
 */
public class AlarmDialogActivityTestCase extends ActivityInstrumentationTestCase2<AlarmDialogActivity> {
	
	public AlarmDialogActivityTestCase() {
		super(AlarmDialogActivity.class);
	}
	
	Activity mActivity;
	
	Context mContext;
	
	@Override
	public void setUp() throws ParseException {
		
		Intent i = new Intent();
		Event event = new Event.Builder()
							.setName("event_name")
							.setDate("13.08.2014")
							.setStartTime("14:31")
							.setAlarmTime("13.08.2014 13:31")
							.build();
		
		i.putExtras(event.convert());
		setActivityIntent(i);
		
		mActivity = getActivity();
		mContext = new RenamingDelegatingContext(mActivity, "TimetableDatabaseTestCase_");
		TimetableDatabase db = TimetableDatabase.getInstance(mContext);
		db.insertEvent(event);
	}
	
	public void testActivity() throws InterruptedException {
	}
	
	@Override 
	public void tearDown() {
		//keep activity alive.
	}
}
