package com.timetable.android;

import org.acra.*;
import org.acra.annotation.ReportsCrashes;
import org.holoeverywhere.app.Application;

import android.content.Intent;


@ReportsCrashes(
        formKey = "", // This is required for backward compatibility but not used
        mode = ReportingInteractionMode.SILENT,
        resToastText = R.string.event_add_date,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        reportType = org.acra.sender.HttpSender.Type.JSON,
        formUri = "http://timetable.iriscouch.com/acra-logs/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "acro_reporter",
        formUriBasicAuthPassword = "Melkonyan",
        logcatArguments = { "-t", "100", "-v", "time", "-s", "Timetable"}
		)

public class TimetableApp  extends Application {
	
	@Override
	public void onCreate() {
		 super.onCreate();
		
		//enable debugging
		Logger.debugging = true;
			
		Logger.sendReport = false;
		
		//send broadcast, that application is started.
		sendBroadcast(new Intent(BroadcastActions.ACTION_APP_STARTED));
			
		//initialize acra
		//ACRA.init(this);
		Logger.log("TimetableApp.onCreate: Application is created. ACRA is initialized");
	}


}
