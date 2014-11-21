package com.timetable.android;

/*
 * Class that contains all actions, that can be broadcasted inside of application.
 */
public class BroadcastActions {
	
	/*
	 * Action to broadcast, when application is started
	 */
	public static final String ACTION_APP_STARTED = "com.timetable.android.ACTION_APP_STARTED";
	
	/*
	 * Action to broadcast, when new event is added to database
	 */
	public static final String ACTION_EVENT_ADDED = "com.timetable.android.ACTION_EVENT_ADDED";
	
	/*
	 * Action to broadcast, when event is updated.
	 */
	public static final String ACTION_EVENT_UPDATED = "com.timetable.android.ACTION_EVENT_UPDATED";
	
	/*
	 * Action to broadcast, when event is deleted.
	 */
	public static final String ACTION_EVENT_DELETED = "com.timetable.android.ACTION_EVENT_DELETED";
	
	/*
	 * Action to broadcast, when event is started
	 */
	public static final String ACTION_EVENT_STARTED = "com.timetable.android.ACTION_EVENT_STARTED";
	
	/*
	 * Action to broadcast, when event is ended
	 */
	public static final String ACTION_EVENT_ENDED = "com.timetable.android.ACTION_EVENT_ENDED";
	
	
}
