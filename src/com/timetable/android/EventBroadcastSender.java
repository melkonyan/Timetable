package com.timetable.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class EventBroadcastSender {
	
	public static void sendEventAddedBroadcast(Context context, Event event) {
		Intent intent = new Intent(BroadcastActions.ACTION_EVENT_ADDED);
		intent.putExtras(event.convert());
		context.sendBroadcast(intent);
		TimetableLogger.log("EventBroadcastSender.sendEventAddedBroadcast: broadcast is sent");
	}
	
	public static void sendEventUpdatedBroadcast(Context context, Event event) {
		Intent intent = new Intent(BroadcastActions.ACTION_EVENT_UPDATED);
		intent.putExtras(event.convert());
		context.sendBroadcast(intent);
		TimetableLogger.log("EventBroadcastSender.sendEventUpdatedBroadcast: broadcast is sent");
	}
	
	public static void sendEventDeletedBroadcast(Context context, Event event) {
		Intent intent = new Intent(BroadcastActions.ACTION_EVENT_DELETED);
		intent.putExtras(event.convert());
		context.sendBroadcast(intent);
		TimetableLogger.log("EventBroadcastSender.sendEventDeletedBroadcast: broadcast is sent");
	}
	
	public static void sendEventEndedBroadcast(Context context, Event event) {
		
	}
	
	public static void sendEventEndedBroadcast(Context context, Bundle eventData) {
		Intent intent = new Intent(BroadcastActions.ACTION_EVENT_ENDED);
		intent.putExtras(eventData);
		context.sendBroadcast(intent);
		TimetableLogger.log("EventBroadcastSender.sendEventEndedBroadcast: broadcast is sent");
	}
}
