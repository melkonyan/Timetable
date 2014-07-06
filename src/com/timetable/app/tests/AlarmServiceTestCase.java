package com.timetable.app.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.timetable.app.EventPeriod;
import com.timetable.app.activities.EventDayViewActivity;
import com.timetable.app.alarm.AlarmService;
import com.timetable.app.alarm.AlarmServiceManager;
import com.timetable.app.alarm.EventAlarm;
import com.timetable.app.functional.TimeProvider;
import com.timetable.app.functional.TimetableFunctional;

public class AlarmServiceTestCase extends AndroidTestCase {
	
	private AlarmServiceManager mManager;
	private Context context;
	private Date currentDate;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	public class FakeTimeProvider implements TimeProvider {

		@Override
		public Date getCurrentTime() {
			return currentDate;
		}
		
	}
	
	private boolean isSetNotification(AlarmService context) {
		return PendingIntent.getBroadcast(context, AlarmService.ALARM_NOTIFICATION_CODE, 
										new Intent(context, EventDayViewActivity.class), PendingIntent.FLAG_NO_CREATE) != null;
	}
	
	public void setUp() throws ParseException {
		//context = new RenamingDelegatingContext(getContext(), "test_");
		context = getContext();
		mManager = new AlarmServiceManager(context);
		mManager.bindService();
		currentDate = EventAlarm.timeFormat.parse("05.07.2014 20:36");
	}

	public void testAlarmService() throws ParseException, InterruptedException {
		AlarmService service = null;
		while (service == null) {
			Thread.sleep(1000);
			service = mManager.getService();
		}
		
		assertNotNull(service);
		assertEquals(false, isSetNotification(service));
		
		TimetableFunctional.setTimeProvider(new FakeTimeProvider());
		
		EventAlarm alarm1 = new EventAlarm();
		alarm1.id = 1;
		alarm1.time = EventAlarm.timeFormat.parse("20.09.2014 20:42");
		
		assertEquals(false, service.existAlarm(alarm1));
		
		service.createAlarm(alarm1);
		
		assertEquals(true, service.existAlarm(alarm1));
		assertEquals(true, isSetNotification(service));
		
		service.updateAlarm(alarm1);
		
		assertEquals(true, service.existAlarm(alarm1));
		assertEquals(true, isSetNotification(service));
		
		service.deleteAlarm(alarm1);
		
		assertEquals(false, service.existAlarm(alarm1));
		assertEquals(false, isSetNotification(service));
		
		EventAlarm alarm2 = new EventAlarm();
		alarm2.id = 2;
		alarm2.time = EventAlarm.timeFormat.parse("01.07.2014 13:34");
		alarm2.period.type = EventPeriod.Type.DAILY;
		alarm2.period.interval = 1;
		
		service.createAlarm(alarm2);
		
		assertEquals(true, service.existAlarm(alarm2));
		assertEquals(true, isSetNotification(service));
		
		alarm2.period.endDate = currentDate;
		
		service.updateAlarm(alarm2);
		
		assertEquals(false, service.existAlarm(alarm2));
		assertEquals(false, isSetNotification(service));
		
	}
	
	public void tearDown() {
		mManager.unbindService();
	}
}
