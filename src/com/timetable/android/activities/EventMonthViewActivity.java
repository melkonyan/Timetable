package com.timetable.android.activities;



import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.timetable.android.R;

public class EventMonthViewActivity extends ActionBarActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_month_view);
		
		GridView greedView = (GridView) findViewById(R.id.month_days);
		greedView.setAdapter(new MonthViewAdapter(this));
		
	}
	
	public class MonthViewAdapter extends BaseAdapter {

		private Context mContext;
		
		public MonthViewAdapter(Context context) {
			mContext = context;
		}
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View converView, ViewGroup Parent) {
			TextView textView = new TextView(mContext);
			
			textView.setText("1");
			return textView;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 49;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
