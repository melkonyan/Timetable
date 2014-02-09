package com.example.timetable.tests;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

public class MyInstrumentationTestRunner extends InstrumentationTestRunner {

		@Override 
		public TestSuite getAllTests() {
			InstrumentationTestSuite suite = new InstrumentationTestSuite(this);
			suite.addTestSuite(TimetableDatabaseTestCase.class);
			suite.addTestSuite(EventTestCase.class);
			suite.addTestSuite(EventPeriodTestCase.class);
			suite.addTestSuite(TimetableFunctionalTestCase.class);
			suite.addTestSuite(EventAddActivityTestCase.class);
			return suite;
		}
}
