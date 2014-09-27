package com.timetable.android;


import java.util.Date;

import org.holoeverywhere.widget.Toast;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.timetable.android.alarm.EventAlarm;
import com.timetable.android.utils.DateUtils;

/*
 * Class for saving events to database, updating and deleting them. If needed, shows dialog for user to choose some option.
 */
public class EventController {
	
	private Context mContext; 
	
	private TimetableDatabase db; 
	
	private OnEventSavedListener mOnEventSavedListener;
	
	private OnEventUpdatedListener mOnEventUpdatedListener;
	
	private OnEventDeletedListener mOnEventDeletedListener;
	
	public static final long MAX_TIME_TILL_NEXT_ALARM_OCCURRENCE = 1000 * 60 * 60 * 24;
	
	public EventController(Context context) {
		mContext = context;
		db = TimetableDatabase.getInstance(mContext);
	}
	
	public void setOnEventSavedListener(OnEventSavedListener onEventSavedListener) {
		mOnEventSavedListener = onEventSavedListener;
	}
	
	public void setOnEventUpdatedListener(OnEventUpdatedListener onEventUpdatedListener) {
		mOnEventUpdatedListener = onEventUpdatedListener;
	}
	
	public void setOnEventDeletedListener(OnEventDeletedListener onEventDeletedListener) {
		mOnEventDeletedListener = onEventDeletedListener;
	}
	
	public static String getAlarmToastMessage(long timeTillNextOccurrence) {
		if (timeTillNextOccurrence > MAX_TIME_TILL_NEXT_ALARM_OCCURRENCE) {
			return null;
		}
		long hoursLeft = timeTillNextOccurrence / DateUtils.HOUR_MILLIS;
		long minutesLeft = (timeTillNextOccurrence % DateUtils.HOUR_MILLIS)  / DateUtils.MINUTE_MILLIS;
		String message = "Alarm is set for ";
		if (hoursLeft > 0) {
			message += Long.toString(hoursLeft) + " hour";
			if (hoursLeft % 10 != 1) {
				message += "s";
			}
			message += " ";
		}
		if (hoursLeft > 0 && minutesLeft > 0) {
			message += "and ";
		}
		if (minutesLeft > 0) {
			message += Long.toString(minutesLeft) + " minute";
			if (minutesLeft % 10 != 1) {
				message += "s";
			}
			message += " ";
			
		}
		if (hoursLeft == 0 && minutesLeft == 0) {
			message += "less than minute ";
		}
		
		message += "from now.";
		return message;
	}
	
	/*
	 * If event has alarm, and it has next occurrence in 12 hours, show time till next alarm occurrence
	 */
	public void showAlarmToast(Event event) {
		EventAlarm alarm = event.getAlarm();
		Date nextOccurrence = alarm.getTimeTillNextOccurrence();
		if (nextOccurrence == null) {
			return;
		}
		
		long timeTillNextOccurrence = alarm.getTimeTillNextOccurrence().getTime();
		String message = getAlarmToastMessage(timeTillNextOccurrence);
		if (message != null) {
			Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
		}
	}
	
	/*
	 * Show time till next occurrence if alarm has changed.
	 */
	public void showAlarmToast(Event editedEvent, Event oldEvent) {
		if (editedEvent.hasAlarm() && (!oldEvent.hasAlarm() || !editedEvent.getAlarm().equals(oldEvent.getAlarm()) ) ) {
			showAlarmToast(editedEvent);
		}
	
	}
	/*
	 * Save given event to database. Call onEventSaved after that.
	 */
	public void saveEvent(Event event) {
		Event savedEvent = db.insertEvent(event);
		if (savedEvent != null) {
			EventBroadcastSender.sendEventAddedBroadcast(mContext, event);
			if (savedEvent.hasAlarm()) {
				showAlarmToast(savedEvent);
			}
		}
		
		mOnEventSavedListener.onEventSaved(savedEvent);
	}
	
	/*
	 * When repeatable oldEvent is updated or deleted and option "Override future events" is selected by user, 
	 * new version of the oldEvent is inserted into the database, and the end editDate of old version is updated,
	 * so that old oldEvent is already finished on the editDate of editing.
	 */
	private void updateOldEventEndDate(Event oldEvent, Date editDate) {
		//TODO: collide this function an addOldEventException into one function updateOldEvent
		// change updateEvent method in the database, so that all oldEvent's exception would be updated.
		oldEvent.getPeriod().setEndDate(editDate);
		if (oldEvent.getPeriod().isFinished(oldEvent.getDate())) {
			db.deleteEvent(oldEvent);
			EventBroadcastSender.sendEventDeletedBroadcast(mContext, oldEvent);
		} else {
			db.updateEvent(oldEvent);
			EventBroadcastSender.sendEventUpdatedBroadcast(mContext, oldEvent);
			
		}
		
	}
	
	/*
	 * When repeatable oldEvent is updated or deleted and option "Change only this oldEvent" is selected by user,
	 * new version of the oldEvent is inserted into the database, and date of editing is added to exceptions of old oldEvent.
	 */
	private void addOldEventException(Event oldEvent, Date editDate) {
		oldEvent.addException(editDate);
		db.insertException(oldEvent, editDate);
		EventBroadcastSender.sendEventUpdatedBroadcast(mContext, oldEvent);	
	}
	
	private void updateRepeatableEvent(Event editedEvent, Event oldEvent, Date editDate, boolean overrideFutureEvents) {
		
		Event updatedEvent;
		if (overrideFutureEvents) {
			//from today on this oldEvent ends
			updateOldEventEndDate(oldEvent, editDate);
			
			updatedEvent = db.insertEvent(editedEvent);
			//copy exceptions to new oldEvent
			for (Date exception: editedEvent.getExceptions()) {
				db.insertException(editedEvent, exception);
			}
		} else {
			//today there is no session of this oldEvent
			addOldEventException(oldEvent, editDate);
			editedEvent.getPeriod().setType(EventPeriod.NONE);
			updatedEvent = db.insertEvent(editedEvent);
			
		}
		
		if (editedEvent.hasAlarm() && (!oldEvent.hasAlarm() || !editedEvent.getAlarm().equals(oldEvent.getAlarm()) ) ) {
			showAlarmToast(editedEvent);
		}
		EventBroadcastSender.sendEventAddedBroadcast(mContext, updatedEvent);
		mOnEventUpdatedListener.onEventUpdated(updatedEvent);
	}
	
	/*
	 * Update given event. If event is periodic show dialog to choose update option.
	 * @param editedEvent - event after editing
	 * @param oldEvent - event before editing
	 * @param editDate - date of editing 
	 */
	public void updateEvent(Event editedEvent, Event oldEvent, Date editDate) {
		editedEvent.setId(oldEvent.getId());
		editedEvent.getPeriod().setId(oldEvent.getPeriod().getId());
		if (editedEvent.hasAlarm() && oldEvent.hasAlarm()) {
			editedEvent.getAlarm().id = oldEvent.getAlarm().id;	
		}
		// If oldEvent hasn't changed, we do not to save it
		if (oldEvent.equals(editedEvent)) {
			mOnEventUpdatedListener.onEventUpdated(editedEvent);
			return;
		}
		
		//If oldEvent is periodic, ask user to choose update options.
		if (oldEvent.isRepeatable()) {
			new SaveDialog(mContext, editedEvent, oldEvent, editDate); 
			return;
		}
		
		Event updatedEvent = db.updateEvent(editedEvent);
		if (updatedEvent != null) {
			EventBroadcastSender.sendEventUpdatedBroadcast(mContext, updatedEvent);
			showAlarmToast(updatedEvent, oldEvent);
		}
		mOnEventUpdatedListener.onEventUpdated(updatedEvent);
	}
	
	public void deleteEvent(Event event, Date deleteDate) {
		if (event.isRepeatable()) {
			new DeleteDialog(mContext, event, deleteDate);
			return;
		}
		db.deleteEvent(event);
		EventBroadcastSender.sendEventDeletedBroadcast(mContext, event);
		mOnEventDeletedListener.onEventDeleted();
	}
	
	private void deleteRepeatableEvent(Event event, Date deleteDate, boolean deleteFutureEvents) {
		if (deleteFutureEvents) {
			//from today on this oldEvent ends
			updateOldEventEndDate(event, deleteDate);
		} else {
			//today there is no session of this oldEvent
			addOldEventException(event, deleteDate);
		}
		mOnEventDeletedListener.onEventDeleted();
	}
	
	public static interface OnEventSavedListener {
		
		public void onEventSaved(Event savedEvent);
	
	}
	
	public static interface OnEventUpdatedListener {
		
		public void onEventUpdated(Event updatedEvent);
	
	}
	
	public static interface OnEventDeletedListener {
		
		public void onEventDeleted();
	
	}
	
	private class SaveDialog extends AlertDialog {
		
		private Event mEditedEvent;
		
		private Event mOldEvent; 
		
		private Date mEditDate;
		
		public SaveDialog(Context context, Event editedEvent, Event oldEvent, Date editDate) {
			super(context);
			mEditedEvent = editedEvent;
			mOldEvent = oldEvent;
			mEditDate = editDate;
			AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(context);
			saveDialogBuilder.setTitle(R.string.dialog_title_save_event);
			SaveDialogOnClickListener mListener = new SaveDialogOnClickListener(); 
			saveDialogBuilder.setPositiveButton(R.string.dialog_button_save, mListener);
			saveDialogBuilder.setNeutralButton(R.string.dialog_button_cancel, mListener);
			saveDialogBuilder.setSingleChoiceItems(new String [] {mContext.getResources().getString(R.string.dialog_option_change_this_event),
																	mContext.getResources().getString(R.string.dialog_option_change_all_events)}, 1, null);
			saveDialogBuilder.show(); 
		}
		
		private class SaveDialogOnClickListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int buttonType) {
				switch (buttonType) {
					case DialogInterface.BUTTON_POSITIVE:
						int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
						updateRepeatableEvent(mEditedEvent, mOldEvent, mEditDate, selectedPosition == 1); 
						break;
				}
			}
		}
	}
	
	private class DeleteDialog extends AlertDialog {
		
		private Event mEvent;
		
		private Date mDeleteDate;
		
		public DeleteDialog(Context context, Event event, Date deleteDate) {
			super(context);
			mEvent = event;
			mDeleteDate = deleteDate;
			
			AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(context);
			saveDialogBuilder.setTitle(R.string.dialog_title_delete_event);
			SaveDialogOnClickListener mListener = new SaveDialogOnClickListener(); 
			saveDialogBuilder.setPositiveButton(R.string.dialog_button_delete, mListener);
			saveDialogBuilder.setNeutralButton(R.string.dialog_button_cancel, mListener);
			saveDialogBuilder.setSingleChoiceItems(new String [] {mContext.getResources().getString(R.string.dialog_option_delete_this_event),
																	mContext.getResources().getString(R.string.dialog_option_delete_all_events)}, 1, null);
			saveDialogBuilder.show(); 
		}
		
		private class SaveDialogOnClickListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int buttonType) {
				switch (buttonType) {
					case DialogInterface.BUTTON_POSITIVE:
						int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
						deleteRepeatableEvent(mEvent, mDeleteDate, selectedPosition == 1); 
						break;
				}
			}
		}
	}

	
}
