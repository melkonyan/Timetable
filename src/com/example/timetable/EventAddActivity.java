package com.example.timetable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.ImageButton;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import com.timetable.app.R;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import com.example.timetable.EventChecker.IllegalEventDateException;

/*
 * Activity that provides an user interface to add event.
 * If Intent has extra field 'date' contains date in format 'dd.MM.yyyy HH:mm',
 * this values will be set to appropriate fields
 */
public class EventAddActivity extends ActionBarActivity {
	
	public static final SimpleDateFormat INIT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	private static final EventPeriod.Type EVENT_PERIOD_TYPE_IDS [] = 
			new EventPeriod.Type[] {
									EventPeriod.Type.NONE,
									EventPeriod.Type.DAILY,
									EventPeriod.Type.WEEKLY,
									EventPeriod.Type.MONTHLY,
									EventPeriod.Type.YEARLY			
									}; 
	
	private String eventPeriodWeekDayNames [];

	public CheckBox eventPeriodWeekDayCheckBoxes [] = new CheckBox[7]; 
	
	public RelativeLayout mContainer;
	public EditText eventNameVal;
	public EditText eventPlaceVal;
	public EditText eventDateVal;
	public EditText eventStartTimeVal;
	public EditText eventEndTimeVal;
	public EditText eventNoteVal;
	public Spinner eventPeriodTypeSpinner;
	public TextView eventPeriodIntervalTextLeft;
	public TextView eventPeriodIntervalTextRight;
	public EditText eventPeriodIntervalVal;
	public Spinner eventPeriodEndDateSpinner;
	public EditText eventPeriodEndDateVal; 
	
	//table containing checkboxes of weekdays
	public LinearLayout eventPeriodWeekDaysTable;
	
	public ImageButton eventDatePickerButton;
	public ImageButton eventStartTimePickerButton;
	public ImageButton eventEndTimePickerButton;
	
	
	//Date when event is being created
	public Date initDate;
	
	EventChecker checker;
	
	public static final SimpleDateFormat dateFormat = EventChecker.dateFormat;
	
	public static final SimpleDateFormat timeFormat = EventChecker.timeFormat;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_event_add);
		getSupportActionBar().setTitle(getResources().getString(R.string.actionbar_add_event));
		
		checker = new EventChecker(this);
		
		eventPeriodWeekDayNames = getResources().getStringArray(R.array.event_period_week_day_names_array);
		
		mContainer = (RelativeLayout) findViewById(R.id.event_add_container);
		eventNameVal = (EditText) findViewById(R.id.event_add_name_val);
		eventPlaceVal = (EditText) findViewById(R.id.event_add_place_val);
		eventDateVal = (EditText) findViewById(R.id.event_add_date_val);
		eventStartTimeVal = (EditText) findViewById(R.id.event_add_start_time_val);
		eventEndTimeVal = (EditText) findViewById(R.id.event_add_end_time_val);
		eventNoteVal = (EditText) findViewById(R.id.event_add_note_val);
		
		eventPeriodTypeSpinner = (Spinner) findViewById(R.id.event_period_type_spinner);
		ArrayAdapter<CharSequence> eventPeriodTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
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
		ArrayAdapter<CharSequence> eventPeriodEndDateSpinnerAdapter= ArrayAdapter.createFromResource(this,
		        R.array.event_period_end_date_array, android.R.layout.simple_spinner_item);
		
		eventPeriodEndDateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		eventPeriodEndDateSpinner.setAdapter(eventPeriodEndDateSpinnerAdapter);
		PeriodEndDateListener EventPeriodEndDateSpinnerListener = new PeriodEndDateListener();
		eventPeriodEndDateSpinner.setOnItemSelectedListener(EventPeriodEndDateSpinnerListener);
		eventPeriodEndDateVal = (EditText) findViewById(R.id.event_period_end_date_val);
		
		eventPeriodWeekDaysTable = (LinearLayout) findViewById(R.id.event_period_weekdays_table);
		for (int i = 0; i < 7; i++) {
			TextView weekdayName = new TextView(this);
			weekdayName.setText(eventPeriodWeekDayNames[i]);
			eventPeriodWeekDayCheckBoxes[i] = new CheckBox(this);
			
			LinearLayout weekdayLayout = new LinearLayout(this);
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
		
		eventDatePickerButton = (ImageButton) findViewById(R.id.event_add_date_picker);
		eventDatePickerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.log("Creating DatePickerDialog");
				DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.YEAR, year);
						cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						cal.set(Calendar.MONTH, monthOfYear);
						eventDateVal.setText(dateFormat.format(cal.getTime()));
					}
					
				};
				new DatePickerDialog(EventAddActivity.this, mOnDateSetListener, 
						getDate().get(Calendar.YEAR), getDate().get(Calendar.MONTH), getDate().get(Calendar.DAY_OF_MONTH)).show();
				
			}
		});
		
		eventStartTimePickerButton = (ImageButton) findViewById(R.id.event_add_start_time_picker);
		eventStartTimePickerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.log("Creating StartTimePickerDialog");
				TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
					
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
						cal.set(Calendar.MINUTE, minute);
						eventStartTimeVal.setText(timeFormat.format(cal.getTime()));
						}
				};
				
				new TimePickerDialog(EventAddActivity.this, mOnTimeSetListener, 
										getStartTime().get(Calendar.HOUR_OF_DAY), getStartTime().get(Calendar.MINUTE), true).show();
			}
		});
		
		eventEndTimePickerButton = (ImageButton) findViewById(R.id.event_add_end_time_picker);
		eventEndTimePickerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.log("Creating EndTimePickerDialog");
				TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
					
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
						cal.set(Calendar.MINUTE, minute);
						eventEndTimeVal.setText(timeFormat.format(cal.getTime()));
						}
				};
				
				new TimePickerDialog(EventAddActivity.this, mOnTimeSetListener, 
										getEndTime().get(Calendar.HOUR_OF_DAY), getEndTime().get(Calendar.MINUTE), true).show();
			}
		});
		
		setMaxLength(eventPeriodIntervalVal, Event.MAX_PERIOD_INTERVAL_LENGTH);
		setMaxLength(eventNameVal, Event.MAX_NAME_LENGTH);
		setMaxLength(eventPlaceVal, Event.MAX_PLACE_LENGTH);
		setMaxLength(eventNoteVal, Event.MAX_NOTE_LENGTH);
		
		//set initial date if it is given
		setInitDate();
		TimetableLogger.log("EventAddActivity created.");
		
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		TimetableLogger.log("onResume()");
	}
	
	/*
	 * try to get event date from intent extras and fill appropriate fields
	 */
	private void setInitDate() {
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		try {
			initDate = INIT_DATE_FORMAT.parse(extras.getString("date"));
			eventDateVal.setText(dateFormat.format(initDate));
			Calendar cal = Calendar.getInstance();
			cal.setTime(initDate);
			cal.add(Calendar.HOUR, 1);
			cal.set(Calendar.MINUTE, 0);
			eventStartTimeVal.setText(timeFormat.format(cal.getTime()));
			cal.add(Calendar.HOUR, 1);
			eventEndTimeVal.setText(timeFormat.format(cal.getTime()));
			boolean [] initWeekOccurences = new boolean[7];
			initWeekOccurences[cal.get(Calendar.DAY_OF_WEEK) - 1] = true;
			setEventPeriodWeekOccurrences(initWeekOccurences);
		} catch (Exception e) {
			TimetableLogger.log("Invalid date was given to EventAddActivity.");
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_event_add, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_save_event:
	        	TimetableLogger.log("Try to save event.");
	        	try {
	        		saveEvent();
	        		finish();
	        	} catch (IllegalEventDataException e) {
	            	Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	            }
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void setMaxLength(EditText view, int length) {
		view.setFilters(new InputFilter[] {new InputFilter.LengthFilter(length)});
	}
	
	public Calendar getDate() {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(dateFormat.parse(eventDateVal.getText().toString()));
		} catch (ParseException e) {
			cal.setTime(initDate);
		}
		return cal;
	}
	
	public Calendar getStartTime() {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(timeFormat.parse(eventStartTimeVal.getText().toString()));
		} catch (ParseException e) {
			cal.setTime(initDate);
			cal.add(Calendar.HOUR_OF_DAY, 1);
			cal.set(Calendar.MINUTE, 0);
		}
		return cal;
	}
	
	public Calendar getEndTime() {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(timeFormat.parse(eventEndTimeVal.getText().toString()));
		} catch (ParseException e) {
			cal = getStartTime();
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		return cal;
	}
	
	public  void showEventPeriodIntervalText() {
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
		eventPeriodIntervalTextRight.setText(getResources().getString(string_id));
	}
	
	public EventPeriod getEventPeriod() throws IllegalEventDataException {
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

	public void setEventPeriod(EventPeriod period) {
		setEventPeriodType(period.type);
		if (period.isRepeatable()) {
			eventPeriodIntervalVal.setText(Integer.toString(period.interval));
			if (period.isEveryWeek()) {
				setEventPeriodWeekOccurrences(period.weekOccurrences);
			}
			setEventPeriodEndDate(period.endDate);
		}
	}

	public void showEventPeriod() {
		EventPeriod.Type type = getEventPeriodType();
		int isVisible = (type == EventPeriod.Type.NONE ? View.GONE : View.VISIBLE); 
		showEventPeriodWeekOccurrences();
		//TimetableLogger.log("EventAddActivity. Show event type: " + type.toString() + Boolean.toString(isVisible == View.VISIBLE));
		eventPeriodIntervalVal.setVisibility(isVisible);
		eventPeriodIntervalTextLeft.setVisibility(isVisible);
		eventPeriodIntervalTextRight.setVisibility(isVisible);
		eventPeriodEndDateSpinner.setVisibility(isVisible);
		showPeriodEndDate();
		showEventPeriodIntervalText();
	}
	
	public void showEventPeriodWeekOccurrences() {
		int isVisible = (getEventPeriodType() == EventPeriod.Type.WEEKLY ?  View.VISIBLE : View.GONE);
		eventPeriodWeekDaysTable.setVisibility(isVisible);
	}
	
	public void setEventPeriodWeekOccurrences(boolean [] weekOccurrences) {
		for (int i = 0; i < 7; i++) {
			eventPeriodWeekDayCheckBoxes[i].setChecked(weekOccurrences[i]);
		}
	}
	
	public boolean [] getEventPeriodWeekOccurrences() {
		boolean [] weekOccurrences = new boolean[7];
		for (int i = 0; i < 7; i++) {
			weekOccurrences[i] = eventPeriodWeekDayCheckBoxes[i].isChecked();
		}
		return weekOccurrences;
	}
	
	/*
	 * returns true if PeriodEndDateSpinner has item "until a date" selected 
	 */
	public boolean isSetEventPeriodEndDate() {
		return (eventPeriodEndDateSpinner.getSelectedItemPosition() == 1);
	}
	
	
	 /*
	  *  shows period end date if it is set
	  */
	 public void showPeriodEndDate() {
		if (isSetEventPeriodEndDate()  && getEventPeriodType() != EventPeriod.Type.NONE) {
			eventPeriodEndDateVal.setVisibility(View.VISIBLE);
		}
		else {
			eventPeriodEndDateVal.setVisibility(View.GONE);
		}
	}
	
	public void setEventPeriodEndDate(Date endDate) {
		if (endDate != null) {
			//set spinner to show "until a date"
			eventPeriodEndDateSpinner.setSelection(1);
			eventPeriodEndDateVal.setText(dateFormat.format(endDate));
		}
	}

	public Date getEventPeriodEndDate() throws EventChecker.IllegalEventPeriodEndDateException {
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
	 
	public void setEventPeriodType(EventPeriod.Type type) {
		eventPeriodTypeSpinner.setSelection(type.ordinal());
	}
	
	public EventPeriod.Type getEventPeriodType() {
		return EVENT_PERIOD_TYPE_IDS[eventPeriodTypeSpinner.getSelectedItemPosition()];
	}
	
	public void setEventEndTime(Date endTime) {
		if (endTime != null) {
			eventEndTimeVal.setText(timeFormat.format(endTime));
		}
		else {
			eventEndTimeVal.setText("");
		}
	}
	
	public void setEvent(Event event) {
		eventNameVal.setText(event.name);
		eventPlaceVal.setText(event.place);
		
		//set date field to be equal current date, not event start date
		eventDateVal.setText(dateFormat.format(event.date));
		eventStartTimeVal.setText(timeFormat.format(event.startTime));
		setEventEndTime(event.endTime);
		eventNoteVal.setText(event.note);
		setEventPeriod(event.period);
	}
	
	public Event getEvent() throws IllegalEventDataException {
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
	  *  save event to database
	  */
	public void saveEvent() throws IllegalEventDataException {
		Event event = getEvent();
		TimetableDatabase db = new TimetableDatabase(this);
		db.insertEvent(event);
		db.close();
	}
	
	public class PeriodTypeListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
				long id) {
			showEventPeriod();
			//bug in HoloEverywhere. Need to do this, to resize layout.
			mContainer.post(new Runnable() {
                 @Override
                 public void run() {
                     mContainer.requestLayout();
                 }
             });  

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}
	
	public class PeriodEndDateListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			showPeriodEndDate();
			//bug in HoloEverywhere. Need to do this, to resize layout.
			mContainer.post(new Runnable() {
                 @Override
                 public void run() {
                     mContainer.requestLayout();
                 }
             });  

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
		
	}


}
