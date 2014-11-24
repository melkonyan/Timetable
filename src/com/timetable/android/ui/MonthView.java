package com.timetable.android.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.LinearLayout;

import android.content.Context;
import android.os.AsyncTask;
import android.view.MotionEvent;
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

	Date mInitDate; 
	
	GridView mGridView;
	
	LayoutInflater mLayoutInflater;
	
	IMonthViewObserver mObserver;
	
	onDayClickedListener mListener;
	
	
	/**
	 * Create MonthView
	 * @date - month to show.
	 */
	public MonthView(Context context, Date date, IMonthViewObserver observer) {
		super(context);
		mObserver = observer;
		mInitDate = date;
		mListener = new onDayClickedListener();
		mLayoutInflater = LayoutInflater.from(context);
		mLayoutInflater.inflate(R.layout.layout_month_view, this, true);
		mGridView = (GridView) findViewById(R.id.month_days);
		mGridView.setAdapter(new MonthViewAdapter(context, date));
		//Prevent mGridVeiw from scrolling 
		mGridView.setOnTouchListener(new OnTouchListener(){

		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_MOVE){
		            return true;
		        }
		        return false;
		    }

		});
	}

	public static interface IMonthViewObserver {

		/**
		 * Called by {@link:MonthView}, when user selects day to show. 
		 * @param date - date to show.
		 */
		public void onDateSelected(Date date);
	}
	
	class onDayClickedListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Date selectedDate = ((MonthCellView) v).getDate();
			TimetableLogger.error("onDayClickedListener: view clicked on date " + selectedDate);
			TimetableLogger.error("Actual cell width: " + v.getWidth());
			if (selectedDate == null) {
				TimetableLogger.error("MonthView.onDayClickedListener.onClick(): selected date is null");
				selectedDate = mInitDate;
			}
			MonthView.this.mObserver.onDateSelected(selectedDate);
			
		}
		
	}
	
	class MonthViewAdapter extends BaseAdapter {
	
		private Context mContext;
		
		int mDayViewHeight = -1;
		
		int mDayViewWidth = -1;
		
		Calendar mDisplayedMonth;
		
		Date mFirstDisplayedDate;
		
		boolean mDataIsLoaded = false;
		
		//Number of displayed days
		static final int mGreedSize = 42;
		
		static final int mRowsNum = 6;
		
		static final int mColsNum = 7;
		
		int[] mDisplayedDays = new int[mGreedSize];
		
		boolean[] mIsCurrentMonth = new boolean[mGreedSize];
		
		//number of events displayed on each day of month.
		ArrayList<ArrayList<String>> mEvents;
		
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
		
		/**
		 * Calculate appropriate fields, according to month, that is shown.
		 * @param month - calendar, that contains month, that should be shown.
		 */
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
			for (int i = 0; i < mGreedSize; i++) {
				mDisplayedDays[i] = currentlyDisplayedDay;
				mIsCurrentMonth[i] = showingCurrentMonth;
				currentlyDisplayedDay++;
				if (showingPreviousMonth && currentlyDisplayedDay > previousMonthDaysCount) {
					showingPreviousMonth = false;
					showingCurrentMonth = true;
					currentlyDisplayedDay = 1;
				} else if (showingCurrentMonth && currentlyDisplayedDay > currentMonthDaysCount) {
					currentlyDisplayedDay = 1;
					showingCurrentMonth = false;
				}
			}
		}
		
		/**
		 * Calculate two-dimensional array of events, that should be shown on each day of month.
		 */
		private void calcEventsArray() {
			Vector<Event> events = TimetableDatabase.getInstance(mContext).getAllEvents();
			mEvents = new ArrayList<ArrayList<String>>(mGreedSize);
			Date currentDate = mFirstDisplayedDate;
			for (int i = 0; i < mGreedSize; i++) {
				mEvents.add(new ArrayList<String>());
				if (mIsCurrentMonth[i]) {
					for(Event event: events) {
						if (event.isToday(currentDate)) {
							mEvents.get(i).add(event.getName());
						}
					}
				}
				//TimetableLogger.log("Month: " + (mDisplayedMonth.get(Calendar.MONTH)+1) + " Cell # " + i + " Date: " + currentDate + "Events #: " + mEvents[i] );
				currentDate = DateUtils.addDay(currentDate, 1);
			}
		}
		
		private int getDayViewHeight() {
			if (mDayViewHeight <= 0) {
				int gridViewHeight = MonthView.this.mGridView.getHeight();
				mDayViewHeight = gridViewHeight / mRowsNum;
				if (gridViewHeight % mRowsNum > 0) {
					mDayViewHeight++;
				}
			}
			return mDayViewHeight;
		}
		
		private int getDayViewWidth() {
			if (mDayViewWidth <= 0) {
				int gridViewWidth =  MonthView.this.mGridView.getWidth();
				TimetableLogger.error("Grid width: "+ gridViewWidth);
				
				mDayViewWidth = gridViewWidth / mColsNum;
				if (gridViewWidth % mColsNum > 0) {
					mDayViewWidth++;
					TimetableLogger.error("add on pixel");
				}
				TimetableLogger.error("Cell width: "+ mDayViewWidth);
				
			}
			return mDayViewWidth;
		}
		
		Date getDateToDisplay(int position) {
			return DateUtils.addDay(mFirstDisplayedDate, position);
		}
		
		@Override
		public long getItemId(int position) {
			return 0;
		}
	
		@Override
		public View getView(int position, View convertView, ViewGroup Parent) {
			MonthCellView view = new MonthCellView(mContext, mDisplayedDays[position], getDateToDisplay(position), getDayViewHeight(), getDayViewWidth());
			view.setMinimumWidth(50);
			view.setOnClickListener(MonthView.this.mListener);
			if (!mDataIsLoaded) {
				return view;
			}
			//TimetableLogger.error(""+mEvents.size());
			for (String name: mEvents.get(position)) {
				view.addEventName(name);
			}
			return view;
		
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
				mDataIsLoaded = true;
				MonthViewAdapter.this.notifyDataSetChanged();
			}

			
		}
	}
}