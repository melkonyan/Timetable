package com.timetable.android.ui;




import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.LinearLayout;

import android.os.Bundle;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;

import com.timetable.android.EventPager;
import com.timetable.android.IEventViewer;
import com.timetable.android.IEventViewerContainer;
import com.timetable.android.MonthViewPager;
import com.timetable.android.R;
import com.timetable.android.TimetableLogger;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.Utils;

/**
 * Class for showing events, grouped by month.
 * Activity to which Fragment is attached should implement {@link IEventViewerConainer}
 */
public class MonthViewFragment extends Fragment implements IEventViewer {
	
	public static SimpleDateFormat ACTION_BAR_DATE_FORMAT = DateFormatFactory.getFormat("MMMMMM yyyy");
	
	public static String ARGUMENT_INIT_DATE = "init_date";
	Date mInitDate;
	
	Date mDisplayedDate;
	
	IEventViewerContainer mContainer;
	
	Activity mActivity;
	
	EventPagerListener mPagerListener;
	
	MonthViewPager mPager;
	
	LinearLayout mFragmentView;
	
	public MonthViewFragment() {
		super();
	}
	
	/**
	 * Create new instance of MonthViewFragment
	 * @param initDate - date to display.
	 */
	public static MonthViewFragment newInstance(Date initDate) {
		MonthViewFragment fragment = new MonthViewFragment();
		Bundle args = new Bundle();
		args.putLong(ARGUMENT_INIT_DATE, initDate.getTime());
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override 
	public void onAttach(Activity activity) {
		TimetableLogger.log("MonthViewFragment. Attaching fragment.");
		super.onAttach(activity);
		mActivity = activity;
		mContainer = (IEventViewerContainer) activity;
	}
	
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		TimetableLogger.log("MonthViewFragment. Creating fragment's view.");
		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		mInitDate = new Date(savedInstanceState.getLong(ARGUMENT_INIT_DATE));
		mPagerListener = new EventPagerListener();
		mFragmentView = (LinearLayout) inflater.inflate(R.layout.fragment_month_view, null);
		setEventPager(new MonthViewPager(getActivity(), this, mInitDate));
		return mFragmentView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		TimetableLogger.log("MonthViewFragment. Saving instance state.");
		super.onSaveInstanceState(outState);
		outState.putLong(ARGUMENT_INIT_DATE, mInitDate.getTime());
	}
	public SimpleOnPageChangeListener getEventPagerListener() {
		return mPagerListener;
	}

	public EventPager getEventPager() {
		return mPager;
	}
	
	public void setEventPager(MonthViewPager eventPager) {
		if (this.mPager != null) {
			mFragmentView.removeView(this.mPager);
		}
		this.mPager = eventPager;
		eventPager.prepare();
		mFragmentView.addView(eventPager,1);
	}

	@Override
	public void goToDate(Date date) {
		setEventPager(new MonthViewPager(getActivity(), this, date));
	}

	@Override
	public Date getDisplayedDate() {
		return mDisplayedDate;
	}

	@Override
	public void update() {
		getEventPager().update();
	}

	/*
	 * Listener, that is called, when user slides to other page.
	 */
	private class EventPagerListener extends SimpleOnPageChangeListener {
		
		
		public EventPagerListener() {
			super();
			//mCurrentYear = Utils.getCurrDateTimeCal().get(Calendar.YEAR);
			TimetableLogger.log("EventPagerListener successfully created.");
		}
		
		@Override
		public void  onPageSelected(int pageNumber) {
			mDisplayedDate = getEventPager().getDateByPageNumber(pageNumber);
			mContainer.setActionBarTitle(ACTION_BAR_DATE_FORMAT.format(mDisplayedDate));
			mContainer.setActionBarSubtitle("");
		}

		
	}



}
