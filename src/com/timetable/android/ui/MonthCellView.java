package com.timetable.android.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

import android.content.Context;

import com.timetable.android.Event;

/**
 * Class, that represents single day in {@link MonthView}.
 * Events are represented using their names.
 */
public class MonthCellView extends LinearLayout {

	/**
	 * @param day - number of day to show.
	 */
	public MonthCellView(Context context, int day) {
		super(context);
		setOrientation(VERTICAL);
		TextView dateView = new TextView(context);
		dateView.setText(day);
	}
	
	public void addEvent(Event event) {
		TextView textView = new TextView(getContext());
		textView.setText(event.getName());
		addView(textView);
	}

	
}
