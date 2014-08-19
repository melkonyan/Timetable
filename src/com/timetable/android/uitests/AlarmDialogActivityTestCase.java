package com.timetable.android.uitests;

import java.text.ParseException;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.timetable.android.Event;
import com.timetable.android.alarm.AlarmDialogActivity;

public class AlarmDialogActivityTestCase extends ActivityInstrumentationTestCase2<AlarmDialogActivity> {
	
	public AlarmDialogActivityTestCase() {
		super(AlarmDialogActivity.class);
	}
	
	Activity mActivity;
	
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
		
	}
	
	public void testActivity() throws InterruptedException {
		Thread.sleep(3000);
	}
}
