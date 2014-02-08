package com.example.timetable.tests;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.test.RenamingDelegatingContext;

import com.example.timetable.Event;
import com.example.timetable.EventEditor;
import com.example.timetable.EventDayViewActivity;
import com.example.timetable.TimetableLogger;

public class EventEditorTestCase extends ActivityInstrumentationTestCase2<EventDayViewActivity> {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	private Context mContext;
	
	public EventEditorTestCase() {
		super(EventDayViewActivity.class);
	}

	@Override
	public void setUp() {
		mContext = new RenamingDelegatingContext(getActivity(), "test_");
	}
	
	public void testGetEvent() {
		try {
			Event event = new Event();
			event.name = "Name";
			event.place = "Place";
			event.note = "Note";
			event.startTime = timeFormat.parse("17:30:00");
			event.date = dateFormat.parse("15.01.2014");
			//EventEditor mEditor = new EventEditor((TimetableActivity) mContext, event, event.date); 
			
			//Method getEventMethod = EventEditor.class.getDeclaredMethod("getEvent"); 
			//getEventMethod.setAccessible(true);
			//TimetableLogger.log(getEventMethod.invoke(mEditor).toString());
			//assertEquals(true, event.equals(getEventMethod.invoke(mEditor)));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		
	}
}
