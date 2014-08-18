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
 * Activity provides user interface to edit oldEvent
 * Should be started with extra field 'event_id' that contains id of oldEvent,
 * that should be edited and field 'date', that contains editDate, when oldEvent was edited.
 */
public class EventEditActivity extends EventAddActivity {

	public static final String EXTRA_EVENT_ID = "event_id";
	
	//oldEvent that should be edited
	private Event oldEvent;
	
	//new data
	private Event editedEvent;
	
	//editDate when oldEvent is edited
	private Date editDate;
	
	private TimetableDatabase db; 
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			Bundle extras = getIntent().getExtras();
			int eventId = extras.getInt(EXTRA_EVENT_ID);
			db = TimetableDatabase.getInstance(this);
			oldEvent = db.searchEventById(eventId);
			if (oldEvent == null) {
				TimetableLogger.error("EventEditActivity.onCreate: oldEvent not found. " + Integer.toString(eventId));
				finish();
				return;
			}
			getSupportActionBar().setTitle(getResources().getString(R.string.actionbar_edit_event));
			
			try {
				editDate = INIT_DATE_FORMAT.parse(extras.getString(EXTRA_DATE));
			} catch(ParseException e) {
				TimetableLogger.error("EventEditActivity.onCreate: could not parse date of editing. " + e.getMessage());
				finish();
				return;
			}
			
			setEvent(oldEvent);
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
								eventPeriodWeekDayCheckBoxes[i].setChecked(oldEvent.getPeriod().isWeekOccurrence(i));
							}
						eventPeriodWeekDayCheckBoxes[weekDay].setEnabled(false);
						eventPeriodWeekDayCheckBoxes[weekDay].setChecked(true);
						}
					} catch (ParseException e) {
						return;
					}
				}
			});
			
			eventDateVal.setText(dateFormat.format(editDate));
			
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
	        	TimetableLogger.log("EventEditActivity: try to save oldEvent.");
	        	try {
	        		saveEvent();
	        	} catch (IllegalEventDataException e) {
	            	Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	            }
	        	return true;
	        case R.id.action_delete_event:
	        	TimetableLogger.log("EventEditActivity: try to delete oldEvent.");
	        	deleteEvent();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override 
	public void saveEvent() throws IllegalEventDataException {
		editedEvent = getEvent();
		editedEvent.setId(oldEvent.getId());
		editedEvent.getPeriod().setId(oldEvent.getPeriod().getId());
		if (editedEvent.hasAlarm() && oldEvent.hasAlarm()) {
			editedEvent.getAlarm().id = oldEvent.getAlarm().id;	
		}
		// If oldEvent hasn't changed, we do not to save it
		if (oldEvent.equals(editedEvent)) {
			finish();
			return;
		}
		
		if (oldEvent.isRepeatable()) {
			new SaveDialog(this); 
			return;
		}
		
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		editedEvent = db.updateEvent(editedEvent);
		EventBroadcastSender.sendEventUpdatedBroadcast(this, editedEvent);
		
		finish();
	}
	
	/*
	 * When repeatable oldEvent is updated or deleted and option "Override future events" is selected by user, 
	 * new version of the oldEvent is inserted into the database, and the end editDate of old version is updated,
	 * so that old oldEvent is already finished on the editDate of editing.
	 */
	private void updateOldEventEndDate() {
		//TODO: collide this function an addOldEventException into one function updateOldEvent
		// change updateEvent method in the database, so that all oldEvent's exception would be updated.
		oldEvent.getPeriod().setEndDate(editDate);
		if (oldEvent.getPeriod().isFinished(oldEvent.getDate())) {
			db.deleteEvent(oldEvent);
			EventBroadcastSender.sendEventDeletedBroadcast(this, oldEvent);
		} else {
			db.updateEvent(oldEvent);
			EventBroadcastSender.sendEventUpdatedBroadcast(this, oldEvent);
			
		}
		
	}
	
	/*
	 * When repeatable oldEvent is updated or deleted and option "Change only this oldEvent" is selected by user,
	 * new version of the oldEvent is inserted into the database, and editDate of editing is added to exceptions of old oldEvent.
	 */
	private void addOldEventException() {
		oldEvent.addException(editDate);
		db.insertException(oldEvent, editDate);
		EventBroadcastSender.sendEventUpdatedBroadcast(this, oldEvent);	
	}
	
	private void saveRepeatableEvent(boolean overrideFutureEvents) {
		if (overrideFutureEvents) {
			//from today on this oldEvent ends
			updateOldEventEndDate();
			
			editedEvent = db.insertEvent(editedEvent);
			//copy exceptions to new oldEvent
			for (Date exception: oldEvent.getExceptions()) {
				db.insertException(editedEvent, exception);
			}
		
		} else {
			//today there is no session of this oldEvent
			addOldEventException();
			editedEvent.getPeriod().setType(EventPeriod.NONE);
			editedEvent = db.insertEvent(editedEvent);
			
		}
		
		EventBroadcastSender.sendEventAddedBroadcast(this, editedEvent);
	}
	
	public void deleteEvent() {
		if (oldEvent.isRepeatable()) {
			new DeleteDialog(this);
			return;
		}
		TimetableDatabase db = TimetableDatabase.getInstance(this);
		db.deleteEvent(oldEvent);
		EventBroadcastSender.sendEventDeletedBroadcast(this, oldEvent);
		finish();
	}
	
	private void deleteRepeatableEvent(boolean deleteFutureEvents) {
		if (deleteFutureEvents) {
			//from today on this oldEvent ends
			updateOldEventEndDate();
		} else {
			//today there is no session of this oldEvent
			addOldEventException();
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
