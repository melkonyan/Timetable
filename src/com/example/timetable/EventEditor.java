package com.example.timetable;



import java.util.Date;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class EventEditor extends EventAdder {
	
	private TimetableActivity context;
	
	private Event event;
	
	private Event editedEvent;
	
	private Date date;
	
	public EventEditor(TimetableActivity context, Event event, Date date) {
		super(context);
		this.context = context;
		this.event = event;
		this.date = date;
		setEvent(event);
		showEventPeriod();
	}
	
	
	@Override 
	//return true, if page should be changed, false otherwise
	public boolean saveEvent() throws IllegalEventDataException {
		editedEvent = getEvent();
		editedEvent.id = event.id;
		editedEvent.period.id = event.period.id;
		// event event hasn't changed we do not to save it
		if (event.equals(editedEvent)) {
			return true;
		}
		if (event.isRepeatable()) {
			new SaveDialog(context); 
			return false;
		}
		TimetableDatabase db = new TimetableDatabase(context);
		db.updateEvent(editedEvent);
		db.close();
		return true;
	
	}
	
	private void saveRepeatableEvent(boolean overrideFutureEvents) {
		TimetableDatabase db = new TimetableDatabase(context);
		
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
	
	//return true, if page should be changed, false otherwise
	public boolean deleteEvent() {
		if (event.isRepeatable()) {
			new DeleteDialog(context);
			return false;
		}
		else {
			TimetableDatabase db = new TimetableDatabase(context);
			db.deleteEvent(event);
			db.close();
			return true;
		}
	}
	
	private void deleteRepeatableEvent(boolean deleteFutureEvents) {
		TimetableDatabase db = new TimetableDatabase(context);
		
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
						//need to change page manually
						context.getEventPager().update();
						context.switchPage(Page.EVENT_VIEW);
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
						//need to change page manually
						context.getEventPager().update();
						context.switchPage(Page.EVENT_VIEW);
						break;
				}
			}
		}
	}
}
