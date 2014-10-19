package ui;




import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.LinearLayout;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.timetable.android.DayViewPager;
import com.timetable.android.R;
import com.timetable.android.TimetableLogger;

public class MonthViewFragment extends Fragment {
	
	Activity mActivity;
	
	@Override 
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		TimetableLogger.log("MonthViewFragment. Fragment is being attached.");
		mActivity = activity;
	}
	
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		TimetableLogger.log("MonthViewFragment. Fragment has creates it's view.");
		
		return new MonthView(mActivity);
	}

}
