package com.timetable.android;

import java.util.Date;
import java.util.Vector;

import org.holoeverywhere.LayoutInflater;

import com.timetable.android.DayViewPager.DayViewPagerAdapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public abstract class EventPager extends ViewPager {

	protected Context mContext;
	
	protected Date mInitDate;
	
	public static final int INIT_PAGE_NUMBER = 1000;
	
	protected EventPagerAdapter mDayViewPagerAdapter;

	public EventPager(Context context, OnPageChangeListener onPageChangeListener, Date initDate) {
		super(context);
		mContext = context;
		mInitDate = initDate;
		setId(1000);
		setOffscreenPageLimit(1);
		LayoutInflater layoutInflater = LayoutInflater.from(context);	
		layoutInflater.inflate(R.layout.event_pager, this, true);
		setOnPageChangeListener(onPageChangeListener);
	}

	public void prepare() {
		setCurrentItem(INIT_PAGE_NUMBER);
	}

	public Date getDisplayedDate() {
		return getDateByPageNumber(getCurrentItem());
	}

	public abstract Date getDateByPageNumber(int pageNumber);

	protected abstract int getPageNumberByDate(Date date);
	
	public void goToDate(Date date) {
		TimetableLogger.error("DayViewPager.goToDate: go to page: " + getPageNumberByDate(date));
		setCurrentItem(getPageNumberByDate(date), false);
	}

	public void update() {
		mDayViewPagerAdapter.notifyDataSetChanged();
	}
	
	public abstract class EventPagerAdapter extends PagerAdapter {
		
		//save all events from database into adaptor
		protected Vector<Event> events = new Vector<Event>();
		
		protected TimetableDatabase db;
		
		public EventPagerAdapter(Date currentDate) {
			db = TimetableDatabase.getInstance(mContext);
			loadEvents();
		}
		
		@Override
		public int getCount() {
			return Integer.MAX_VALUE;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((View) object);
		}
		
		public void update() {
			loadEvents();
		}
		
		private void loadEvents() {
			events = db.getAllEvents();
		}
		
		//peace of code, that gets update function work
		public int getItemPosition(Object object){
			return POSITION_NONE;
		}
				
		@Override
	    public void destroyItem(View viewPager, int pageNumber, Object view) {
            TimetableLogger.verbose("EventPagerAdapter destroys page number " + pageNumber);
            ((ViewPager) viewPager).removeView((View) view);
        }
	
	}
}