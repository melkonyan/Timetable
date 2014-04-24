package com.timetable.app;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.timetable.app.R;

public class TimetableDatabase extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "TimeTable";
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SQLiteDatabase dbRead;
	
	private SQLiteDatabase dbWrite;
	
	public TimetableDatabase(Context context) {
		super(context, DB_NAME, null, 1);
		dbRead = this.getReadableDatabase();
		dbWrite = this.getWritableDatabase();
    }
	
	@Override
    public void onCreate(SQLiteDatabase db) {
		String query = String.format("CREATE TABLE Events ("
				+ "evt_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
				+ "evt_name VARCHAR(%s),"
				+ "evt_place VARCHAR(%s),"
				+ "evt_start_time TIME,"
				+ "evt_end_time TIME,"
				+ "evt_start_date DATE,"
				+ "evt_date DATE,"
				+ "per_id INT,"
				+ "evt_note TEXT)", Event.MAX_NAME_LENGTH, Event.MAX_PLACE_LENGTH);
		db.execSQL(query);
		query = "CREATE TABLE Exceptions ("
				+ "ex_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "evt_id INTEGER NOT NULL, "
				+ "ex_date DATE)"; 
		db.execSQL(query);
		query = "CREATE TABLE Periods ("
				+ "per_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "per_type INTEGER NOT NULL, "
				+ "per_interval INTEGER, "
				+ "per_week_occurences INTEGER,"
				+ "per_end_date DATE,"
				+ "per_num_of_repeats INTEGER)";
		db.execSQL(query);
		query = "CREATE TABLE Alarms (" +
				"alm_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"alm_time DATETIME," +
				"alm_type INTEGER," +
				"evt_id INTEGER)";
		try {
			TimetableLogger.log("Create table Alarms");
			db.execSQL(query);
		} catch (SQLException e) {
			TimetableLogger.error("Error" + e.getMessage());
		}
	}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    
    
    private ContentValues createContentValuesFromEventAlarm(EventAlarm alarm) {
    	ContentValues values = new ContentValues();
    	values.put("alm_type", alarm.type.ordinal());
    	values.put("alm_time", dateTimeFormat.format(alarm.time));
    	values.put("evt_id", alarm.eventId);
    	TimetableLogger.log(values.toString());
    	return values;
    }
    
    private EventAlarm getEventAlarmFromCursor(Cursor cursor) {
    	if (cursor.getCount() != 1) {
    		cursor.close();
    		return null;
    	}
    	cursor.moveToFirst();
    	EventAlarm alarm = new EventAlarm();
    	alarm.id = cursor.getInt(cursor.getColumnIndex("alm_id"));
    	alarm.type = EventAlarm.Type.values()[cursor.getInt(cursor.getColumnIndex("alm_type"))];
    	alarm.eventId = cursor.getInt(cursor.getColumnIndex("evt_id"));
    	
    	try {
    		alarm.time = dateTimeFormat.parse(cursor.getString(cursor.getColumnIndex("alm_time")));
    	} catch (Exception e) {
    	}
    	cursor.close();
    	return alarm;
    }
    
    public long insertEventAlarm(EventAlarm alarm) {
    	if (!alarm.isOk()) {
    		TimetableLogger.log("alarm is not Ok");
        	return -1;
    	}
    	return dbWrite.insert("Alarms", null, createContentValuesFromEventAlarm(alarm));
    }
    
    public int updateEventAlarm(EventAlarm alarm) {
    	return dbWrite.update("Alarms", createContentValuesFromEventAlarm(alarm), "alm_id = ?", new String [] {Integer.toString(alarm.id)});
    }
    
    public int deleteEventAlarm(EventAlarm alarm) {
    	return dbWrite.delete("Alarms","alm_id = ?", new String [] { Integer.toString(alarm.id)});
    }
    
    public EventAlarm searchEventAlarmById(int id) {
    	String query = "SELECT * FROM Alarms WHERE alm_id = ?";
    	Cursor cursor = dbRead.rawQuery(query, new String [] { Integer.toString(id) });
    	return getEventAlarmFromCursor(cursor);
    }
    
    public EventAlarm searchEventAlarmByEventId(int id) {
    	String query = "SELECT * FROM Alarms WHERE evt_id = ?";
    	Cursor cursor = dbRead.rawQuery(query, new String [] { Integer.toString(id) });
    	return getEventAlarmFromCursor(cursor);
    }
    
    private ContentValues createContentValuesFromEventPeriod(EventPeriod period) {
    	ContentValues values = new ContentValues();
    	values.put("per_type", period.type.ordinal());
    	values.put("per_interval", period.interval);
    	values.put("per_week_occurences", period.getWeekOccurrences());
    	values.put("per_end_date", period.endDate == null ? "" : dateFormat.format(period.endDate));
    	values.put("per_num_of_repeats", period.numberOfRepeats);
    	return values;
    }
    
    /*
     * returns id of inserted period
     * returns -1 if insertion was unsuccessful
     */
    public long insertEventPeriod(EventPeriod period) {
    	if (!period.isOk()) {
    		return -1;
    	}
    	return dbWrite.insert("Periods",null, createContentValuesFromEventPeriod(period));
    	
    }
    
    public int updateEventPeriod(EventPeriod period) {
    	return dbWrite.update("Periods", createContentValuesFromEventPeriod(period), "per_id = ?", new String [] {Integer.toString(period.id)} );
    }
    
    public int deleteEventPeriod(EventPeriod period) {
    	return dbWrite.delete("Periods", "per_id = ?", new String [] {Integer.toString(period.id)} );
    }
    
    public EventPeriod searchEventPeriodById(int id) {
    	String query = "SELECT * FROM Periods WHERE per_id = ?";
    	Cursor cursor = dbRead.rawQuery(query, new String [] { Integer.toString(id) });
    	if (cursor.getCount() == 0) {
    		cursor.close();
    		return null;
    	}
    	cursor.moveToFirst();
    	EventPeriod period = new EventPeriod(id);
    	period.type = EventPeriod.Type.values()[cursor.getInt(cursor.getColumnIndex("per_type"))];
    	period.interval = cursor.getInt(cursor.getColumnIndex("per_interval"));
    	period.setWeekOccurrences(cursor.getInt(cursor.getColumnIndex("per_week_occurences")));
    	try {
    		period.endDate = dateFormat.parse(cursor.getString(cursor.getColumnIndex("per_end_date")));
    	} catch (Exception e) {
    	}
    	period.numberOfRepeats = cursor.getInt(cursor.getColumnIndex("per_num_of_repeats"));
    	cursor.close();
    	return period;
    }
    
    public long insertException(Event event, Date date) {
    	ContentValues values = new ContentValues();
    	values.put("evt_id", event.id);
    	values.put("ex_date", dateFormat.format(date));
    	return dbWrite.insert("Exceptions", null, values);
    	}
    
    public int deleteAllEventExceptions(Event event) {
    	return dbWrite.delete("Exceptions", "evt_id = ?", new String [] { Integer.toString(event.id) } );
    	}
    
    //Check if periodic event has no session todays
    public boolean isException(Event event, Date date) {
    	String query = "SELECT * FROM Exceptions where evt_id = ? and ex_date = ?";
    	Cursor cursor = dbRead.rawQuery(query, new String [] {Integer.toString(event.id), dateFormat.format(date)});
    	boolean isException = cursor.getCount() != 0;
    	cursor.close();
    	return isException;
    }
    
    private ContentValues createContentValuesFromEvent(Event event) {
    	ContentValues values = new ContentValues();
    	values.put("evt_name", event.name);
    	values.put("evt_place", event.place);
    	values.put("evt_start_time", timeFormat.format(event.startTime));
    	values.put("evt_end_time", (event.endTime != null ? timeFormat.format(event.endTime) : ""));
    	values.put("evt_date", dateFormat.format(event.date));
    	values.put("per_id", event.period.id);
    	values.put("evt_note", event.note);
    	
    	return values;
    }
    
    public long insertEvent(Event event) {
    	event.period.id = (int) insertEventPeriod(event.period);
    	long id = dbWrite.insert("Events", null, createContentValuesFromEvent(event));
    	if (event.hasAlarm()) {
    		event.alarm.id = (int) id;
        	if (insertEventAlarm(event.alarm) == -1) {
        		TimetableLogger.log("Error inserting alarm");
        		return -1;
        	}
    	}
    	return id;
    }
    
    public int updateEvent(Event event) {
    	if (event.hasAlarm()) {
    		if (event.alarm.id == -1) {
    			if (insertEventAlarm(event.alarm) == -1) {
    				return -1;
    			}
    		}
    		else if (updateEventAlarm(event.alarm) == -1) {
    			return -1;
    		}
    	}
    	if (updateEventPeriod(event.period) == -1) {
    		return -1;
    	}
    	return dbWrite.update("Events", createContentValuesFromEvent(event), "evt_id = ?", new String [] {Integer.toString(event.id)});
    	}
    
    public int deleteEvent(Event event) {
    	deleteEventPeriod(event.period);
    	if (event.hasAlarm()) {
    		deleteEventAlarm(event.alarm);
    	}
    	return dbWrite.delete("Events", "evt_id = ?", new String [] {Integer.toString(event.id)});
    }
    
    private Event getEvent(Cursor cursor) {
    	Event event = new Event(cursor.getInt(cursor.getColumnIndex("evt_id")));
    	event.name = cursor.getString(cursor.getColumnIndex("evt_name"));
		event.place = cursor.getString(cursor.getColumnIndex("evt_place"));
		
		try {
			event.startTime = timeFormat.parse(cursor.getString(cursor.getColumnIndex("evt_start_time")));
			event.date = dateFormat.parse(cursor.getString(cursor.getColumnIndex("evt_date")));
    	} catch(Exception e) {
    		return null;
    	}
		
		try {
			event.endTime = timeFormat.parse(cursor.getString(cursor.getColumnIndex("evt_end_time")));
		} catch(Exception e) {
			//this fields are null by default
		}
		
		event.note = cursor.getString(cursor.getColumnIndex("evt_note"));
		event.period = searchEventPeriodById(cursor.getInt(cursor.getColumnIndex("per_id")));
		event.alarm = searchEventAlarmByEventId(event.id);
    	return event;
    }
    
    public Event searchEventById(int id) {
    	String query = "SELECT * FROM Events where evt_id = ?";
    	Cursor cursor = dbRead.rawQuery(query, new String [] {Integer.toString(id)});
    	Event event = null;
    	if (cursor.getCount() != 0) {
    		cursor.moveToFirst();
    		event = getEvent(cursor);
    	}
    	cursor.close();
    	return event;
    	
    }
    
    public Vector<Event> searchEventsByDate(Date date) {
    	String dateString = dateFormat.format(date);
    	String query = "SELECT * FROM Events order by evt_start_time ASC";
    	TimetableLogger.log(dateString);
    	Cursor cursor = dbRead.rawQuery(query, new String [] {});
    	
    	Vector<Event> events = new Vector<Event>();
    	if (cursor.getCount() == 0) {
    		cursor.close();
    		return events;
    	}
    	cursor.moveToFirst();
    	do {
    		Event event = getEvent(cursor);
    		if (event != null && event.isToday(date) && !isException(event, date)) {
    			events.add(event);
    		}
    	} while(cursor.moveToNext());
    	cursor.close();
    	return events;
    }
    
    @Override
    public void close() {
    	super.close();
    	dbRead.close();
    	dbWrite.close();
    }
}
