package com.timetable.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/*
 * Class that should be used to create EventViews.
 * It tries to reuse EventView, if it is possible.
 * After EventView is not needed(visible) anymore, method releaseViews should be called.
 */
public class EventViewProvider {

	private static EventViewProvider mInstance;
	
	private Context mContext;
	
	private static final int ID_NO_OWNER = 0;
	
	private List<ViewContainer> views = new ArrayList<ViewContainer>();
	
	private EventViewProvider(Context context) {
		mContext = context;
	}
	
	public static EventViewProvider getInstance(Context context) {
		if (mInstance == null) {
			return new EventViewProvider(context);
		}
		return mInstance;
	}
	
	/*
	 * Get view. If there is free view, return it.
	 * Assign view to given owner.
	 */
	public EventView getView(int ownerId) {
		if (ownerId == ID_NO_OWNER) {
			return null;
		}
		for (ViewContainer view: views) {
			if (!hasOwner(view)) {
				view.ownerId = ownerId;
				return view.eventView;
			}
		}
		return new EventView(mContext);
	}
	
	/*
	 * Release all views, attached to given owner, so that they can be used later by another owner.
	 */
	public void releaseViews(int ownerId) {
		for (ViewContainer view: views) {
			if (view.ownerId == ownerId) {
				view.ownerId = ID_NO_OWNER;
			}
		}
	}
	
	private boolean hasOwner(ViewContainer view) {
		return view.ownerId != ID_NO_OWNER;
	}
	
	static class ViewContainer {
		
		public int ownerId;
		
		public EventView eventView;
		
		public ViewContainer(EventView _eventView, int _ownerId) {
			eventView = _eventView;
			ownerId = _ownerId;
		}
		
	}
}
