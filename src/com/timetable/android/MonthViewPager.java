package com.timetable.android;

import java.util.Calendar;
import java.util.Date;

import ui.MonthView;
import ui.MonthViewFragment;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;

public class MonthViewPager extends EventPager {

	Calendar mInitDate;
	
	MonthViewFragment mFragment;
	
	public MonthViewPager(Context context, MonthViewFragment fragment, Date initDate) {
		super(context, fragment.getEventPagerListener(), initDate);
		mFragment = fragment;
		mInitDate = Calendar.getInstance();
		mInitDate.setTime(initDate);
		setEventPagerAdapter(new MonthViewPagerAdapter(initDate));
		setAdapter(getEventPagerAdapter());
	}

	@Override
	public Date getDateByPageNumber(int pageNumber) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(mInitDate.getTime());
		cal.add(Calendar.MONTH, pageNumber - INIT_PAGE_NUMBER);
		return cal.getTime();
	}

	@Override
	protected int getPageNumberByDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return (cal.get(Calendar.YEAR) - mInitDate.get(Calendar.YEAR)) * 12 + cal.get(Calendar.MONTH) - mInitDate.get(Calendar.MONTH) + INIT_PAGE_NUMBER;
	}
	
	class MonthViewPagerAdapter extends EventPagerAdapter{
		
		public MonthViewPagerAdapter(Date currentDate) {
			super(currentDate);
			TimetableLogger.log("MonthViewPagerAdapter created");
		}
		

		@Override
		public Object instantiateItem(View viewPager, int pageNumber) {
			MonthView view = new MonthView(mFragment.getActivity());
			((ViewPager) viewPager).addView(view, 0);
			
			return view;
		}
		
		
		@Override
	    public void destroyItem(View viewPager, int pageNumber, Object view) {
			super.destroyItem(viewPager, pageNumber, view);
		}
	}

}
