package com.timetable.android;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import ui.DayViewFragment;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.timetable.android.EventView.EventViewObserver;

/*
 * Class, that instantiate views of events for each day, and allows user to slide among the days.
 */
public class EventPager extends ViewPager {

	private Context mContext;
	
	private DayViewFragment mFragment;
	
	private Date mInitDate; 
	
	private final static int INIT_PAGE_NUMBER = 1000;
	
	private EventPagerAdapter mEventPagerAdapter;
	
	private EventViewProvider mEventViewProvider; 
	
	public EventPager(Context context, DayViewFragment fragment, Date initDate) {
		super(context);
		setId(1000);
		mContext = context;
		mInitDate = initDate;
		mFragment = fragment;
		
		setOffscreenPageLimit(0);
		LayoutInflater layoutInflater = LayoutInflater.from(context);	
		layoutInflater.inflate(R.layout.event_pager, this, true);
		mEventViewProvider = EventViewProvider.getInstance(mContext);
		mEventPagerAdapter = new EventPagerAdapter(initDate);
		setAdapter(mEventPagerAdapter);
		setOnPageChangeListener(mFragment.getEventPagerListener());
		
	}
	
	/*
	 * Should be could after EventPager is created to display default date. 
	 */
	public void prepare() {
		setCurrentItem(INIT_PAGE_NUMBER);
	}
	
	/*
	 * Return currently displayed date.
	 */
	public Date getDisplayedDate() {
		return getDateByPageNumber(getCurrentItem());
	}
	
	public Date getDateByPageNumber(int pageNumber) {
		Date date = new Date();
		date.setTime(mInitDate.getTime() + (long) (pageNumber-INIT_PAGE_NUMBER)*24*3600*1000);
		return date;
	}
	
	private int getPageNumberByDate(Date date) {
		long day = 24*3600*1000;
		return  INIT_PAGE_NUMBER + (int) ((date.getTime() - mInitDate.getTime()) / day);
	}
	
	/*
	 * Display specified date.
	 */
	public void goToDate(Date date) {
		//TimetableLogger.error("EventPager.goToDate: go to page: " + getPageNumberByDate(date));
		setCurrentItem(getPageNumberByDate(date), false);
	}
	
	/*
	 * Update pages when content has changed.
	 */
	public void update() {
		mEventPagerAdapter.update();
		mEventPagerAdapter.notifyDataSetChanged();
	}
	
	private class EventPagerAdapter extends PagerAdapter{
	
		//save all events from database into adaptor
		private Vector<Event> events = new Vector<Event>();
		
		private TimetableDatabase db;
		
		public EventPagerAdapter(Date currentDate) {
			TimetableLogger.log("EventPagerAdapter created");
			db = TimetableDatabase.getInstance(mContext);
			loadEvents();
		}
		
		public void update() {
			loadEvents();
		}
		
		private void loadEvents() {
			events = db.getAllEvents();
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
			TimetableLogger.verbose("EventPager: try instantiate page " + Integer.toString(pageNumber));
			Date currentDate = EventPager.this.getDateByPageNumber(pageNumber);
			
			LinearLayout externalLayout = new LinearLayout(mContext);
			externalLayout.setOrientation(LinearLayout.HORIZONTAL);
			ScrollView scrollView = new ScrollView(mContext);
			scrollView.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
			scrollView.setFillViewport(true);
			
			LinearLayout internalLayout = new LinearLayout(mContext); 
			internalLayout.setOrientation(LinearLayout.VERTICAL);
			internalLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			
			boolean hasEventsToday = false;
			
			for (Event event: events) {
				if (!event.isToday(currentDate)) {
					continue;
				}
				
				EventView eventView = mEventViewProvider.getView(pageNumber);
				eventView.populate(event, currentDate);
				eventView.setEventViewObserver(mFragment);
				eventView.setScrollView(scrollView);
				internalLayout.addView(eventView);
				hasEventsToday = true;
			}
			if (!hasEventsToday) {
				TextView textView = new TextView(mContext);
				textView.setPadding(0,60,0,0);
				textView.setGravity(Gravity.CENTER_HORIZONTAL);
				textView.setText(R.string.event_pager_no_events);
				internalLayout.addView(textView);
			}
			
			scrollView.addView(internalLayout,0);
			externalLayout.addView(scrollView,0);
			
			((ViewPager) viewPager).addView(externalLayout,0);
			
			TimetableLogger.log("EventPagerAdapter created page # "+ pageNumber + " " + new SimpleDateFormat("dd.MM.yyy").format(currentDate.getTime()));
			//logger.log("Events added to layout: " + internalLayout.getChildCount());
			return externalLayout;
		}
		
		//peace of code, that gets update function work
		public int getItemPosition(Object object){
		     return POSITION_NONE;
		}
		
		@Override
	    public void destroyItem(View viewPager, int pageNumber, Object view) {
	            TimetableLogger.verbose("EventPagerAdapter destroys page number " + pageNumber);
	            ((ViewPager) viewPager).removeView((View) view);
	            mEventViewProvider.releaseViews(pageNumber);
		}
	}

}
