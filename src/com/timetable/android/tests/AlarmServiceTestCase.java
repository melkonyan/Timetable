package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.timetable.android.Event;
import com.timetable.android.EventPeriod;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.alarm.AlarmService;
import com.timetable.android.alarm.AlarmServiceManager;
import com.timetable.android.alarm.EventAlarm;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.FakeTimeProvider;
import com.timetable.android.utils.TimetableUtils;

public class AlarmServiceTestCase extends AndroidTestCase {
	
	private AlarmServiceManager mManager;
	private Context context;
	private Date currentDate;
	
	private SimpleDateFormat dateFormat = DateFormatFactory.getDateFormat();
	
	private SimpleDateFormat dateTimeFormat = DateFormatFactory.getDateFormat();
	
	private boolean isSetNotification = false;
	
	private AlarmService service;
	
	private ArrayList<EventAlarm> setAlarms = new ArrayList<EventAlarm>();
	
	private boolean isSetNotification() {
		return PendingIntent.getBroadcast(context, AlarmService.ALARM_NOTIFICATION_CODE, 
										new Intent(context, EventDayViewActivity.class), PendingIntent.FLAG_NO_CREATE) != null;
	}
	
	
	public void setUp() throws ParseException, InterruptedException {
		context = new RenamingDelegatingContext(getContext(), "test_");
		mManager = new AlarmServiceManager(context);
		mManager.bindService();
		
		int waitTime = 0;
		while (service == null) {
			if (waitTime == 5) {
				fail("Can not bind to service");
			}
			Thread.sleep(1000);
			waitTime++;
			service = mManager.getService();
		}
		
		isSetNotification = isSetNotification();
		if(isSetNotification) {
			service.deleteNotification();
		}
		
		currentDate = EventAlarm.timeFormat.parse("05.07.2044 20:36");
	}

	public void testAlarmService() throws ParseException {
		
		assertNotNull(service);
		assertEquals(false, isSetNotification());
		
		TimetableUtils.setTimeProvider(new FakeTimeProvider(currentDate));
		
		
		EventAlarm alarm1 = new Event.Builder()
							.setDate(dateFormat.parse("20.09.2044"))
							.setAlarmTime(dateTimeFormat.parse("20.09.2044 20:42"))
							.build()
							.alarm;
		alarm1.id = -1;
		//service.deleteAlarm(alarm1);
		assertEquals(false, service.existAlarm(alarm1));
		
		setAlarms.add(alarm1);
		service.createAlarm(alarm1);
		
		assertEquals(true, service.existAlarm(alarm1));
		assertEquals(true, isSetNotification());
		
		service.updateAlarm(alarm1);
		
		assertEquals(true, service.existAlarm(alarm1));
		assertEquals(true, isSetNotification());
		
		service.deleteAlarm(alarm1);
		
		assertEquals(false, service.existAlarm(alarm1));
		assertEquals(false, isSetNotification());
		
		EventAlarm alarm2 = new Event.Builder()
							.setDate(dateFormat.parse("01.07.2044"))
							.setAlarmTime(dateTimeFormat.parse("01.07.2044 13:34"))
							.setPeriodType(EventPeriod.Type.DAILY)
							.setPeriodInterval(1)
							.build()
							.alarm;
		alarm2.id = -2;
		setAlarms.add(alarm2);
		service.createAlarm(alarm2);
		
		assertEquals(true, service.existAlarm(alarm2));
		assertEquals(true, isSetNotification());
		
		
		alarm2.event.period.endDate = currentDate;
		
		service.updateAlarm(alarm2);
		
		assertEquals(false, service.existAlarm(alarm2));
		assertEquals(false, isSetNotification());
		service.deleteAlarm(alarm1);
		
	}
	
	public void tearDown() {
		
		for (EventAlarm alarm: setAlarms) {
			service.deleteAlarm(alarm);
		}
		
		if (isSetNotification) {
			service.createNotification();
		}
		
		mManager.unbindService();
		
	}
}
