package com.timetable.android.ui;

import java.util.Date;
import java.util.Vector;

import org.holoeverywhere.LayoutInflater;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.timetable.android.Event;
import com.timetable.android.R;
import com.timetable.android.TimetableDatabase;
import com.timetable.android.TimetableLogger;

/**
 * Class for paging events. 
 */
public abstract class EventPager extends ViewPager {

	private Context mContext;
	
	private  Date mInitDate;
	
	public static final int INIT_PAGE_NUMBER = 1000;
	
	private EventPagerAdapter mEventPagerAdapter;

	/**
	 * Construct EventPager using given context, onPageChangeListener.  
	 */
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

	/**
	 * Need to be called, to set start page, which shows initDate. 
	 */
	public void prepare() {
		setCurrentItem(INIT_PAGE_NUMBER);
	}

	protected EventPagerAdapter getEventPagerAdapter() {
		return mEventPagerAdapter;
	}

	protected void setEventPagerAdapter(EventPagerAdapter eventPagerAdapter) {
		mEventPagerAdapter = eventPagerAdapter;
	}

	protected Date getInitDate() {
		return mInitDate;
	}

	protected void setInitDate(Date initDate) {
		mInitDate = initDate;
	}

	/**
	 * Get currently displayed date.
	 */
	public Date getDisplayedDate() {
		return getDateByPageNumber(getCurrentItem());
	}
	
	/**
	 * Get date, that is displayed on given page.
	 */
	public abstract Date getDateByPageNumber(int pageNumber);

	/**
	 * Get number of page, that should display given date.
	 */
	protected abstract int getPageNumberByDate(Date date);
	
	/**
	 * Update content of EventPager. 
	 */
	public void update() {
		mEventPagerAdapter.update();
		mEventPagerAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Adapter, that provides data for EventPager.
	 */
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
		
		@Override 
		public Object instantiateItem(View viewPager, int pageNumber) {
			return null;
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