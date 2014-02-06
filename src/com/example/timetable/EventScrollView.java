package com.example.timetable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class EventScrollView extends ScrollView {
		
	private float xDistance, yDistance, lastX, lastY;
	
	private TimetableLogger logger = new TimetableLogger();
	
	public EventScrollView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    logger.log("EventScrollView created");
	}
	
	public EventScrollView(Context context) {
	    super(context);
	    logger.log("EventScrollView created");
		}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
	    logger.log("EventScrollView intercepted MotionEvent");
	    switch (ev.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	            logger.log("Action down");
	        	xDistance = yDistance = 0f;
	            lastX = ev.getX();
	            lastY = ev.getY();
	            break;
	        case MotionEvent.ACTION_MOVE:
	            final float curX = ev.getX();
	            final float curY = ev.getY();
	            xDistance += Math.abs(curX - lastX);
	            yDistance += Math.abs(curY - lastY);
	            lastX = curX;
	            lastY = curY;
	            logger.log("Action move. Moving: x - " + xDistance + " y - " + yDistance);
	            if(xDistance > yDistance)
	                return false;
	    }
	    //return true;
	    return super.onInterceptTouchEvent(ev);
	}
}

