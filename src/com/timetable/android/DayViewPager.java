package com.timetable.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import ui.DayViewFragment;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/*
 * Class, that instantiates views of events for each day and allows user to slide among the days.
 */
public class DayViewPager extends EventPager {

	private DayViewFragment mFragment;
	
	private EventViewProvider mEventViewProvider; 
	
	
	
	public DayViewPager(Context context, DayViewFragment fragment, Date initDate) {
		super(context, fragment.getEventPagerListener(), initDate);
		mFragment = fragment;
		
		mEventViewProvider = EventViewProvider.getInstance(mContext);
		mDayViewPagerAdapter = new DayViewPagerAdapter(initDate);
		setAdapter(mDayViewPagerAdapter);
		
	}
	
	@Override
	public Date getDateByPageNumber(int pageNumber) {
		Date date = new Date();
		date.setTime(mInitDate.getTime() + (long) (pageNumber-INIT_PAGE_NUMBER)*24*3600*1000);
		return date;
	}
	
	@Override
	protected int getPageNumberByDate(Date date) {
		long day = 24*3600*1000;
		return  INIT_PAGE_NUMBER + (int) ((date.getTime() - mInitDate.getTime()) / day);
	}
	
	class DayViewPagerAdapter extends EventPagerAdapter{
	
		public DayViewPagerAdapter(Date currentDate) {
			super(currentDate);
			TimetableLogger.log("DayViewPagerAdapter created");
		}
		

		@Override
		public Object instantiateItem(View viewPager, int pageNumber) {
			TimetableLogger.verbose("DayViewPager: try instantiate page " + Integer.toString(pageNumber));
			Date currentDate = DayViewPager.this.getDateByPageNumber(pageNumber);
			
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
			
			TimetableLogger.log("DayViewPagerAdapter created page # "+ pageNumber + " " + new SimpleDateFormat("dd.MM.yyy").format(currentDate.getTime()));
			//logger.log("Events added to layout: " + internalLayout.getChildCount());
			return externalLayout;
		}
		
		
		@Override
	    public void destroyItem(View viewPager, int pageNumber, Object view) {
			super.destroyItem(viewPager, pageNumber, view);
			mEventViewProvider.releaseViews(pageNumber);
		}
	}

	


}
