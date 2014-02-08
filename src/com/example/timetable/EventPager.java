package com.example.timetable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class EventPager extends ViewPager {

	private final Context context;
	
	private EventActionBar eventActionBar;
	
	private final SimpleDateFormat actionBarDateFormat = new SimpleDateFormat("EEE, dd.MM"); 
	
	private Date initDate; 
	
	private final int initPageNumber = 1000;
	
	EventPagerAdapter eventPagerAdapter;
	
	EventPagerListener eventPagerListener;
	
	EventScrollView eventScrollView;
	
	public EventPager(Context context, EventActionBar eventActionBar, Date initDate) {
		super(context);
		this.setId(1000);
		this.context = context;
		this.eventActionBar = eventActionBar;
		this.initDate = initDate;
		
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );	
		layoutInflater.inflate(R.layout.event_pager, this, true);
		this.eventPagerAdapter = new EventPagerAdapter(initDate);
		this.eventPagerListener = new EventPagerListener();
		this.setAdapter(eventPagerAdapter);
		this.setOnPageChangeListener(eventPagerListener);
		this.setCurrentItem(this.initPageNumber, false);
		
	}
	
	//return current displaying date
	public Date getDate() {
		return eventPagerListener.currentDate;
	}
	
	private Date getDateByPageNumber(int pageNumber) {
		Date date = new Date();
		date.setTime(EventPager.this.initDate.getTime() + (pageNumber-this.initPageNumber)*24*3600*1000);
		return date;
	}
	
	//updates pages when content has changed
	public void update() {
		eventPagerAdapter.notifyDataSetChanged();
	}
	
	private class EventPagerAdapter extends PagerAdapter{
	
		private Date currentDate;
		
		private int pageNumber = EventPager.this.initPageNumber;
		
		public EventPagerAdapter(Date currentDate) {
			TimetableLogger.log("EventPagerAdapter created");
			this.currentDate = currentDate;
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
			
			this.currentDate = EventPager.this.getDateByPageNumber(pageNumber);
			this.pageNumber = pageNumber;
			
			TimetableDatabase db = new TimetableDatabase(EventPager.this.context);
		
			LinearLayout externalLayout = new LinearLayout(context);
			externalLayout.setOrientation(LinearLayout.HORIZONTAL);
			ScrollView scrollView = new ScrollView(context);
			scrollView.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			scrollView.setFillViewport(true);
			
			LinearLayout internalLayout = new LinearLayout(context); 
			internalLayout.setOrientation(LinearLayout.VERTICAL);
			internalLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			
			List<Event> events = db.searchEventsByDate(currentDate);
			for (Event event: events) {
				EventView eventView = new EventView(EventPager.this.context, event);
				internalLayout.addView(eventView);
			}
			if (events.size() == 0) {
				TextView textView = new TextView(context);
				textView.setPadding(0,60,0,0);
				textView.setGravity(Gravity.CENTER_HORIZONTAL);
				textView.setText(R.string.event_pager_no_events);
				internalLayout.addView(textView);
			}
			
			scrollView.addView(internalLayout,0);
			externalLayout.addView(scrollView,0);
			
			((ViewPager) viewPager).addView(externalLayout,0);
			
			TimetableLogger.log("EventPagerAdapter displays page # "+ this.pageNumber + " " + new SimpleDateFormat("dd.MM.yyy").format(currentDate.getTime()));
			//logger.log("Events added to layout: " + internalLayout.getChildCount());
			db.close();
			return externalLayout;
		}
		
		//peace of code, that gets update function work
		public int getItemPosition(Object object){
		     return POSITION_NONE;
		}
		
		@Override
	    public void destroyItem(View viewPager, int pageNumber, Object view) {
	            TimetableLogger.log("EventPagerAdapter destroys page number " + pageNumber);
	            ((ViewPager) viewPager).removeView((View) view);
	    }
	}
	
	public class EventPagerListener extends SimpleOnPageChangeListener {
		
		private Date currentDate;
		
		public EventPagerListener() {
			super();
			TimetableLogger.log("EventPagerListener successfully created.");
		}
		
		@Override
		public void  onPageSelected(int pageNumber) {
			TimetableLogger.log("EventPagerListener detected page # " + pageNumber + " selection.");
			currentDate = EventPager.this.getDateByPageNumber(pageNumber);
			//update action bar
			eventActionBar.setTitle(actionBarDateFormat.format(currentDate));
			eventActionBar.showTitle(Page.EVENT_VIEW);
		}
	}

}
