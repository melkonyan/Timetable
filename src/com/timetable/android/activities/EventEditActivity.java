package com.timetable.android.activities;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.timetable.android.Event;
import com.timetable.android.EventPeriod;
import com.timetable.android.IllegalEventDataException;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;
import com.timetable.android.alarm.AlarmService;
import com.timetable.android.R;

/*
 * Activity provides user interface to edit event
 * Should be started with extra field 'event_id' that contains id of event,
 * that should be edited and field 'date', that contains date, when event was edited
 */
public class EventEditActivity extends EventAddActivity {

	//event that should be edited
	private Event event;
	
	//new data
	private Event editedEvent;
	
	//date when event is edited
	private Date date;
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Bundle extras = getIntent().getExtras();
			int eventId = extras.getInt("event_id");
			TimetableDatabase db = TimetableDatabase.getInstance(this);
			event = db.searchEventById(eventId);
			if (event == null) {
				TimetableLogger.error("EventEditActivity: event not found. " + Integer.toString(eventId));
				finish();
			}
			getSupportActionBar().setTitle(getResources().getString(R.string.actionbar_edit_event));
			
			date = INIT_DATE_FORMAT.parse(extras.getString("date"));
			
			setEvent(event);
			showEventPeriod();
			showEventAlarm();
			eventDateVal.removeTextChangedListener(eventAddTextWatcher);
			eventDateVal.addTextChangedListener(new TextWatcher() {
				
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
						for (int i = EventPeriod.SUNDAY; i <= EventPeriod.SATURDAY; i++) {
							if (!eventPeriodWeekDayCheckBoxes[i].isEnabled()) {
								eventPeriodWeekDayCheckBoxes[i].setEnabled(true);
								eventPeriodWeekDayCheckBoxes[i].setChecked(event.period.isWeekOccurrence(i));
							}
						eventPeriodWeekDayCheckBoxes[weekDay].setEnabled(false);
						eventPeriodWeekDayCheckBoxes[weekDay].setChecked(true);
						}
					} catch (ParseException e) {
						return;
					}
				}
			});
			
			eventDateVal.setText(dateFormat.format(date));
			
		} catch(Exception e) {
			TimetableLogger.error("EventEditActivity received illegal data.");
			finish();
		}
		TimetableLogger.log("EventEditActivity successfully created.");
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_event_edit, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_save_event:
	        	TimetableLogger.log("EventEditActivity: try to save event.");
	        	try {
	        		saveEvent();
	        	} catch (IllegalEventDataException e) {
	            	Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	            }
	        	return true;
	        case R.id.action_delete_event:
	        	TimetableLogger.log("EventEditActivity: try to delete event.");
	        	deleteEvent();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override 
	public void saveEvent() throws IllegalEventDataException {
		editedEvent = getEvent();
		editedEvent.id = event.id;
		editedEvent.period.id = event.period.id;
		if (editedEvent.hasAlarm()) {
			if (event.hasAlarm()) {
				editedEvent.alarm.id = event.hasAlarm() ? event.alarm.id: -1;	
			}
			editedEvent.alarm.eventId = editedEvent.id;
		}
		// event event hasn't changed we do not to save it
		if (event.equals(editedEvent)) {
			finish();
			return;
		}
		
		if (event.isRepeatable()) {
			new SaveDialog(this); 
			return;
		}
		
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		editedEvent = db.updateEvent(editedEvent);
		updateEventAlarmManager();
		db.close();
		finish();
	}
	
	private void saveRepeatableEvent(boolean overrideFutureEvents) {
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		AlarmService alarmService = mManager.getService(); 
		if (overrideFutureEvents) {
			//from today on this event ends
			TimetableLogger.log("saving event:" + editedEvent.toString());
			long day = 1000*60*60*24;
			event.period.endDate = new Date();
			event.period.endDate.setTime(date.getTime() - day);
			db.updateEvent(event);
			editedEvent = db.insertEvent(editedEvent);
			for (Date exception: event.exceptions) {
				db.insertException(editedEvent, exception);
			}
			if (event.hasAlarm()) {
				event.alarm.period = event.period;
				alarmService.updateAlarm(event.alarm);
			}
		} else {
			//today there is no session of this event
			db.insertException(event, date);
			//TODO: eliminate code duplication(same code as in deleteRepeatableEvent
			if (event.hasAlarm()) {
				event.addException(date);
				event.alarm.event = event;
				alarmService.updateAlarm(event.alarm);
			}
			editedEvent.period.type = EventPeriod.Type.NONE;
			editedEvent = db.insertEvent(editedEvent);
		}
		if (editedEvent.hasAlarm()) {
			editedEvent.alarm.period = editedEvent.period;
			alarmService.createAlarm(editedEvent.alarm);
		}
		db.close();
	}
	
	public void deleteEvent() {
		if (event.isRepeatable()) {
			new DeleteDialog(this);
			return;
		}
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		db.deleteEvent(event);
		if (event.hasAlarm()) {
			AlarmService alarmService = mManager.getService();
			alarmService.deleteAlarm(event.alarm);
		}
		db.close();
		finish();
	}
	
	private void deleteRepeatableEvent(boolean deleteFutureEvents) {
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		AlarmService alarmService = mManager.getService();
		
		if (deleteFutureEvents) {
			//from today on this event ends
			long day = 1000*60*60*24;
			event.period.endDate = new Date();
			event.period.endDate.setTime(date.getTime() - day);
			event = db.updateEvent(event);
			if (event.hasAlarm()) {
				event.alarm.period = event.period;
				alarmService.updateAlarm(event.alarm);
			}
		} else {
			//today there is no session of this event
			db.insertException(event, date);
			if (event.hasAlarm()) {
				event.addException(date);
				event.alarm.event = event;
				alarmService.updateAlarm(event.alarm);
			}
		}
		db.close();
	}
	
	private void updateEventAlarmManager() {
		//TODO: may be event updated listener would be better
		AlarmService alarmService = mManager.getService();
		if (editedEvent.hasAlarm()) {
			alarmService.updateAlarm(editedEvent.alarm);
		} else if (event.hasAlarm()) {
			alarmService.deleteAlarm(event.alarm);
		}
		mManager.unbindService();
	}
	
	private class SaveDialog extends AlertDialog {
		
		public SaveDialog(Context context) {
			super(context);
			AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(context);
			saveDialogBuilder.setTitle(R.string.dialog_title_save_event);
			SaveDialogOnClickListener mListener = new SaveDialogOnClickListener(); 
			saveDialogBuilder.setPositiveButton(R.string.dialog_button_save, mListener);
			saveDialogBuilder.setNeutralButton(R.string.dialog_button_cancel, mListener);
			saveDialogBuilder.setSingleChoiceItems(new String [] {getResources().getString(R.string.dialog_option_change_this_event),
																	getResources().getString(R.string.dialog_option_change_all_events)}, 1, null);
			saveDialogBuilder.show(); 
		}
		
		private class SaveDialogOnClickListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int buttonType) {
				switch (buttonType) {
					case DialogInterface.BUTTON_POSITIVE:
						int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
						saveRepeatableEvent(selectedPosition == 1); 
						finish();
						break;
				}
			}
		}
	}
	
	private class DeleteDialog extends AlertDialog {
		
		public DeleteDialog(Context context) {
			super(context);
			AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(context);
			saveDialogBuilder.setTitle(R.string.dialog_title_delete_event);
			SaveDialogOnClickListener mListener = new SaveDialogOnClickListener(); 
			saveDialogBuilder.setPositiveButton(R.string.dialog_button_delete, mListener);
			saveDialogBuilder.setNeutralButton(R.string.dialog_button_cancel, mListener);
			saveDialogBuilder.setSingleChoiceItems(new String [] {getResources().getString(R.string.dialog_option_delete_this_event),
																	getResources().getString(R.string.dialog_option_delete_all_events)}, 1, null);
			saveDialogBuilder.show(); 
		}
		
		private class SaveDialogOnClickListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int buttonType) {
				switch (buttonType) {
					case DialogInterface.BUTTON_POSITIVE:
						int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
						deleteRepeatableEvent(selectedPosition == 1); 
						TimetableLogger.log("Event was saved. Leaving EventEditActivity.");
						finish();
						break;
				}
			}
		}
	}
	
}
