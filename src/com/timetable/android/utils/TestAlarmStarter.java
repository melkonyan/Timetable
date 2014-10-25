package com.timetable.android.utils;

import java.text.ParseException;

import android.content.Context;
import android.content.Intent;

import com.timetable.android.Event;
import com.timetable.android.Logger;
import com.timetable.android.alarm.AlarmDialogActivity;

public class TestAlarmStarter {
	public static void startAlarm(Context context) {
		try {
			Event event = new Event.Builder()
							.setDate("03.10.2014")
							.setStartTime("19:02")
							.setAlarmTime("13.10.2014 20:00")
							.build();
			Intent intent = new Intent(context, AlarmDialogActivity.class);
			intent.putExtras(event.convert());
			context.startActivity(intent);
		} catch (ParseException e) {
			Logger.error("TestAlarmStarter: unable to create event: " + e.getMessage());
			return;
		}
	}
}
