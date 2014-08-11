package com.timetable.android;

import org.holoeverywhere.app.Application;


/*@ReportsCrashes(
        formKey = "", // This is required for backward compatibility but not used
        mode = ReportingInteractionMode.SILENT,
        resToastText = R.string.event_add_date,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        reportType = org.acra.sender.HttpSender.Type.JSON,
        formUri = "http://timetable.iriscouch.com/acra-logs/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "acro_reporter",
        formUriBasicAuthPassword = "Melkonyan"
    )
*/
public class TimetableApp  extends Application {
	
	@Override
	public void onCreate() {
		 super.onCreate();
		 //ACRA.init(this);
		 TimetableLogger.log("TimetableApp.onCreate: Application is created. ACRA is initialized");
	}

}
