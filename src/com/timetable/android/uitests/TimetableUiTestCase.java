package com.timetable.android.uitests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Spinner;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.robotium.solo.Solo;
import com.timetable.android.Event;
import com.timetable.android.EventPeriod;
import com.timetable.android.EventView;
import com.timetable.android.R;
import com.timetable.android.activities.EventAddActivity;
import com.timetable.android.activities.EventDayViewActivity;
import com.timetable.android.activities.EventEditActivity;

public class TimetableUiTestCase extends ActivityInstrumentationTestCase2<EventDayViewActivity> {

	private static final int DEFAULT_TIMEOUT = 1000;
	
	private Solo solo;
	
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	
	private Resources mResources;
		

	
	public TimetableUiTestCase() {
		super(EventDayViewActivity.class);
	}
	
	private void clickOnView(int id) {
		final View view = solo.getView(id);
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				view.performClick();
			}
		});
		
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
		solo.clickOnView(solo.getView(View.class, itemIndex)); 
	}
	
	public void setUp() throws ParseException {
		solo = new Solo(getInstrumentation());
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
		
		Event event = new Event.Builder()
						.setName("event1")
						.setDate("07.07.2024")
						.setStartTime("20:07")
						.setEndTime("21:07")
						.setPeriodType(EventPeriod.DAILY)
						.setAlarmTime("10.08.2024 14:00")
						.build();
		
		View eventAddButton = solo.getView(R.id.action_add_event);
		solo.assertCurrentActivity("Wrong Activity", EventDayViewActivity.class);
		
		solo.clickOnView(eventAddButton);
		solo.sleep(1000);
		
		solo.assertCurrentActivity("EventAddActivity not started.", EventAddActivity.class);
		
		View eventSaveButton = solo.getView(R.id.action_save_event);
		View alarmAddButton = solo.getView(R.id.event_add_alarm);
		solo.clickOnView(eventSaveButton);
	
		assertEquals(true, solo.waitForText(mResources.getText(R.string.event_checker_empty_name).toString(), 0, DEFAULT_TIMEOUT));
		
		//Create event with alarm, daily period, no end time,  no period end time
		solo.sleep(1000);//wait for toast to gone.
		solo.enterText((EditText) solo.getView(R.id.event_add_name_val), event.getName());
		setEditText(R.id.event_add_date_val, DATE_FORMAT.format(event.getDate()));
		setEditText(R.id.event_add_start_time_val, TIME_FORMAT.format(event.getStartTime()));
		setEditText(R.id.event_add_end_time_val, TIME_FORMAT.format(event.getEndTime()));
		
		//solo.clickOnView(alarmAddButton);
		//setEditText(R.id.event_alarm_time_val, EventAlarm.timeFormat.format(event.getAlarm().time));
		//TODO: remove ordinal
		selectSpinnerItem(0, event.getPeriod().getType().ordinal() + 1);
		solo.clickOnView(eventSaveButton);
		
		assertTrue(solo.waitForActivity(EventDayViewActivity.class, DEFAULT_TIMEOUT));
		assertTrue(solo.searchText(event.getName()));
		assertTrue(solo.searchText(EventView.START_TIME_FORMAT.format(event.getStartTime())));
		assertEquals(View.VISIBLE, solo.getView(R.id.layout_event_image_repeat).getVisibility());
		//assertEquals(View.VISIBLE, solo.getView(R.id.layout_event_image_alarm).getVisibility());
		
		clickOnView(R.id.layout_event_container);
		
		assertEquals(true, solo.waitForActivity(EventEditActivity.class, DEFAULT_TIMEOUT));
		
		//update event: delete event period and alarm
		View alarmDeleteButton = solo.getView(R.id.event_delete_alarm);
		//solo.clickOnView(alarmDeleteButton);
		selectSpinnerItem(0, 1);
		View eventUpdateButton = solo.getView(R.id.action_save_event);
		solo.clickOnView(eventUpdateButton);
		solo.clickOnView(solo.getView(View.class, 1));
		solo.clickOnButton(mResources.getText(R.string.dialog_button_save).toString());
		
		assertTrue(solo.waitForActivity(EventDayViewActivity.class, DEFAULT_TIMEOUT));
		assertEquals(View.INVISIBLE, getActivity().findViewById(R.id.layout_event_image_repeat).getVisibility());
		assertEquals(View.INVISIBLE, getActivity().findViewById(R.id.layout_event_image_alarm).getVisibility());
		
		clickOnView(R.id.layout_event_container);
		
		assertTrue(solo.waitForActivity(EventEditActivity.class, DEFAULT_TIMEOUT));
		
		//update event: add alarm and period
		//solo.clickOnView(alarmAddButton);
		//setEditText(R.id.event_alarm_time_val, EventAlarm.timeFormat.format(event.getAlarm().time));
		//TODO: remove ordinal
		selectSpinnerItem(0, event.getPeriod().getType().ordinal() + 1);
		solo.clickOnView(eventUpdateButton);
		
		assertTrue(solo.waitForActivity(EventDayViewActivity.class, DEFAULT_TIMEOUT));
		assertEquals(View.VISIBLE, solo.getView(R.id.layout_event_image_repeat).getVisibility());
		//assertEquals(View.VISIBLE, solo.getView(R.id.layout_event_image_alarm).getVisibility());
		
		clickOnView(R.id.layout_event_container);
		
		assertTrue(solo.waitForActivity(EventEditActivity.class, DEFAULT_TIMEOUT));
		
		//delete event
		View eventDeleteButton = solo.getView(R.id.action_delete_event);
		solo.clickOnView(eventDeleteButton);
		solo.clickOnView(solo.getView(View.class, 1));
		solo.clickOnButton(mResources.getText(R.string.dialog_button_delete).toString());
		
		assertTrue(solo.waitForActivity(EventDayViewActivity.class, DEFAULT_TIMEOUT));
		assertFalse(solo.searchText(event.getName()));
		
	}
	
	
	public void tearDown() {
	}
}
