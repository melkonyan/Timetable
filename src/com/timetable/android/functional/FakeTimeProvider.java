package com.timetable.android.functional;

import java.util.Date;

public class FakeTimeProvider implements TimeProvider {

		private Date fakeTime;
		
		public FakeTimeProvider(Date fakeTime) {
			setTime(fakeTime);
		}
		
		@Override
		public Date getCurrentTime() {
			return fakeTime;
		}
		
		public void setTime(Date fakeTime) {
			this.fakeTime = fakeTime;
		}
	
}
