package com.timetable.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;

public class EventAlarmDialogActivity extends Activity {
	
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);

	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Test").create().show();
	}
}