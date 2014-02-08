package com.example.timetable;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class TimetableDatabase extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "TimeTable";
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
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
	}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    
    private boolean execSQL(String query) {
    	try {
    		dbWrite.execSQL(query);
    		return true;
    	} catch (SQLException e) {
    		TimetableLogger.log("Timetabledatabase error: " + e.getMessage());
    		return false;
    	}
    }
    
    /*
     * returns id of inserted period
     * returns -1 if insertion was unsuccessful
     */
    public int insertEventPeriod(EventPeriod period) {
    	if (!period.isOk()) {
    		return -1;
    	}
    		String query = String.format("INSERT INTO Periods "
	    			+ "(per_type, per_interval, per_week_occurences, per_end_date, per_num_of_repeats) "
	    			+ "VALUES (\'%s\', \'%s\', \'%s\', \'%s\', \'%s\')",
	    			period.type.ordinal(), period.interval, period.getWeekOccurrences(), 
	    			(period.endDate == null ? null : dateFormat.format(period.endDate)), period.numberOfRepeats);
	    	if (!execSQL(query)) {
	    		return -1;
	    	}
    		query = "SELECT per_id from Periods where per_id = (SELECT MAX(per_id) FROM Periods)";
	    	Cursor cursor = dbRead.rawQuery(query, null);
	    	cursor.moveToFirst();
	    	int id = cursor.getInt(0);
	    	cursor.close();
	    	return id;
    	
    }
    
    public boolean updateEventPeriod(EventPeriod period) {
    	String query = String.format("UPDATE Periods SET "
    			+ "per_type = \'%s\',"
    			+ "per_interval = \'%s\', "
    			+ "per_week_occurences = \'%s\',"
    			+ "per_end_date = \'%s\',"
    			+ "per_num_of_repeats = \'%s\'"
    			+ "WHERE per_id = \'%s\'",
    			period.type.ordinal(), period.interval, period.getWeekOccurrences(), 
    			(period.endDate == null ? null : dateFormat.format(period.endDate)), period.numberOfRepeats, period.id);
    	return execSQL(query);
    }
    
    public boolean deleteEventPeriod(EventPeriod period) {
    	String query = String.format("DELETE from Period WHERE per_id = %s", period.id);
    	return execSQL(query);
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
    
    public boolean insertException(Event event, Date date) {
    	String query = String.format("INSERT INTO Exceptions (evt_id, ex_date) values (\'%s\',\'%s\')", 
    								event.id, dateFormat.format(date));
    	return execSQL(query);
        }
    
    public boolean deleteAllEventExceptions(Event event) {
    	String query = String.format("DELETE FROM Exceptions WHERE evt_id = \'%s\'", event.id);
    	return execSQL(query);
        }
    
    //Check if periodic event has no session todays
    public boolean isException(Event event, Date date) {
    	String query = "SELECT * FROM Exceptions where evt_id = ? and ex_date = ?";
    	Cursor cursor = dbRead.rawQuery(query, new String [] {Integer.toString(event.id), dateFormat.format(date)});
    	boolean isException = cursor.getCount() != 0;
    	cursor.close();
    	return isException;
    }
    
    public boolean insertEvent(Event event) {
    	event.period.id = insertEventPeriod(event.period);
    	
    	String query = String.format("INSERT INTO Events "
    									+ "(evt_name, evt_place, evt_start_time, evt_end_time, evt_date, per_id, evt_note) "
    									+ "values (\'%s\', \'%s\', \'%s\', \'%s\', \'%s\', %d, \'%s\')", 
    									event.name, event.place, timeFormat.format(event.startTime), 
    									(event.endTime != null ? timeFormat.format(event.endTime) : null), 
    									dateFormat.format(event.date), event.period.id, event.note);
    	return execSQL(query);
        }
    
    public boolean updateEvent(Event event) {
    	updateEventPeriod(event.period);
    	String query = String.format("UPDATE Events SET "
    									+ "evt_name = \'%s\', "
    									+ "evt_place = \'%s\', "
    									+ "evt_start_time = \'%s\', "
    									+ "evt_end_time = \'%s\',"
    									+ "evt_date = \'%s\',"
    									+ "evt_note = \'%s\' "
    									+ " WHERE evt_id = %s",
    									event.name, event.place, timeFormat.format(event.startTime), 
    									(event.endTime != null ? timeFormat.format(event.endTime) : null), 
    									dateFormat.format(event.date), event.note, event.id);
    	return execSQL(query);
        }
    
    public boolean deleteEvent(Event event) {
    	String query = String.format("DELETE FROM Events WHERE evt_id = \'%s\'", event.id);
    	return execSQL(query);
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
