package com.timetable.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.holoeverywhere.widget.TextView;

import ui.DayViewFragment;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

/*
 * Class, that instantiates views of events for each day and allows user to slide among the days.
 */
public class DayViewPager extends EventPager {

	private DayViewFragment mFragment;
	
	private EventViewProvider mEventViewProvider; 
	
	
	
	public DayViewPager(Context context, DayViewFragment fragment, Date initDate) {
		super(context, fragment.getEventPagerListener(), initDate);
		mFragment = fragment;
		mEventViewProvider = EventViewProvider.getInstance(context);
		setEventPagerAdapter(new DayViewPagerAdapter(initDate));
		setAdapter(getEventPagerAdapter());
		
	}
	
	@Override
	public Date getDateByPageNumber(int pageNumber) {
		Date date = new Date();
		date.setTime(getInitDate().getTime() + (long) (pageNumber-INIT_PAGE_NUMBER)*24*3600*1000);
		return date;
	}
	
	@Override
	protected int getPageNumberByDate(Date date) {
		long day = 24*3600*1000;
		return  INIT_PAGE_NUMBER + (int) ((date.getTime() - getInitDate().getTime()) / day);
	}
	
	class DayViewPagerAdapter extends EventPagerAdapter{
	
		public DayViewPagerAdapter(Date currentDate) {
			super(currentDate);
			TimetableLogger.log("MonthViewPagerAdapter created");
		}
		

		@Override
		public Object instantiateItem(View viewPager, int pageNumber) {
			TimetableLogger.verbose("DayViewPager: try instantiate page " + Integer.toString(pageNumber));
			Date currentDate = DayViewPager.this.getDateByPageNumber(pageNumber);
			
			LayoutInflater inflater = LayoutInflater.from(getContext());
			View externalContainer = inflater.inflate(R.layout.event_pager_container, null, true);
			
			LinearLayout internalContainer = (LinearLayout) externalContainer.findViewById(R.id.container);
			
			boolean hasEventsToday = false;
			
			for (Event event: events) {
				if (!event.isToday(currentDate)) {
					continue;
				}
				
				EventView eventView = mEventViewProvider.getView(pageNumber);
				eventView.populate(event, currentDate);
				eventView.setEventViewObserver(mFragment);
				internalContainer.addView(eventView);
				hasEventsToday = true;
			}
			if (!hasEventsToday) {
				TextView textView = new TextView(getContext());
				textView.setPadding(0,60,0,0);
				textView.setGravity(Gravity.CENTER_HORIZONTAL);
				textView.setText(R.string.event_pager_no_events);
				internalContainer.addView(textView);
			}
			
			((ViewPager) viewPager).addView(externalContainer,0);
			
			TimetableLogger.log("MonthViewPagerAdapter created page # "+ pageNumber + " " + new SimpleDateFormat("dd.MM.yyy").format(currentDate.getTime()));
			return externalContainer;
		}
		
		
		@Override
	    public void destroyItem(View viewPager, int pageNumber, Object view) {
			super.destroyItem(viewPager, pageNumber, view);
			mEventViewProvider.releaseViews(pageNumber);
		}
	}

	


}
