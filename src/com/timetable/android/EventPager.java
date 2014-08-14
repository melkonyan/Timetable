package com.timetable.android;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.widget.ScrollView;

import com.timetable.android.activities.EventDayViewActivity;

/*
 * Class, that instantiate views of events for each day, and allows user to slide among the days.
 */
public class EventPager extends ViewPager {

	private final EventDayViewActivity activity;

	private Date initDate; 
	
	private final static int INIT_PAGE_NUMBER = 1000;
	
	EventPagerAdapter eventPagerAdapter;
	
	
	public EventPager(EventDayViewActivity activity, Date initDate) {
		super(activity);
		setId(1000);
		this.activity = activity;
		this.initDate = initDate;
	
		LayoutInflater layoutInflater = LayoutInflater.from(activity);	
		layoutInflater.inflate(R.layout.event_pager, this, true);
		eventPagerAdapter = new EventPagerAdapter(initDate);
		setAdapter(eventPagerAdapter);
		setOnPageChangeListener(activity.getEventPagerListener());
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
		date.setTime(initDate.getTime() + (long) (pageNumber-INIT_PAGE_NUMBER)*24*3600*1000);
		return date;
	}
	
	private int getPageNumberByDate(Date date) {
		long day = 24*3600*1000;
		return  INIT_PAGE_NUMBER + (int) ((date.getTime() - initDate.getTime()) / day);
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
		eventPagerAdapter.notifyDataSetChanged();
	}
	
	private class EventPagerAdapter extends PagerAdapter{
	
		public EventPagerAdapter(Date currentDate) {
			TimetableLogger.log("EventPagerAdapter created");
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
			
			TimetableDatabase db = TimetableDatabase.getInstance(EventPager.this.activity);
		
			LinearLayout externalLayout = new LinearLayout(activity);
			externalLayout.setOrientation(LinearLayout.HORIZONTAL);
			ScrollView scrollView = new ScrollView(activity);
			scrollView.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
			scrollView.setFillViewport(true);
			
			LinearLayout internalLayout = new LinearLayout(activity); 
			internalLayout.setOrientation(LinearLayout.VERTICAL);
			internalLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			
			List<Event> events = db.searchEventsByDate(currentDate);
			for (Event event: events) {
				EventView eventView = new EventView(EventPager.this.activity, event);
				internalLayout.addView(eventView);
			}
			if (events.size() == 0) {
				TextView textView = new TextView(activity);
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
	    }
	}

}
