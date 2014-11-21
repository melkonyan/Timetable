package com.timetable.android.ui;

import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

import android.content.Context;

import com.timetable.android.Event;

/**
 * Class, that represents single day in {@link MonthView}.
 * Events are represented using their names.
 */
public class MonthCellView extends LinearLayout {

	public MonthCellView(Context context) {
		super(context);
		setOrientation(VERTICAL);
	}
	
	public void addEvent(Event event) {
		TextView textView = new TextView(getContext());
		textView.setText(event.getName());
		addView(textView);
	}

	
}
