package com.timetable.android.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;
import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.Utils;

public class MonthView extends LinearLayout {

	GridView mGreedView;
	
	LayoutInflater mLayoutInflater;
	
	Context mContext;
	/*
	 * Create MonthView
	 * date - month of events to show.
	 */
	public MonthView(Context context, Date date) {
		super(context);
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mLayoutInflater.inflate(R.layout.layout_month_view, this, true);
		mGreedView = (GridView) findViewById(R.id.month_days);
		mGreedView.setAdapter(new MonthViewAdapter(context, date));
	
	}

	public class MonthViewAdapter extends BaseAdapter {
	
		private Context mContext;
		
		int mDayViewHeight = -1;
		
		Calendar mDisplayedMonth;
		
		Date mFirstDisplayedDate;
		
		//Number of displayed days
		final int mGreedSize = 42;
		
		final int mRowsNum = 6;
		
		int[] mDisplayedDays = new int[mGreedSize];
		
		boolean[] mIsCurrentMonth = new boolean[mGreedSize];
		
		//number of events displayed on each day of month.
		int[] mEventCounts = new int[mGreedSize];
		
		/*
		 * Create MonthViewAdapter 
		 * date - date, that contains the month of events that adapter will provide.
		 */
		public MonthViewAdapter(Context context, Date date) {
			mContext = context;
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			setDisplayedMonth(cal);
			calcEventsArray();
		}
		
		public void setDisplayedMonth(Calendar month) {
			mDisplayedMonth = month;
			Calendar firstDisplayedDate = (Calendar) month.clone();
			firstDisplayedDate.set(Calendar.DAY_OF_MONTH, 1);
			firstDisplayedDate.add(Calendar.DATE, -firstDisplayedDate.get(Calendar.DAY_OF_WEEK) + 1);
			mFirstDisplayedDate = firstDisplayedDate.getTime(); 
			int firstDisplayedDay = firstDisplayedDate.get(Calendar.DAY_OF_MONTH);
			int currentlyDisplayedDay = firstDisplayedDay;
			boolean showPreviousMonth = firstDisplayedDate.get(Calendar.MONTH) != month.get(Calendar.MONTH);
			boolean showingPreviousMonth = showPreviousMonth;
			boolean showingCurrentMonth = !showingPreviousMonth;
			int previousMonthDaysCount = firstDisplayedDate.getActualMaximum(Calendar.DAY_OF_MONTH);
			int currentMonthDaysCount = month.getActualMaximum(Calendar.DAY_OF_MONTH);
			//TimetableLogger.error(firstDisplayedDate.getTime() + " " + month.getTime());
			for (int i = 0; i < mGreedSize; i++) {
				mDisplayedDays[i] = currentlyDisplayedDay;
				mIsCurrentMonth[i] = showingCurrentMonth;
				currentlyDisplayedDay++;
				if (showingPreviousMonth && currentlyDisplayedDay > previousMonthDaysCount) {
					showingPreviousMonth = false;
					showingCurrentMonth = true;
					currentlyDisplayedDay = 1;
				} else if (currentlyDisplayedDay > currentMonthDaysCount) {
					currentlyDisplayedDay = 1;
					showingCurrentMonth = false;
				}
			}
		}
		
		private void calcEventsArray() {
			Vector<Event> events = TimetableDatabase.getInstance(mContext).getAllEvents();
			Date currentDate = mFirstDisplayedDate;
			for (int i = 0; i < mGreedSize; i++) {
				if (!mIsCurrentMonth[i]) {
					mEventCounts[i] = -1;
					continue;
				}
				mEventCounts[i] = 0;
				for(Event event: events) {
					if (event.isToday(currentDate)) {
						mEventCounts[i]++;
					}
				}
				currentDate = DateUtils.addDay(currentDate, 1);
			}
		}
		
		private int getDayViewHeight() {
			if (mDayViewHeight <= 0) {
				mDayViewHeight = MonthView.this.mGreedView.getHeight() / mRowsNum;
			}
	
			return mDayViewHeight;
		}
		
		
		@Override
		public long getItemId(int position) {
			return 0;
		}
	
		@Override
		public View getView(int position, View convertView, ViewGroup Parent) {
			TextView textView = (TextView) convertView;
			if (textView == null ) {
				textView = (TextView) MonthView.this.mLayoutInflater.inflate(R.layout.month_day_view, null);
				if (getDayViewHeight() > 0) {
					textView.setHeight(getDayViewHeight());
				}
			}
			textView.setText(Integer.toString(mDisplayedDays[position]) + " " + Integer.toString(mEventCounts[position]));
			return textView;
		}
	
		@Override
		public int getCount() {
			return mGreedSize;
		}
	
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}