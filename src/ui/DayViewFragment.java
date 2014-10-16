package ui;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.LinearLayout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;

import com.timetable.android.EventController;
import com.timetable.android.EventController.OnEventDeletedListener;
import com.timetable.android.EventPager;
import com.timetable.android.EventView;
import com.timetable.android.EventView.EventViewObserver;
import com.timetable.android.IEventViewer;
import com.timetable.android.IEventViewerContainer;
import com.timetable.android.R;
import com.timetable.android.TimetableLogger;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.DateUtils;
import com.timetable.android.utils.Utils;


/*
 * Fragment that displays all events for certain day.
 * User can view events for next or previous day by shifting page right or left.
 */
public class DayViewFragment extends Fragment implements EventViewObserver, OnEventDeletedListener, IEventViewer {
	
	//Activity, to which fragment is attached.
	private Activity mActivity;
	
	//Container, that controls fragment.
	private IEventViewerContainer mContainer;
	
	//Currently displayed date.
	private Date mDisplayedDate;
	
	private LinearLayout eventLayout;
	
	private EventPager eventPager;
	
	private EventPagerListener mListener = new EventPagerListener();
	
	//Date, which fragment should display initially.
	private Date mInitDate;
	
	public static final SimpleDateFormat ACTION_BAR_DATE_FORMAT = DateFormatFactory.getFormat("EEE, dd.MM"); 
	
	public static final SimpleDateFormat ACTION_BAR_DATE_FORMAT_WITH_YEAR = DateFormatFactory.getFormat("EEE, dd.MM.yyyy");
	
	//Event view, that shows it's menu.
	private EventView mSelectedEventView;
	
	public DayViewFragment(IEventViewerContainer container, Date initDate) {
		mContainer = container;
		mInitDate = initDate;
	}
	
	public EventPager getEventPager() {
		return eventPager;
	}
	
	public void setEventPager(EventPager eventPager) {
		if (this.eventPager != null) {
			eventLayout.removeView(this.eventPager);
		}
		
		this.eventPager = eventPager;
		this.eventPager.prepare();
		eventLayout.addView(eventPager,0);
	}

	public EventPagerListener getEventPagerListener() {
		return mListener;
	}
	
	
	@Override 
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		TimetableLogger.log("DayViewFragment. Fragment attaches to activity.");
		mActivity = activity;
	}
	
	
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		TimetableLogger.log("DayViewFragment. Fragment has creates it's view.");
		View fragmentView = inflater.inflate(R.layout.activity_event_day_view);
		eventLayout = (LinearLayout) fragmentView.findViewById(R.id.events_table);
		setEventPager(new EventPager(mActivity, this, mInitDate));
		return fragmentView;
	}
	
	@Override 
	public void onPause() {
		super.onPause();
		TimetableLogger.log("DayViewFragment. Fragment is being paused.");
		hideOpenedMenu();
	}

	@Override
	public void goToDate(Date date) {
		setEventPager(new EventPager(mActivity, this, date));
    }

	@Override
	public Date getDisplayedDate() {
		return mDisplayedDate;
	}
	
	/*
	 * Update content of fragment.
	 */
	@Override
	public void update() {
		getEventPager().update();
	}

	
	/*
	 * Hide menu of currently selected EventView
	 */
	private void hideOpenedMenu() {
		if (mSelectedEventView != null) {
			mSelectedEventView.hideMenu();
			mSelectedEventView = null;
		}
	}
	
	@Override
	public void onEventViewClicked(EventView eventView) {
		if (mSelectedEventView == eventView) {
			eventView.hideMenu();
			mSelectedEventView = null;
		} else {
			hideOpenedMenu();
			eventView.showMenu();
			mSelectedEventView = eventView;
		}
	}

	@Override
	public void onButtonDeleteClicked(EventView eventView) {
		EventController eventController = new EventController(mActivity);
		eventController.setOnEventDeletedListener(this);
		eventController.deleteEvent(eventView.getEvent(), eventPager.getDisplayedDate());
	}

	@Override
	public void onEventDeleted() {
		getEventPager().update();
	}

	@Override
	public void onButtonEditClicked(EventView eventView) {
		Intent eventEditIntent = new Intent(mActivity, EventEditActivity.class);
		eventEditIntent.putExtra(EventEditActivity.EXTRA_EVENT_ID, eventView.getEvent().getId());
		//TODO: put date's millis into extra, instead of formatting and then parsing date string 
		eventEditIntent.putExtra(EventEditActivity.EXTRA_DATE, EventEditActivity.INIT_DATE_FORMAT.format(getEventPager().getDisplayedDate()));
		startActivity(eventEditIntent);
	}

	@Override
	public void onButtonCopyClicked(EventView eventView) {
		Intent eventCopyIntent = new Intent(mActivity, EventCopyActivity.class);
		eventCopyIntent.putExtra(EventCopyActivity.EXTRA_COPY_EVENT, eventView.getEvent().convert());
		//TODO: put date's millis into extra, instead of formatting and then parsing date string 
		eventCopyIntent.putExtra(EventCopyActivity.EXTRA_DATE, EventCopyActivity.INIT_DATE_FORMAT.format(getEventPager().getDisplayedDate()));
		this.startActivity(eventCopyIntent);
	}

	@Override
	public void onEventViewLongClicked(EventView eventView) {
		onButtonEditClicked(eventView);
	}

	/*
	 * Listener, that is called, when user slides to other page.
	 */
	private class EventPagerListener extends SimpleOnPageChangeListener {
		
		private int mCurrentYear;
		
		public EventPagerListener() {
			super();
			mCurrentYear = Utils.getCurrDateTimeCal().get(Calendar.YEAR);
			
			TimetableLogger.log("EventPagerListener successfully created.");
		}
		
		@Override
		public void  onPageSelected(int pageNumber) {
			TimetableLogger.log("EventPagerListener detected page # " + pageNumber + " selection.");
			mDisplayedDate = getEventPager().getDateByPageNumber(pageNumber);
			
			//update action bar
			
			updateActionBarTitile();
			hideOpenedMenu();
		}

		private void updateActionBarTitile() {
			String titleString;
			Calendar cal = Calendar.getInstance();
			cal.setTime(mDisplayedDate);
			int displayedYear = cal.get(Calendar.YEAR);
			if (displayedYear == mCurrentYear) {
				titleString = ACTION_BAR_DATE_FORMAT.format(mDisplayedDate);
			} else {
				titleString = ACTION_BAR_DATE_FORMAT_WITH_YEAR.format(mDisplayedDate);
			}
			
			String subtitleString = null;
			if (DateUtils.areSameDates(mDisplayedDate, Utils.getCurrDateTime())) {
				subtitleString = getResources().getString(R.string.actionbar_date_today);
			}
			mContainer.setActionBarTitle(titleString);
			mContainer.setActionBarSubTitle(subtitleString);
		}
	}


}