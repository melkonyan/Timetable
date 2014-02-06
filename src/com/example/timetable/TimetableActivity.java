package com.example.timetable;


import java.util.Calendar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

public class TimetableActivity extends ActionBarActivity {
	
	Menu menu; 
	
	private LinearLayout eventLayout;
	
	private EventPager eventPager;
	
	private EventAdder eventAdder;
	
	private EventEditor eventEditor;
	
	private EventActionBar eventActionBar;
	
	private Page currentPage = Page.EVENT_VIEW;
	
	private MenuItem menuSaveEvent;
    
	private MenuItem menuAddEvent;
	
	private MenuItem menuDeleteEvent;
	
	public EventAdder getEventAdder() {
		return eventAdder;
	}
	
	public void setEventAdder(EventAdder eventAdder) {
		this.eventAdder = eventAdder;
	}
	
	public EventEditor getEventEditor() {
		return eventEditor;
	}
	
	public void setEventEditor(EventEditor eventEditor) {
		this.eventEditor = eventEditor;
	}
	
	public EventPager getEventPager() {
		return eventPager;
	}
	
	public void setEventPager(EventPager eventPager) {
		this.eventPager = eventPager;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Calendar cal = Calendar.getInstance();
		
		//enable debugging
		TimetableLogger.debugging = true;
		
		setContentView(R.layout.activity_events_table);
		eventLayout = (LinearLayout) findViewById(R.id.events_table);
		eventActionBar = new EventActionBar(this);
		setEventPager(new EventPager(this, eventActionBar, cal.getTime()));
		eventLayout.addView(eventPager,0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		menuSaveEvent = menu.findItem(R.id.action_save_event);
		menuAddEvent = menu.findItem(R.id.action_add_event);
		menuDeleteEvent = menu.findItem(R.id.action_delete_event);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_add_event:
	            setEventAdder(new EventAdder(this, eventPager.getDate())); 
	            switchPage(Page.EVENT_ADD);
	            return true;
	        case R.id.action_save_event:
	            switch (currentPage) {
	            	case EVENT_ADD: 
			            try {
				        	getEventAdder().saveEvent();
				            getEventPager().update();
				        	switchPage(Page.EVENT_VIEW);
			            } catch (IllegalEventDataException e) {
			            	Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			            }
			           return true;
	            	case EVENT_EDIT:
	            		try {
	            			//if eventEditor returns false, we should stay on this page
	            			if (!getEventEditor().saveEvent()) {
	            				return true;
	            			}
	            			getEventPager().update();
	            			switchPage(Page.EVENT_VIEW);
	            		} catch (IllegalEventDataException e) {
			            	Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			            }
	            		return true;
	            	default:
	            		return true;
	        	}
	        case R.id.action_delete_event:
	        	if (!getEventEditor().deleteEvent()) {
	        		return true;
	        	}
	        	getEventPager().update();
	        	switchPage(Page.EVENT_VIEW);
	        	return true;
		    default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onBackPressed() {
	    if (currentPage == Page.EVENT_ADD || currentPage == Page.EVENT_EDIT) {
	        switchPage(Page.EVENT_VIEW);
	        return;
	    }
	    // Otherwise defer to system default behavior.
	    super.onBackPressed();
	}
	
	public void onEventViewClick(View v) {
		try {
			String idString = ((TextView) (((RelativeLayout) v).getChildAt(0))).getText().toString();
			TimetableDatabase db = new TimetableDatabase(this);
			Event event = db.searchEventById(Integer.parseInt(idString));
			db.close();
			setEventEditor(new EventEditor(this, event, getEventPager().getDate()));
			switchPage(Page.EVENT_EDIT);
		} catch (Exception e) {
			TimetableLogger.log(e.getMessage());
			return;
		}
	}
	
	// update ActionBar menu, updates ActionBar title, add appropriate view to current RelativaLayout 
	void switchPage(Page page) {
		currentPage = page;
		eventLayout.removeViewAt(0);
		switch (page) {
			case EVENT_VIEW:
			    menuAddEvent.setVisible(true);
	            menuSaveEvent.setVisible(false);
	            menuDeleteEvent.setVisible(false);
	            eventLayout.addView(getEventPager());
	            break;
			case EVENT_ADD:
				menuAddEvent.setVisible(false);
	            menuSaveEvent.setVisible(true);
	            eventLayout.addView(getEventAdder());
	            break;
			case EVENT_EDIT:
				menuAddEvent.setVisible(false);
				menuSaveEvent.setVisible(true);
				menuDeleteEvent.setVisible(true);
				eventLayout.addView(getEventEditor());
				break;
		}
		//update action bar title
		eventActionBar.showTitle(currentPage);
		}

}
