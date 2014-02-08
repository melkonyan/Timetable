package com.example.timetable;

import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
			TimetableDatabase db = new TimetableDatabase(this);
			event = db.searchEventById(eventId);
			if (event == null) {
				TimetableLogger.error("EventEditActivity: event not found. " + Integer.toString(eventId));
				finish();
			}
			date = INIT_DATE_FORMAT.parse(extras.getString("date"));
			
			setEvent(event);
			showEventPeriod();
			eventDateVal.setText(dateFormat.format(date));
		} catch(Exception e) {
			TimetableLogger.error("EventEditActivity received illegal data.");
			finish();
		}
		
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
		// event event hasn't changed we do not to save it
		if (event.equals(editedEvent)) {
			finish();
			return;
		}
		if (event.isRepeatable()) {
			new SaveDialog(this); 
			return;
		}
		TimetableDatabase db = new TimetableDatabase(this);
		db.updateEvent(editedEvent);
		db.close();
	}
	
	private void saveRepeatableEvent(boolean overrideFutureEvents) {
		TimetableDatabase db = new TimetableDatabase(this);
		
		if (overrideFutureEvents) {
			//from today on this event ends
			//TODO: fix this shit
			TimetableLogger.log("saving event:" + editedEvent.toString());
			long day = 1000*60*60*24;
			event.period.endDate = new Date();
			event.period.endDate.setTime(date.getTime() - day);
			db.updateEvent(event);
			db.insertEvent(editedEvent);
		} else {
			//today there is no session of this event
			db.insertException(event, date);
			editedEvent.period.type = EventPeriod.Type.NONE;
			db.insertEvent(editedEvent);
		}
		db.close();
	}
	
	public void deleteEvent() {
		if (event.isRepeatable()) {
			new DeleteDialog(this);
			return;
		}
		TimetableDatabase db = new TimetableDatabase(this);
		db.deleteEvent(event);
		db.close();
		finish();
	}
	
	private void deleteRepeatableEvent(boolean deleteFutureEvents) {
		TimetableDatabase db = new TimetableDatabase(this);
		
		if (deleteFutureEvents) {
			//from today on this event ends
			long day = 1000*60*60*24;
			event.period.endDate = new Date();
			event.period.endDate.setTime(date.getTime() - day);
			db.updateEvent(event);
		} else {
			//today there is no session of this event
			db.insertException(event, date);
		}
	}
	
	private class SaveDialog extends AlertDialog {
		
		public SaveDialog(Context context) {
			super(context);
			AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(context);
			saveDialogBuilder.setTitle("Save changes?");
			SaveDialogOnClickListener mListener = new SaveDialogOnClickListener(); 
			saveDialogBuilder.setPositiveButton("Save", mListener);
			saveDialogBuilder.setNeutralButton("Cancel", mListener);
			saveDialogBuilder.setSingleChoiceItems(new String [] {"Change only this event","Change all future events"}, 1, null);
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
			saveDialogBuilder.setTitle("Delete event?");
			SaveDialogOnClickListener mListener = new SaveDialogOnClickListener(); 
			saveDialogBuilder.setPositiveButton("Delete", mListener);
			saveDialogBuilder.setNeutralButton("Cancel", mListener);
			saveDialogBuilder.setSingleChoiceItems(new String [] {"Delete only this event","Delete all future events"}, 1, null);
			saveDialogBuilder.show(); 
		}
		
		private class SaveDialogOnClickListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int buttonType) {
				switch (buttonType) {
					case DialogInterface.BUTTON_POSITIVE:
						int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
						deleteRepeatableEvent(selectedPosition == 1); 
						finish();
						break;
				}
			}
		}
	}

}
