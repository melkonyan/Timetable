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
import com.timetable.android.EventBroadcastSender;
import com.timetable.android.EventPeriod;
import com.timetable.android.IllegalEventDataException;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;

/*
 * Activity provides user interface to edit event
 * Should be started with extra field 'event_id' that contains id of event,
 * that should be edited and field 'date', that contains date, when event was edited.
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
								eventPeriodWeekDayCheckBoxes[i].setChecked(event.getPeriod().isWeekOccurrence(i));
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
		editedEvent.setId(event.getId());
		editedEvent.getPeriod().setId(event.getPeriod().getId());
		if (editedEvent.hasAlarm() && event.hasAlarm()) {
			editedEvent.getAlarm().id = event.getAlarm().id;	
		}
		// If event hasn't changed, we do not to save it
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
		EventBroadcastSender.sendEventUpdatedBroadcast(this, editedEvent);
		
		finish();
	}
	
	private void saveRepeatableEvent(boolean overrideFutureEvents) {
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		if (overrideFutureEvents) {
			//from today on this event ends
			TimetableLogger.log("EventEditActivity.saveRepetableEvent: saving event" + editedEvent.toString());
			long day = 1000*60*60*24;
			event.period.endDate = new Date();
			event.period.endDate.setTime(date.getTime() - day);
			if (event.period.isFinished(event.date)) {
				db.deleteEvent(event);
				EventBroadcastSender.sendEventDeletedBroadcast(this, event);
				
			} else {
				db.updateEvent(event);
				EventBroadcastSender.sendEventUpdatedBroadcast(this, event);
				
			}
			
			editedEvent = db.insertEvent(editedEvent);
			//copy exception to new event
			for (Date exception: event.exceptions) {
				db.insertException(editedEvent, exception);
			}
		
		} else {
			//today there is no session of this event
			event.addException(date);
			db.insertException(event, date);
			editedEvent.period.type = EventPeriod.Type.NONE;
			editedEvent = db.insertEvent(editedEvent);
			EventBroadcastSender.sendEventUpdatedBroadcast(this, event);
			
		}
		
		EventBroadcastSender.sendEventAddedBroadcast(this, editedEvent);
	}
	
	public void deleteEvent() {
		if (event.isRepeatable()) {
			new DeleteDialog(this);
			return;
		}
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		db.deleteEvent(event);
		EventBroadcastSender.sendEventDeletedBroadcast(this, event);
		finish();
	}
	
	private void deleteRepeatableEvent(boolean deleteFutureEvents) {
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		if (deleteFutureEvents) {
			//from today on this event ends
			long day = 1000*60*60*24;
			event.period.endDate = new Date();
			event.period.endDate.setTime(date.getTime() - day);
			if (event.period.isFinished(event.date)) {
				db.deleteEvent(event);
				EventBroadcastSender.sendEventDeletedBroadcast(this, event);
			} else {
				db.updateEvent(event);
				EventBroadcastSender.sendEventUpdatedBroadcast(this, event);
			}
			
		} else {
			//today there is no session of this event
			event.addException(date);
			db.insertException(event, date);
			EventBroadcastSender.sendEventUpdatedBroadcast(this, event);
		}
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
