package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.timetable.android.EventPeriod;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.alarm.AlarmService;
import com.timetable.android.alarm.AlarmServiceManager;
import com.timetable.android.alarm.EventAlarm;
import com.timetable.android.functional.FakeTimeProvider;
import com.timetable.android.functional.TimetableFunctional;

public class AlarmServiceTestCase extends AndroidTestCase {
	
	private AlarmServiceManager mManager;
	private Context context;
	private Date currentDate;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	private boolean isSetNotification = false;
	

	private boolean isSetNotification() {
		return PendingIntent.getBroadcast(context, AlarmService.ALARM_NOTIFICATION_CODE, 
										new Intent(context, EventDayViewActivity.class), PendingIntent.FLAG_NO_CREATE) != null;
	}
	
	
	public void setUp() throws ParseException {
		context = new RenamingDelegatingContext(getContext(), "test_");
		isSetNotification = isSetNotification();
		mManager = new AlarmServiceManager(context);
		mManager.bindService();
		currentDate = EventAlarm.timeFormat.parse("05.07.2044 20:36");
	}

	public void testAlarmService() throws ParseException, InterruptedException {
		AlarmService service = null;
		int waitTime = 0;
		while (service == null) {
			if (waitTime == 5) {
				fail("Can not bind to service");
			}
			Thread.sleep(1000);
			waitTime++;
			service = mManager.getService();
		}
		
		service.deleteNotification();
		assertNotNull(service);
		assertEquals(false, isSetNotification());
		
		TimetableFunctional.setTimeProvider(new FakeTimeProvider(currentDate));
		
		EventAlarm alarm1 = new EventAlarm();
		alarm1.id = -1;
		alarm1.time = EventAlarm.timeFormat.parse("20.09.2044 20:42");
		
		assertEquals(false, service.existAlarm(alarm1));
		
		service.createAlarm(alarm1);
		
		assertEquals(true, service.existAlarm(alarm1));
		assertEquals(true, isSetNotification());
		
		service.updateAlarm(alarm1);
		
		assertEquals(true, service.existAlarm(alarm1));
		assertEquals(true, isSetNotification());
		
		service.deleteAlarm(alarm1);
		
		assertEquals(false, service.existAlarm(alarm1));
		assertEquals(false, isSetNotification());
		
		EventAlarm alarm2 = new EventAlarm();
		alarm2.id = -2;
		alarm2.time = EventAlarm.timeFormat.parse("01.07.2044 13:34");
		alarm2.period.type = EventPeriod.Type.DAILY;
		alarm2.period.interval = 1;
		
		service.createAlarm(alarm2);
		
		assertEquals(true, service.existAlarm(alarm2));
		assertEquals(true, isSetNotification());
		
		alarm2.period.endDate = currentDate;
		
		service.updateAlarm(alarm2);
		
		assertEquals(false, service.existAlarm(alarm2));
		assertEquals(false, isSetNotification());
		if (isSetNotification) {
			service.createNotification();
		}
	}
	
	public void tearDown() {
		
		mManager.unbindService();
		
	}
}
