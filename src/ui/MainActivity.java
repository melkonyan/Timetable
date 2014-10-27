package ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.datetimepicker.date.DatePickerDialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;

import com.timetable.android.IEventViewer;
import com.timetable.android.IEventViewerContainer;
import com.timetable.android.R;
import com.timetable.android.TimetableLogger;
import com.timetable.android.utils.DateFormatFactory;
import com.timetable.android.utils.Utils;

public class MainActivity extends Activity implements IEventViewerContainer, OnNavigationListener {
	 
	
	
	private IEventViewer mEventViewer;
	
	public static final int EVENT_ADD_ACTIVITY_REQUEST_CODE = 10001;
	
	private DatePickerDialog mDatePickerDialog;
	
	private ActionBar mActionBar;
	
	public static final String EXTRAS_DATE = "date";
	
	public static final SimpleDateFormat EXTRAS_DATE_FORMAT = DateFormatFactory.getDateFormat();
	
	private Date mInitDate;
	
	private NavigationAdapter mNavigationAdapter;
	
	int mCurrentViewMode = NAVIGATION_DAY_VIEW;
	
	private final static int NAVIGATION_DAY_VIEW = 0;
	
	private final static int NAVIGATION_MONTH_VIEW = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mActionBar = getSupportActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);
		mNavigationAdapter = new NavigationAdapter(getSupportActionBar().getThemedContext());
		
		mActionBar.setListNavigationCallbacks(mNavigationAdapter, this);
		setInitDate();
		DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePickerDialog dialog, int year,
					int monthOfYear, int dayOfMonth) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				cal.set(Calendar.MONTH, monthOfYear);
				mEventViewer.goToDate(cal.getTime());
			}
			
		};
		
		mDatePickerDialog = DatePickerDialog.newInstance(mOnDateSetListener, 0, 0, 0);
		DayViewFragment fragment = new DayViewFragment(this, mInitDate);
		mEventViewer = fragment;
		//MonthViewFragment fragment = new MonthViewFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.main_container, fragment);
		fragmentTransaction.commit();
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		TimetableLogger.log("MainActivity was restarted.");
		mEventViewer.update();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_event_view, menu);
		return true;
	}
	
	@Override
	public boolean onNavigationItemSelected(int position, long itemId) {
		if (position == mCurrentViewMode) {
			return true;
		}
		Fragment fragment;
		
		switch (position) {
			case NAVIGATION_DAY_VIEW:
				fragment = new DayViewFragment(this, mEventViewer.getDisplayedDate());
				break;
			case NAVIGATION_MONTH_VIEW:
				fragment = new MonthViewFragment();
				break;
			default:
				return false;
		}
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main_container, fragment);
		fragmentTransaction.commit();
		mCurrentViewMode = position;
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_add_event:
	            Intent eventAddIntent = new Intent(this, EventAddActivity.class);
	            eventAddIntent.putExtra(EventAddActivity.EXTRA_DATE, EventAddActivity.INIT_DATE_FORMAT.format(mEventViewer.getDisplayedDate()));
	            startActivityForResult(eventAddIntent, EVENT_ADD_ACTIVITY_REQUEST_CODE);
	        	return true;
	        case R.id.action_view_today:
	        	mEventViewer.goToDate(Utils.getCurrDate());
	        	//eventPager.goToDate(Utils.getCurrentTime());
	        	return true;
	        case R.id.action_go_to_date:
	        	Calendar currDate = Calendar.getInstance();
	        	currDate.setTime(mEventViewer.getDisplayedDate());
	        	mDatePickerDialog.setDate(currDate.get(Calendar.YEAR), currDate.get(Calendar.MONTH), currDate.get(Calendar.DAY_OF_MONTH));
	        	mDatePickerDialog.show(getSupportFragmentManager());
	        	return true;
	        case R.id.action_month_view:
	        	return true;
	        case R.id.action_settings:
	        	startActivity(new Intent(this, SettingsActivity.class));
	        
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/*
	 * Set init date from intent of current date.
	 */
	private void setInitDate() {
		mInitDate = getInitDateFromIntent();
		if (mInitDate == null) {
			mInitDate = Utils.getCurrDate();
		}
	}
	
	/*
	 * Try to get init date from intent.
	 */
	private Date getInitDateFromIntent() {
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return null;
		}
		String dateString = extras.getString(EXTRAS_DATE);
		if (dateString == null) {
			return null;
		}
		try {
			return EXTRAS_DATE_FORMAT.parse(dateString);
		} catch (ParseException e) {
			TimetableLogger.error("DayViewFragment.getExtraDate: could not parse date");
			return null;
		}
	}
	
	@Override 
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		TimetableLogger.log("EventDayViewAvtivity: new intent received!");
		setIntent(intent);
		setInitDate();
		mEventViewer.goToDate(mInitDate);
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		TimetableLogger.sendReport();
	}
	
	
	/*
	 * Method that is called, when EventAddActivity adds event to database. 
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EVENT_ADD_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			long dateMillis = data.getLongExtra(EXTRAS_DATE, -1);
			if (dateMillis == -1) {
				TimetableLogger.error("DayViewFragment.onActivityResult: Invalid date returned.");
				return;
			}
			Date date = new Date(dateMillis);
			mEventViewer.goToDate(date);
		}
	}

	@Override
	public void setActionBarTitle(String title) {
		mNavigationAdapter.setTitle(title);
	}

	@Override
	public void setActionBarSubtitle(String subtitle) {
		mNavigationAdapter.setSubtitle(subtitle);
	}
	
	/*
	 * Adapter for navigation spinner of the action bar.
	 */
	public class NavigationAdapter extends BaseAdapter {
		 
		private String mTitle;
		private String mSubtitle;
		
		private ArrayList<String> mSpinnerItems;
	    private Context mContext;
	 
	    public NavigationAdapter(Context context) {
	        mContext = context;
	        mSpinnerItems = new ArrayList<String>(Arrays.asList(mContext.getResources().getStringArray(R.array.view_modes)));
	    }
	 
	    @Override
	    public int getCount() {
	    	return mSpinnerItems.size();
	    }
	 
	    @Override
	    public Object getItem(int index) {
	        return mSpinnerItems.get(index);
	    }
	 
	    @Override
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) { 
	    	if (convertView == null) {
	            LayoutInflater mInflater = (LayoutInflater)
	                    mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	            convertView = mInflater.inflate(R.layout.navigation_spinner_title, null);
	        }
	    	TextView title = (TextView) convertView.findViewById(R.id.item_title);
	        TextView subtitle = (TextView) convertView.findViewById(R.id.item_subtitle);
	    	
	        subtitle.setVisibility(View.GONE);
	        
        	if (mTitle != null) {
        		title.setText(mTitle);
        	}
        	if (mSubtitle != null) {
        		if (mSubtitle != "") {
        			subtitle.setVisibility(View.VISIBLE);
        		}
        		subtitle.setText(mSubtitle);
        	}
            return convertView;
	    }
	     
	 
	    @Override
	    public View getDropDownView(int position, View convertView, ViewGroup parent) {
	    	if (convertView == null) {
	            LayoutInflater mInflater = (LayoutInflater)
	            			mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	            convertView = mInflater.inflate(R.layout.navigation_spinner_item, null);
	    		
    		}
    		TextView item = (TextView) convertView; 
    		item.setText(mSpinnerItems.get(position));
            return item;
    
	    }
	    
	   
	    public void setTitle(String title) {
	    	mTitle = title;
	    	notifyDataSetChanged();
	    }
	    
	    public void setSubtitle(String subtitle) {
	    	mSubtitle = subtitle;
	    	notifyDataSetChanged();
	    }
	}
}
