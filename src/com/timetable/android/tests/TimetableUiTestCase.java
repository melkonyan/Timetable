package com.timetable.android.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Spinner;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.View;

import com.robotium.solo.Solo;
import com.timetable.android.Event;
import com.timetable.android.EventPeriod;
import com.timetable.android.EventView;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.activities.EventAddActivity;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.activities.EventEditActivity;
import com.timetable.android.alarm.EventAlarm;
import com.timetable.android.functional.FakeTimeProvider;
import com.timetable.android.functional.TimetableFunctional;

public class TimetableUiTestCase extends ActivityInstrumentationTestCase2<EventDayViewActivity> {

	private static final int DEFAULT_TIMEOUT = 1000;
	
	private Solo solo;
	
	private Date currentDate;
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	
	private Resources mResources;
		
	
	public TimetableUiTestCase() {
		super(EventDayViewActivity.class);
	}
	
	private void setEditText(int id, String text) {
		EditText editText = (EditText) solo.getView(id); 
		solo.clearEditText(editText);
		solo.enterText(editText, text);
	}
	
	
	private void selectSpinnerItem(int spinnerIndex, int itemIndex) {
		View spinnerView = solo.getView(Spinner.class, spinnerIndex);
		solo.clickOnView(spinnerView);
		solo.scrollToTop(); // I put this in here so that it always keeps the list at start
		solo.clickOnView(solo.getView(View.class, 2)); 
	}
	
	public void setUp() throws ParseException {
		currentDate = DATE_FORMAT.parse("7.07.2014");
		TimetableFunctional.setTimeProvider(new FakeTimeProvider(currentDate));
		
		solo = new Solo(getInstrumentation(), getActivity());
		mResources = getActivity().getResources();
		
	}
	
	/*public void testPageViewer() throws ParseException, InterruptedException {
		solo.assertCurrentActivity("Wrong Activity", EventDayViewActivity.class);
		ActionBarActivity mActivity = (ActionBarActivity) solo.getCurrentActivity();
		
		assertEquals(mActivity.getSupportActionBar().getTitle().toString(), mResources.getString(R.string.actionbar_date_today));
		
		//shit-solo can not scroll screen
		//solo.scrollViewToSide(solo.getView(R.id.event_pager) ,Solo.RIGHT);
		//assertEquals(EventPager.ACTION_BAR_DATE_FORMAT.format(DATE_FORMAT.parse("08.07.2014")), 
		//		mActivity.getSupportActionBar().getTitle().toString());
		
		solo.sendKey(Solo.MENU);
		solo.clickOnText(mResources.getString(R.string.menu_go_to_date));
		//solo.clickOnMenuItem(mResources.getString(R.string.menu_go_to_date));
		solo.setDatePicker(0, 2014, 6, 8);
		
		//TODO: create string resource
		solo.clickOnText("Set");
		solo.sleep(1000);//Should wait, because activity is reloaded
		assertEquals(EventPager.ACTION_BAR_DATE_FORMAT.format(DATE_FORMAT.parse("08.07.2014")), 
						mActivity.getSupportActionBar().getTitle().toString());
		
	}
	*/
	
	public void testEventAdd() throws ParseException {
		solo.assertCurrentActivity("Wrong Activity", EventDayViewActivity.class);
		
		View eventAddButton = solo.getView(R.id.action_add_event);
		solo.clickOnView(eventAddButton);
		solo.sleep(1000);
		solo.assertCurrentActivity("EventAddActivity not started.", EventAddActivity.class);
		
		View eventSaveButton = solo.getView(R.id.action_save_event);
		solo.clickOnView(eventSaveButton);
	
		assertEquals(true, solo.waitForText(mResources.getText(R.string.event_checker_empty_name).toString(), 0, DEFAULT_TIMEOUT));
		
		Event event = new Event();
		
		event.name = "event1";
		event.date = DATE_FORMAT.parse("07.07.2014");
		event.startTime = TIME_FORMAT.parse("20:07");
		
		event.period = new EventPeriod();
		event.period.type = EventPeriod.Type.DAILY;
		
		event.alarm = new EventAlarm();
		event.alarm.time = EventAlarm.timeFormat.parse("10.08.2014 14:00");
		
		solo.sleep(1000);//wait for toast to gone.
		solo.enterText((EditText) solo.getView(R.id.event_add_name_val), event.name);
		setEditText(R.id.event_add_date_val, DATE_FORMAT.format(event.date));
		setEditText(R.id.event_add_start_time_val, TIME_FORMAT.format(event.startTime));
		setEditText(R.id.event_add_end_time_val, "");
		View addAlarmButton = solo.getView(R.id.event_add_alarm);
		solo.clickOnView(addAlarmButton);
		setEditText(R.id.event_alarm_time_val, EventAlarm.timeFormat.format(event.alarm.time));
		
		selectSpinnerItem(0, event.period.type.ordinal());
		
		
		
		solo.clickOnView(eventSaveButton);
		
		assertEquals(true, solo.waitForActivity(EventDayViewActivity.class, DEFAULT_TIMEOUT));
		
		assertTrue(solo.searchText(event.name));
		assertTrue(solo.searchText(EventView.START_TIME_FORMAT.format(event.startTime)));
		assertEquals(View.VISIBLE, solo.getView(R.id.layout_event_image_repeat).getVisibility());
		assertEquals(View.VISIBLE, solo.getView(R.id.layout_event_image_alarm).getVisibility());
		
		solo.clickOnView(solo.getView(R.id.event_layout));
		solo.clickOnScreen(50,50);
		assertEquals(true, solo.waitForActivity(EventEditActivity.class, DEFAULT_TIMEOUT));
		
		View eventDeleteButton = solo.getView(R.id.action_delete_event);
		solo.clickOnView(eventDeleteButton);
		solo.clickOnView(solo.getView(View.class, 1));
		
		assertTrue(solo.waitForActivity(EventDayViewActivity.class, DEFAULT_TIMEOUT));
		assertNull(solo.searchText(event.name));
		
	}
	
	
	public void tearDown() {
		TimetableDatabase.getInstance(getActivity()).clear();
	}
}
