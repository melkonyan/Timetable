package com.timetable.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/*
 * Class, that can send all main broadcasts.
 */
public class EventBroadcastSender {
	
	public static void sendEventAddedBroadcast(Context context, Event event) {
		Intent intent = new Intent(BroadcastActions.ACTION_EVENT_ADDED);
		intent.putExtras(event.convert());
		context.sendBroadcast(intent);
		Logger.log("EventBroadcastSender.sendEventAddedBroadcast: broadcast is sent");
	}
	
	public static void sendEventUpdatedBroadcast(Context context, Event event) {
		Intent intent = new Intent(BroadcastActions.ACTION_EVENT_UPDATED);
		intent.putExtras(event.convert());
		context.sendBroadcast(intent);
		Logger.log("EventBroadcastSender.sendEventUpdatedBroadcast: broadcast is sent");
	}
	
	public static void sendEventDeletedBroadcast(Context context, Event event) {
		Intent intent = new Intent(BroadcastActions.ACTION_EVENT_DELETED);
		intent.putExtras(event.convert());
		context.sendBroadcast(intent);
		Logger.log("EventBroadcastSender.sendEventDeletedBroadcast: broadcast is sent");
	}
	
	public static void sendEventEndedBroadcast(Context context, Event event) {
		sendEventEndedBroadcast(context, event.convert());
	}
	
	public static void sendEventEndedBroadcast(Context context, Bundle eventData) {
		Intent intent = new Intent(BroadcastActions.ACTION_EVENT_ENDED);
		intent.putExtras(eventData);
		context.sendBroadcast(intent);
		Logger.log("EventBroadcastSender.sendEventEndedBroadcast: broadcast is sent");
	}
}
