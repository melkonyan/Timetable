package com.timetable.android.ui;



import java.util.Date;

import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.TimetableLogger;

/**
 * Class, that represents single day in {@link MonthView}.
 * Events are represented using their names.
 */
public class MonthCellView extends LinearLayout {

	Date mDisplayedDate;
	
	static final int mTextSize = 12;
	
	/**
	 * @param day - number of day to show.
	 * @param date - date to show.
	 */
	public MonthCellView(Context context, int day, Date date) {
		super(context);
		mDisplayedDate = date;
		setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.FILL_PARENT, GridView.LayoutParams.FILL_PARENT));
		setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
		setOrientation(VERTICAL);
		
		TextView dateView = new TextView(context);
		dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
		dateView.setTypeface(null, Typeface.BOLD);
		dateView.setText(""+day);
		addView(dateView);
		
	}
	
	public void addEvent(Event event) {
		addEventName(event.getName());
	}
	
	public void addEventName(String name) {
		TextView textView = new TextView(getContext());
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
		textView.setEllipsize(TextUtils.TruncateAt.END);
		textView.setSingleLine();
		textView.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
		textView.setText(name);
		addView(textView);
	
	}
	
	/**
	 * 
	 * @return date that is displayed by view.
	 */
	public Date getDate() {
		TimetableLogger.error(mDisplayedDate.toString());
		return mDisplayedDate;
	}
	
}
