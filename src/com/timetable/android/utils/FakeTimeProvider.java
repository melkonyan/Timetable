package com.timetable.android.utils;

import java.util.Calendar;
import java.util.Date;

public class FakeTimeProvider extends TimeProvider {

		private Calendar fakeTime = Calendar.getInstance();
		
		public FakeTimeProvider(Date fakeTime) {
			setTime(fakeTime);
		}
		
		public FakeTimeProvider(Calendar fakeTime) {
			setTime(fakeTime);
		}
		
		@Override
		public Calendar getCurrDateTimeCal() {
			return fakeTime;
		}
		
		public void setTime(Date fakeTime) {
			this.fakeTime.setTime(fakeTime);
		}
		
		public void setTime(Calendar fakeTime) {
			this.fakeTime.setTime(fakeTime.getTime());
		}
	
}
