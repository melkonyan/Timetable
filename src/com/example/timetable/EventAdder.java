package com.example.timetable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.example.timetable.EventChecker.IllegalEventDateException;

import android.content.Context;
import android.content.res.Resources;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class EventAdder extends RelativeLayout {

	private static final EventPeriod.Type EVENT_PERIOD_TYPE_IDS [] = 
			new EventPeriod.Type[] {
									EventPeriod.Type.NONE,
									EventPeriod.Type.DAILY,
									EventPeriod.Type.WEEKLY,
									EventPeriod.Type.MONTHLY,
									EventPeriod.Type.YEARLY			
									}; 
	
	private final String eventPeriodWeekDayNames [];

	protected CheckBox eventPeriodWeekDayCheckBoxes [] = new CheckBox[7]; 
	
	protected EditText eventNameVal;
	
	protected EditText eventPlaceVal;
	
	protected EditText eventDateVal;
	
	protected EditText eventStartTimeVal;
	
	protected EditText eventEndTimeVal;
	
	protected EditText eventNoteVal;
	
	protected Spinner eventPeriodTypeSpinner;
	
	protected TextView eventPeriodIntervalTextLeft;
	
	protected TextView eventPeriodIntervalTextRight;
	
	protected EditText eventPeriodIntervalVal;

	protected Spinner eventPeriodEndDateSpinner;
	
	protected EditText eventPeriodEndDateVal;
	
	protected LinearLayout eventPeriodWeekDaysTable; //table containing checkboxes of weekdays
	
	private Context context;
	
	EventChecker checker = new EventChecker();
	
	protected static final SimpleDateFormat dateFormat = EventChecker.dateFormat;
	
	protected static final SimpleDateFormat timeFormat = EventChecker.timeFormat;	
	
	protected Resources resources;
	
	public EventAdder(Context context) {
		super(context);
		this.context = context;
		this.resources = context.getResources();
		eventPeriodWeekDayNames = resources.getStringArray(R.array.event_period_week_day_names_array);
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );	
		layoutInflater.inflate(R.layout.activity_event_add, this, true);
		eventNameVal = (EditText) findViewById(R.id.event_add_name_val);
		eventPlaceVal = (EditText) findViewById(R.id.event_add_place_val);
		eventDateVal = (EditText) findViewById(R.id.event_add_date_val);
		eventStartTimeVal = (EditText) findViewById(R.id.event_add_start_time_val);
		eventEndTimeVal = (EditText) findViewById(R.id.event_add_end_time_val);
		eventNoteVal = (EditText) findViewById(R.id.event_add_note_val);
		
		eventPeriodTypeSpinner = (Spinner) findViewById(R.id.event_period_type_spinner);
		ArrayAdapter<CharSequence> eventPeriodTypeSpinnerAdapter = ArrayAdapter.createFromResource(context,
		        R.array.event_period_type_array, android.R.layout.simple_spinner_item);
		
		eventPeriodTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		eventPeriodTypeSpinner.setAdapter(eventPeriodTypeSpinnerAdapter);
		PeriodTypeListener eventPeriodTypeSpinnerListener = new PeriodTypeListener();
		eventPeriodTypeSpinner.setOnItemSelectedListener(eventPeriodTypeSpinnerListener);
		eventPeriodIntervalVal = (EditText) findViewById(R.id.event_period_inteval_val);
		eventPeriodIntervalTextLeft = (TextView) findViewById(R.id.event_period_interval_text_left);
		eventPeriodIntervalTextRight = (TextView) findViewById(R.id.event_period_interval_text_right);
		
		eventPeriodEndDateSpinner = (Spinner) findViewById(R.id.event_period_end_date_spinner);
		ArrayAdapter<CharSequence> eventPeriodEndDateSpinnerAdapter= ArrayAdapter.createFromResource(context,
		        R.array.event_period_end_date_array, android.R.layout.simple_spinner_item);
		
		eventPeriodEndDateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		eventPeriodEndDateSpinner.setAdapter(eventPeriodEndDateSpinnerAdapter);
		PeriodEndDateListener EventPeriodEndDateSpinnerListener = new PeriodEndDateListener();
		eventPeriodEndDateSpinner.setOnItemSelectedListener(EventPeriodEndDateSpinnerListener);
		eventPeriodEndDateVal = (EditText) findViewById(R.id.event_period_end_date_val);
		
		eventPeriodWeekDaysTable = (LinearLayout) findViewById(R.id.event_period_weekdays_table);
		for (int i = 0; i < 7; i++) {
			TextView weekdayName = new TextView(context);
			weekdayName.setText(eventPeriodWeekDayNames[i]);
			eventPeriodWeekDayCheckBoxes[i] = new CheckBox(context);
			
			LinearLayout weekdayLayout = new LinearLayout(context);
			LinearLayout.LayoutParams weekdayLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			weekdayLayout.setLayoutParams(weekdayLayoutParams);
			
			LinearLayout.LayoutParams weekdayParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			weekdayParams.gravity = Gravity.CENTER_HORIZONTAL;
			weekdayName.setLayoutParams(weekdayParams);
			eventPeriodWeekDayCheckBoxes[i].setLayoutParams(weekdayParams);
			
			weekdayLayout.setOrientation(LinearLayout.VERTICAL);
			weekdayLayout.addView(weekdayName);
			weekdayLayout.addView(eventPeriodWeekDayCheckBoxes[i]);
			eventPeriodWeekDaysTable.addView(weekdayLayout);
		}
		
		showEventPeriod();
		
		setMaxLength(eventPeriodIntervalVal, Event.MAX_PERIOD_INTERVAL_LENGTH);
		setMaxLength(eventNameVal, Event.MAX_NAME_LENGTH);
		setMaxLength(eventPlaceVal, Event.MAX_PLACE_LENGTH);
		setMaxLength(eventNoteVal, Event.MAX_NOTE_LENGTH);
	}
	
	
	public EventAdder(Context context, Date date) {
		this(context);
		eventDateVal.setText(dateFormat.format(date));
		eventStartTimeVal.setText(timeFormat.format(date));
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR, 1);
		eventEndTimeVal.setText(timeFormat.format(cal.getTime()));
		
	}
	
	private void setMaxLength(EditText view, int length) {
		view.setFilters(new InputFilter[] {new InputFilter.LengthFilter(length)});
	}
	
	protected  void showEventPeriodIntervalText() {
		int string_id;
		switch (getEventPeriodType()) {
			case DAILY:
				string_id = R.string.event_add_interval_text_daily;
				break;
			case WEEKLY:
				string_id = R.string.event_add_interval_text_weekly;
				break;
			case MONTHLY:
				string_id = R.string.event_add_interval_text_monthly;
				break;
			case YEARLY:
				string_id = R.string.event_add_interval_text_yearly;
				break;
			default:
				return;
		}
		eventPeriodIntervalTextRight.setText(resources.getString(string_id));
	}
	
	protected EventPeriod getEventPeriod() throws IllegalEventDataException {
		EventPeriod period = new EventPeriod();
		period.type = getEventPeriodType();
		if (period.isRepeatable()) {
			period.interval = checker.getPeriodIntervalFromString(eventPeriodIntervalVal.getText().toString());
			if (period.isEveryWeek()) {
				period.weekOccurrences = getEventPeriodWeekOccurrences();
			}
			period.endDate = getEventPeriodEndDate();
		}
		return period;
	}

	protected void setEventPeriod(EventPeriod period) {
		setEventPeriodType(period.type);
		if (period.isRepeatable()) {
			eventPeriodIntervalVal.setText(Integer.toString(period.interval));
			if (period.isEveryWeek()) {
				setEventPeriodWeekOccurrences(period.weekOccurrences);
			}
			setEventPeriodEndDate(period.endDate);
		}
	}

	protected void showEventPeriod() {
		EventPeriod.Type type = getEventPeriodType();
		int isVisible = (type == EventPeriod.Type.NONE ? View.GONE : View.VISIBLE); 
		eventPeriodIntervalVal.setVisibility(isVisible);
		eventPeriodIntervalTextLeft.setVisibility(isVisible);
		eventPeriodIntervalTextRight.setVisibility(isVisible);
		eventPeriodEndDateSpinner.setVisibility(isVisible);
		showPeriodEndDate();
		showEventPeriodIntervalText();
		showEventPeriodWeekOccurrences();
	}
	
	protected void showEventPeriodWeekOccurrences() {
		int isVisible = (getEventPeriodType() == EventPeriod.Type.WEEKLY ?  View.VISIBLE : View.GONE);
		eventPeriodWeekDaysTable.setVisibility(isVisible);
	}
	
	protected void setEventPeriodWeekOccurrences(boolean [] weekOccurrences) {
		for (int i = 0; i < 7; i++) {
			eventPeriodWeekDayCheckBoxes[i].setChecked(weekOccurrences[i]);
		}
	}
	
	protected boolean [] getEventPeriodWeekOccurrences() {
		boolean [] weekOccurrences = new boolean[7];
		for (int i = 0; i < 7; i++) {
			weekOccurrences[i] = eventPeriodWeekDayCheckBoxes[i].isChecked();
		}
		return weekOccurrences;
	}
	
	/*
	 * returns true if PeriodEndDateSpinner has item "until a date" selected 
	 */
	protected boolean isSetEventPeriodEndDate() {
		return (eventPeriodEndDateSpinner.getSelectedItemPosition() == 1);
	}
	
	/*
	 * shows period end date if one is set
	 */
	protected void showPeriodEndDate() {
		if (isSetEventPeriodEndDate()  && getEventPeriodType() != EventPeriod.Type.NONE) {
			eventPeriodEndDateVal.setVisibility(View.VISIBLE);
		}
		else {
			eventPeriodEndDateVal.setVisibility(View.GONE);
		}
	}
	
	protected void setEventPeriodEndDate(Date endDate) {
		if (endDate != null) {
			//set spinner to show "until a date"
			eventPeriodEndDateSpinner.setSelection(1);
			eventPeriodEndDateVal.setText(dateFormat.format(endDate));
		}
	}

	protected Date getEventPeriodEndDate() throws EventChecker.IllegalEventPeriodEndDateException {
		if (isSetEventPeriodEndDate()) {
			return checker.getPeriodEndDateFromString(eventPeriodEndDateVal.getText().toString());
		}
		else {
			return null;
		}
	}
	
	/*
	 * set spinner to show event period type
	 */
	protected void setEventPeriodType(EventPeriod.Type type) {
		eventPeriodTypeSpinner.setSelection(type.ordinal());
	}
	
	protected EventPeriod.Type getEventPeriodType() {
		return EVENT_PERIOD_TYPE_IDS[eventPeriodTypeSpinner.getSelectedItemPosition()];
	}
	
	protected void setEventEndTime(Date endTime) {
		if (endTime != null) {
			eventEndTimeVal.setText(timeFormat.format(endTime));
		}
		else {
			eventEndTimeVal.setText("");
		}
	}
	
	protected void setEvent(Event event) {
		eventNameVal.setText(event.name);
		eventPlaceVal.setText(event.place);
		
		//set date field to be equal current date, not event start date
		eventDateVal.setText(dateFormat.format(event.date));
		eventStartTimeVal.setText(timeFormat.format(event.startTime));
		setEventEndTime(event.endTime);
		eventNoteVal.setText(event.note);
		setEventPeriod(event.period);
	}
	
	protected Event getEvent() throws IllegalEventDataException {
		Event event = new Event();
		//TODO: set focus on invalid text fields
		try {
			event.name = checker.getNameFromString(eventNameVal.getText().toString());
			event.place = checker.getPlaceFromString(eventPlaceVal.getText().toString());
			event.date = checker.getDateFromString(eventDateVal.getText().toString());
			event.startTime = checker.getStartTimeFromString(eventStartTimeVal.getText().toString());
			event.endTime = checker.getEndTimeFromString(eventEndTimeVal.getText().toString());
			event.note = checker.getNoteFromString(eventNoteVal.getText().toString());
			event.period = getEventPeriod();
			
			TimetableLogger.log(event.toString());
			return event;
		}  catch (IllegalEventDateException e) {
			throw e;
		}
	}
	
	/*
	 * return true if activity page needs to be changed
	 */
	public boolean saveEvent() throws IllegalEventDataException {
		Event event = getEvent();
		TimetableDatabase db = new TimetableDatabase(context);
		db.insertEvent(event);
		db.close();
		return true;
	}
	
	protected class PeriodTypeListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
				long id) {
			showEventPeriod();
		}
		
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			return;
		}
		
	}
	
	protected class PeriodEndDateListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			showPeriodEndDate();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		
		}
		
	}
	
}
