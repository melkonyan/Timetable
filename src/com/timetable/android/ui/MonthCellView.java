package com.timetable.android.ui;

import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.text.TextUtils;

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
		//setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
		TextView dateView = new TextView(context);
		dateView.setText(""+day);
		addView(dateView);
	}
	
	public void addEvent(Event event) {
		addEventName(event.getName());
	}
	
	public void addEventName(String name) {
		TextView textView = new TextView(getContext());
		textView.setEllipsize(TextUtils.TruncateAt.END);
		textView.setSingleLine();
		textView.setText(name);
		
		addView(textView);
	
	}

	
}
