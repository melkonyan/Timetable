package com.timetable.android.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.ImageButton;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.holoeverywhere.widget.datetimepicker.date.DatePickerDialog;
import org.holoeverywhere.widget.datetimepicker.time.RadialPickerLayout;
import org.holoeverywhere.widget.datetimepicker.time.TimePickerDialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.timetable.android.Event;
import com.timetable.android.EventChecker;
import com.timetable.android.EventChecker.IllegalEventDateException;
import com.timetable.android.EventPeriod;
import com.timetable.android.EventService;
import com.timetable.android.IllegalEventDataException;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;
import com.timetable.android.alarm.AlarmService;
import com.timetable.android.alarm.AlarmServiceManager;
import com.timetable.android.alarm.EventAlarm;


/*
 * Activity that provides an user interface to add event.
 * If Intent has extra field 'date' contains date in format 'dd.MM.yyyy HH:mm',
 * this values will be set to appropriate fields
 */
public class EventAddActivity extends Activity {
	
	public static final SimpleDateFormat INIT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	public static final String INTENT_EXTRA_DATE = "date";
	
	private static final EventPeriod.Type EVENT_PERIOD_TYPE_IDS [] = 
			new EventPeriod.Type[] {
									EventPeriod.Type.NONE,
									EventPeriod.Type.DAILY,
									EventPeriod.Type.WEEKLY,
									EventPeriod.Type.MONTHLY,
									EventPeriod.Type.YEARLY			
									}; 
	
	private String eventPeriodWeekDayNames [];

	private boolean isSetEventAlarm = false;
	
	public CheckBox eventPeriodWeekDayCheckBoxes [] = new CheckBox[7]; 
	
	public RelativeLayout mContainer;
	public EditText eventNameVal;
	public EditText eventPlaceVal;
	public EditText eventDateVal;
	public EditText eventStartTimeVal;
	public EditText eventEndTimeVal;
	public CheckBox eventMuteDeviceVal;
	public EditText eventNoteVal;
	public Spinner eventPeriodTypeSpinner;
	public TextView eventPeriodIntervalTextLeft;
	public TextView eventPeriodIntervalTextRight;
	public EditText eventPeriodIntervalVal;
	public Spinner eventPeriodEndDateSpinner;
	public EditText eventPeriodEndDateVal; 
	public Button eventAlarmAddButton;
	public EditText eventAlarmTime;
	public Spinner eventAlarmTypeSpinner;
	
	public TextWatcher eventAddTextWatcher;
	
	//table containing checkboxes of weekdays
	public LinearLayout eventPeriodWeekDaysTable;
	
	public ImageButton eventDatePickerButton;
	public ImageButton eventStartTimePickerButton;
	public ImageButton eventEndTimePickerButton;
	public ImageButton eventAlarmDeleteButton;
	
	//Initial event date and time
	public Calendar initDate;
	
	private EventChecker checker;
	
	public AlarmService alarmService;
	public AlarmServiceManager mManager;
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
		eventAddTextWatcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				try {
					String dateString = s.toString();
					Calendar date = Calendar.getInstance();
					date.setTime(dateFormat.parse(dateString));
					int weekDay = date.get(Calendar.DAY_OF_WEEK) - 1;
					for (int i = 0; i < 7; i++) {
						if (!eventPeriodWeekDayCheckBoxes[i].isEnabled()) {
							eventPeriodWeekDayCheckBoxes[i].setEnabled(true);
							eventPeriodWeekDayCheckBoxes[i].setChecked(false);
						}
					eventPeriodWeekDayCheckBoxes[weekDay].setEnabled(false);
					eventPeriodWeekDayCheckBoxes[weekDay].setChecked(true);
					}
				} catch (ParseException e) {
					return;
				}
			}
		};
		eventDateVal.addTextChangedListener(eventAddTextWatcher);
		
		eventStartTimeVal = (EditText) findViewById(R.id.event_add_start_time_val);
		eventEndTimeVal = (EditText) findViewById(R.id.event_add_end_time_val);
		eventMuteDeviceVal = (CheckBox) findViewById(R.id.event_add_mute_device_val);
		eventNoteVal = (EditText) findViewById(R.id.event_add_note_val);
		eventAlarmAddButton = (Button) findViewById(R.id.event_add_alarm);
		eventAlarmAddButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isSetEventAlarm = true;
				showEventAlarm();
			}
		});
	
		eventAlarmDeleteButton = (ImageButton) findViewById(R.id.event_delete_alarm);
		eventAlarmDeleteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isSetEventAlarm = false;
				showEventAlarm();
			}
		});
		eventAlarmTime = (EditText) findViewById(R.id.event_alarm_time_val);
		eventAlarmTypeSpinner = (Spinner) findViewById(R.id.event_alarm_type_spinner);
		ArrayAdapter<CharSequence> eventAlarmTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
		        R.array.event_alarm_type_array, android.R.layout.simple_spinner_item);
		
		eventAlarmTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		eventAlarmTypeSpinner.setAdapter(eventAlarmTypeSpinnerAdapter);
		
		
		eventPeriodTypeSpinner = (Spinner) findViewById(R.id.event_period_type_spinner);
		ArrayAdapter<CharSequence> eventPeriodTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
		        R.array.event_period_type_array, android.R.layout.simple_spinner_item);
		
		eventPeriodTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		eventPeriodTypeSpinner.setAdapter(eventPeriodTypeSpinnerAdapter);
		PeriodTypeListener eventPeriodTypeSpinnerListener = new PeriodTypeListener();
		eventPeriodTypeSpinner.setOnItemSelectedListener(eventPeriodTypeSpinnerListener);
		eventPeriodIntervalVal = (EditText) findViewById(R.id.event_period_interval_val);
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
		
		createEventPeriodWeekDaysTable();
		
		eventDatePickerButton = (ImageButton) findViewById(R.id.event_add_date_picker);
		eventDatePickerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.log("Creating DatePickerDialog");
				DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePickerDialog dialog, int year,
							int monthOfYear, int dayOfMonth) {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.YEAR, year);
						cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						cal.set(Calendar.MONTH, monthOfYear);
						setEventDate(cal);
						
					}
					
				};
				DatePickerDialog mDialog = DatePickerDialog.newInstance(mOnDateSetListener, 
						getEventDate().get(Calendar.YEAR), getEventDate().get(Calendar.MONTH), getEventDate().get(Calendar.DAY_OF_MONTH));
				if (getSupportFragmentManager() == null) {
					TimetableLogger.error("EventAddActivity: fragmentmanager is null");
				}
				mDialog.show(getSupportFragmentManager());
				
				
			}
		});
		
		eventStartTimePickerButton = (ImageButton) findViewById(R.id.event_add_start_time_picker);
		eventStartTimePickerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.log("Creating StartTimePickerDialog");
				TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
					
					@Override
					public void onTimeSet(RadialPickerLayout view,
							int hourOfDay, int minute) {
						
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
						cal.set(Calendar.MINUTE, minute);
						eventStartTimeVal.setText(timeFormat.format(cal.getTime()));

					}
				};
				
				TimePickerDialog.newInstance(mOnTimeSetListener, 
										getEventStartTime().get(Calendar.HOUR_OF_DAY), getEventStartTime().get(Calendar.MINUTE), true)
										.show(getSupportFragmentManager());
			}
		});
		
		eventEndTimePickerButton = (ImageButton) findViewById(R.id.event_add_end_time_picker);
		eventEndTimePickerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TimetableLogger.log("Creating EndTimePickerDialog");
				TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
					
					@Override
					public void onTimeSet(RadialPickerLayout view,
							int hourOfDay, int minute) {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
						cal.set(Calendar.MINUTE, minute);
						eventEndTimeVal.setText(timeFormat.format(cal.getTime()));
						
					}
				};
				
				TimePickerDialog.newInstance(mOnTimeSetListener, 
										getEventEndTime().get(Calendar.HOUR_OF_DAY), getEventEndTime().get(Calendar.MINUTE), true)
										.show(getSupportFragmentManager());
			}
		});
		
		setMaxLength(eventPeriodIntervalVal, Event.MAX_PERIOD_INTERVAL_LENGTH);
		setMaxLength(eventNameVal, Event.MAX_NAME_LENGTH);
		setMaxLength(eventPlaceVal, Event.MAX_PLACE_LENGTH);
		setMaxLength(eventNoteVal, Event.MAX_NOTE_LENGTH);
		
		setInitValues();
		showEventPeriod();
		showEventAlarm();
		TimetableLogger.log("EventAddActivity created.");
		
	}

	private void createEventPeriodWeekDaysTable() {
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
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		mManager = new AlarmServiceManager(this);
		if (!mManager.bindService()) {
			TimetableLogger.error("Cannot bind to Service");
		}
		TimetableLogger.log("EventAddActivity: onResume()");
	}
	
	@Override 
	public void onPause() {
		super.onPause();
		mManager.unbindService();
	}
	/*
	 * try to get event date from intent extras and fill appropriate fields
	 */
	private void setInitValues() {
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		try {
			initDate = Calendar.getInstance();
			initDate.setTime(INIT_DATE_FORMAT.parse(extras.getString(EventAddActivity.INTENT_EXTRA_DATE)));
			setEventDate(getInitDate());
			setEventStartTime(getInitStartTime());
			setEventEndTime(getInitEndTime());
			setEventAlarmTime(getInitAlarmTime());
			setEventPeriodWeekOccurrences(getInitWeekOccurences());
			
		} catch (Exception e) {
			TimetableLogger.error("EventAddActivity.setInitValues:\n" + e.getMessage());
		}
	}
	
	/*
	 * returns date, that was set by default
	 */
	public Calendar getInitDate() {
		return initDate;
	}
	
	/*
	 * returns event's start time, that was set by default 
	 */
	public Calendar getInitStartTime() {
		Calendar initStartTime = Calendar.getInstance();
		initStartTime.setTime(initDate.getTime());
		initStartTime.add(Calendar.HOUR, 1);
		initStartTime.set(Calendar.MINUTE, 0);
		return initStartTime;
	}
	
	public Calendar getInitEndTime() {
		Calendar initEndTime = Calendar.getInstance();
		initEndTime.setTime(getInitStartTime().getTime());
		initEndTime.add(Calendar.HOUR, 1);
		return initEndTime;
	}
	

	public Calendar getInitAlarmTime() {
		Calendar initAlarmTime = Calendar.getInstance();
		initAlarmTime.setTime(initDate.getTime());
		initAlarmTime.add(Calendar.HOUR, -1);
		return initAlarmTime;
	}
	
	public boolean [] getInitWeekOccurences() {
		boolean [] initWeekOccurences = new boolean[7];
		initWeekOccurences[initDate.get(Calendar.DAY_OF_WEEK) - 1] = true;
		setEventPeriodWeekOccurrences(initWeekOccurences);
		return initWeekOccurences;
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
	
	public void setEventDate(Calendar date) {
		eventDateVal.setText(dateFormat.format(date.getTime()));	
	}
	public Calendar getEventDate() {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(dateFormat.parse(eventDateVal.getText().toString()));
		} catch (ParseException e) {
			return getInitDate();
		}
		return cal;
	}
	
	public void setEventStartTime(Calendar startTime) {
		eventStartTimeVal.setText(timeFormat.format(startTime.getTime()));
	}
	
	public Calendar getEventStartTime() {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(timeFormat.parse(eventStartTimeVal.getText().toString()));
		} catch (ParseException e) {
			return getInitStartTime();
		}
		return cal;
	}
	
	public void setEventEndTime(Calendar endTime) {
		eventEndTimeVal.setText(timeFormat.format(endTime.getTime()));
	}
	
	public Calendar getEventEndTime() {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(timeFormat.parse(eventEndTimeVal.getText().toString()));
		} catch (ParseException e) {
			return getEventEndTime();
		}
		return cal;
	}
	
	public boolean isSetEventAlarm() {
		return isSetEventAlarm;
	}
	
	public Date getEventAlarmTime() throws IllegalEventDataException {
		if (!isSetEventAlarm()) {
			return null;
		}
		return checker.getAlarmTimeFromString(eventAlarmTime.getText().toString());
	}
	
	public void setEventAlarm(EventAlarm alarm) {
		isSetEventAlarm = true;
		eventAlarmTime.setText(EventAlarm.timeFormat.format(alarm.time));
	}
	
	public void showEventAlarm() {
		if (isSetEventAlarm == false) {
			eventAlarmAddButton.setVisibility(View.VISIBLE);
			eventAlarmTypeSpinner.setVisibility(View.GONE);
			eventAlarmTime.setVisibility(View.GONE);
			eventAlarmDeleteButton.setVisibility(View.GONE);
		} else {
			eventAlarmAddButton.setVisibility(View.GONE);
			eventAlarmTypeSpinner.setVisibility(View.VISIBLE);
			eventAlarmTime.setVisibility(View.VISIBLE);
			eventAlarmDeleteButton.setVisibility(View.VISIBLE);
		}
	}
	
	public void setEventAlarmTime(Calendar alarmTime) {
		eventAlarmTime.setText(EventAlarm.timeFormat.format(alarmTime.getTime()));
	}
	
	public void showEventPeriodIntervalText() {
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
		eventMuteDeviceVal.setChecked(event.muteDevice);
		eventNoteVal.setText(event.note);
		setEventPeriod(event.period);
		if (event.hasAlarm()) {
			setEventAlarm(event.alarm);
		}
	}
	
	public Event getEvent() throws IllegalEventDataException {
		Event.Builder builder = new Event.Builder();
		//TODO: set focus on invalid text fields
		try {
			builder.setName(checker.getNameFromString(eventNameVal.getText().toString()))
					.setPlace(checker.getPlaceFromString(eventPlaceVal.getText().toString()))
					.setDate(checker.getDateFromString(eventDateVal.getText().toString()))
					.setStartTime(checker.getStartTimeFromString(eventStartTimeVal.getText().toString()))
					.setEndTime(checker.getEndTimeFromString(eventEndTimeVal.getText().toString()))
					.setNote(checker.getNoteFromString(eventNoteVal.getText().toString()))
					.setMuteDevice(eventMuteDeviceVal.isChecked())
					.setPeriod(getEventPeriod());
			Date alarmTime = getEventAlarmTime();
			if (alarmTime != null) {
				builder.setAlarmTime(alarmTime);
			}
			Event event = builder.build();
			TimetableLogger.log("EventAddActivity.getEvent:\n" +  event.toString());
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
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		if (db.insertEvent(event) == null) {
			Toast.makeText(this, "Error occured while saving event.", Toast.LENGTH_SHORT).show();
		}
		if (event.hasAlarm()) {
			mManager.getService().createAlarm(event.alarm);
		}
		if (event.muteDevice) {
			EventService.addEvent(this, event);
		}
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
