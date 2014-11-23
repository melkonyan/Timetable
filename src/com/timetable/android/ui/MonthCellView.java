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
	
	static final int mDateTextSize = 12;
	
	static final int mDataTextSize = 8;
	
	static final int mPadding = 5;
	
	static int mTextViewHeight = -1;
	
	//Total height, that is available to view.
	int mAvailableHeight = -1;
	
	int mCurrHeight = 0;
	
	
	/**
	 * @param day - number of day to show.
	 * @param date - date to show.
	 * @param availableHeight - height of space that is available to view.
	 * @param availableWidth - width of space that is available to view.
	 */
	public MonthCellView(Context context, int day, Date date, int availableHeight, int availableWidth) {
		super(context);
		mDisplayedDate = date;
		setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.FILL_PARENT, GridView.LayoutParams.FILL_PARENT));
		setBackgroundDrawable(getResources().getDrawable(R.drawable.month_cell_background));
		setOrientation(VERTICAL);
		
		if (availableHeight > 0) {
			setMinimumHeight(availableHeight);
			mAvailableHeight = availableHeight;
		}
		if (availableWidth > 0) {
			setMinimumWidth(availableWidth);
		}
		
		TextView dateView = new TextView(context);
		dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mDateTextSize);
		dateView.setPadding(mPadding, mPadding, 0, 0);
		dateView.setTypeface(null, Typeface.BOLD);
		dateView.setText(""+day);
		mCurrHeight += getTextViewHeight(dateView);
		addView(dateView);
		
	}
	
	public MonthCellView(Context context, int day, Date date) {
		this(context, day, date, -1, -1);
	}
	
	int getTextViewHeight(TextView view) {
		if (mTextViewHeight <= 0) {
			view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			mTextViewHeight = view.getMeasuredHeight();
		}
		return mTextViewHeight;
		
	}
	
	public void addEvent(Event event) {
		addEventName(event.getName());
	}
	
	public void addEventName(String name) {
		TextView textView = new TextView(getContext());
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mDataTextSize);
		textView.setPadding(mPadding, 0, 0, 0);
		textView.setEllipsize(TextUtils.TruncateAt.END);
		textView.setSingleLine();
		textView.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
		textView.setText(name);
		mCurrHeight -= getTextViewHeight(textView);
		if (mAvailableHeight > 0 && mCurrHeight > mAvailableHeight) {
			return;
		}
		addView(textView);
	}
	

	/**
	 * 
	 * @return date that is displayed by view.
	 */
	public Date getDate() {
		return mDisplayedDate;
	}
	
}
