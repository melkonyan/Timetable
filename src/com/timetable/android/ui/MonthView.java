package com.timetable.android.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;
import com.timetable.android.utils.DateUtils;

public class MonthView extends LinearLayout {

	GridView mGreedView;
	
	LayoutInflater mLayoutInflater;
	
	Context mContext;
	

	/**
	 * Create MonthView
	 * @date - month to show.
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
	
		private final int[] COLORS = { 0xFFC0C0C0, 0xFF98FF98, 0xFF00FF00, 0xFF008000, 0x006400 }; 
		
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
		
		/**
		 * Create MonthViewAdapter 
		 * @date - date, that contains the month of events that adapter will provide.
		 */
		public MonthViewAdapter(Context context, Date date) {
			TimetableLogger.log("Creating MonthViewAdapter.");
			mContext = context;
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			setDisplayedMonth(cal);
			new EventsLoader().execute();
		}
		
		public void setDisplayedMonth(Calendar month) {
			mDisplayedMonth = month;
			Calendar firstDisplayedDate = (Calendar) month.clone();
			firstDisplayedDate.set(Calendar.DAY_OF_MONTH, 1);
			firstDisplayedDate.add(Calendar.DATE, -firstDisplayedDate.get(Calendar.DAY_OF_WEEK) + 1);
			mFirstDisplayedDate = firstDisplayedDate.getTime(); 
			//TimetableLogger.error(mDisplayedMonth.getTime().toString() + " " + mFirstDisplayedDate.toString());
			int firstDisplayedDay = firstDisplayedDate.get(Calendar.DAY_OF_MONTH);
			int currentlyDisplayedDay = firstDisplayedDay;
			boolean showPreviousMonth = firstDisplayedDate.get(Calendar.MONTH) != month.get(Calendar.MONTH);
			boolean showingPreviousMonth = showPreviousMonth;
			boolean showingCurrentMonth = !showingPreviousMonth;
			int previousMonthDaysCount = firstDisplayedDate.getActualMaximum(Calendar.DAY_OF_MONTH);
			int currentMonthDaysCount = month.getActualMaximum(Calendar.DAY_OF_MONTH);
			//TimetableLogger.error(Boolean.toString(showPreviousMonth));
			//TimetableLogger.error(Boolean.toString(showingPreviousMonth));
			//TimetableLogger.error(Integer.toString(previousMonthDaysCount));
			//TimetableLogger.error(Integer.toString(currentlyDisplayedDay));
			//TimetableLogger.error(Integer.toString(currentMonthDaysCount));
			
			//TimetableLogger.error(firstDisplayedDate.getTime() + " " + month.getTime());
			for (int i = 0; i < mGreedSize; i++) {
				mDisplayedDays[i] = currentlyDisplayedDay;
				mIsCurrentMonth[i] = showingCurrentMonth;

				//TimetableLogger.error(Integer.toString(currentlyDisplayedDay) + " " + Boolean.toString(mIsCurrentMonth[i]));
				currentlyDisplayedDay++;
				if (showingPreviousMonth && currentlyDisplayedDay > previousMonthDaysCount) {
					//TimetableLogger.error("Showing current month");
					showingPreviousMonth = false;
					showingCurrentMonth = true;
					currentlyDisplayedDay = 1;
				} else if (showingCurrentMonth && currentlyDisplayedDay > currentMonthDaysCount) {
					currentlyDisplayedDay = 1;
					showingCurrentMonth = false;
					//TimetableLogger.error("Showing next month");
				}
			}
			
			//TimetableLogger.error(Arrays.toString(mIsCurrentMonth));
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
				
				//TimetableLogger.log("Month: " + (mDisplayedMonth.get(Calendar.MONTH)+1) + " Cell # " + i + " Date: " + currentDate + "Events #: " + mEventCounts[i] );
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
			textView.setBackgroundColor(getCellColor(position));
			
			return textView;
		}
		
		private int getCellColor(int position) {
			int num = mEventCounts[position];
			int colorPos = 0;
			if (num >= 6) {
				colorPos = 4;
			} else if (num >= 3) {
				colorPos = 3;
			} else if (num >= 1) {
				colorPos = 2;
			} else if (num >= 0) {
				colorPos = 1;
			}
			return COLORS[colorPos];
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
		
		private class EventsLoader extends AsyncTask<Void, Void, Void> {

			@Override
			protected Void doInBackground(Void... arg0) {
				MonthViewAdapter.this.calcEventsArray();
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				MonthViewAdapter.this.notifyDataSetChanged();
			}

			
		}
	}
}