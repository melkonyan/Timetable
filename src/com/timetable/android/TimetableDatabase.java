package com.timetable.android;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.timetable.android.alarm.EventAlarm;
import com.timetable.android.utils.DateFormatFactory;

/*
 * Class for working with database(inserting, updating and deleting events, etc.).
 */
public class TimetableDatabase extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "TimeTable";
	
	private static final SimpleDateFormat dateFormat = DateFormatFactory.getFormat("yyyy-MM-dd");
	
	private SQLiteDatabase dbRead;
	
	private SQLiteDatabase dbWrite;
	
	private static TimetableDatabase mInstance;
	
	public static TimetableDatabase getInstance(Context context) {
		return getInstance(context, false);
	}
	
	/*
	 * Is needed for testing.
	 */
	public static TimetableDatabase getInstance(Context context, boolean updateCurrent) {
		if (updateCurrent || mInstance == null) {
			mInstance = new TimetableDatabase(context);
			Logger.log("TimetableDatabase.getInstance: creating new instance.");
		}
		return mInstance;
	}
	
	private TimetableDatabase(Context context) {
		super(context, DB_NAME, null, 3);
		dbRead = this.getReadableDatabase();
		dbWrite = this.getWritableDatabase();
    }
	
	@Override
    public void onCreate(SQLiteDatabase db) {
		/*
		 * Table, containing events.
		 */
		String query = String.format("CREATE TABLE Events ("
				+ "evt_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
				+ "evt_name VARCHAR(%s),"
				+ "evt_place VARCHAR(%s),"
				+ "evt_start_time BIGINT,"
				+ "evt_end_time BIGINT,"
				+ "evt_date BIGINT,"
				+ "per_id INT," 
				+ "evt_mute_device INT,"   
				+ "evt_note TEXT)", Event.MAX_NAME_LENGTH, Event.MAX_PLACE_LENGTH);
		db.execSQL(query);
		
		/*
		 * Table, containing days, on which repeating event has no occurrence.
		 */
		query = "CREATE TABLE Exceptions ("
				+ "ex_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "evt_id INTEGER NOT NULL, "
				+ "ex_date DATE)"; 
		db.execSQL(query);
		
		/*
		 * Table, containing information about event's period type, end date, interval etc.
		 */
		query = "CREATE TABLE Periods ("
				+ "per_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "per_type INTEGER NOT NULL, "
				+ "per_interval INTEGER, "
				+ "per_week_occurences INTEGER,"
				+ "per_end_date BIGINT,"
				+ "per_num_of_repeats INTEGER)";
		db.execSQL(query);
		
		/*
		 * Table, containing information about event's alarm.
		 */
		query = "CREATE TABLE Alarms (" +
				"alm_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"alm_time BIGINT," +
				"alm_type INTEGER," +
				"evt_id INTEGER)";
		db.execSQL(query);
	}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	Logger.log("Upgrading database from " + oldVersion + "to " + newVersion + ". All data will be deleted.");
    	db.execSQL("DROP TABLE IF EXISTS Events");
    	db.execSQL("DROP TABLE IF EXISTS Periods");
    	db.execSQL("DROP TABLE IF EXISTS Alarms");
    	db.execSQL("DROP TABLE IF EXISTS Exceptions"); 
        onCreate(db);
    }
    

    private ContentValues createContentValuesFromEventAlarm(EventAlarm alarm) {
    	ContentValues values = new ContentValues();
    	values.put("alm_type", alarm.type.ordinal());
    	values.put("alm_time", alarm.getTimeMillis());
    	values.put("evt_id", alarm.event.getId());
    	return values;
    }
    
    private EventAlarm getEventAlarmFromCursor(Cursor cursor, Event event) {
    	EventAlarm alarm = new EventAlarm(event);
    	alarm.id = cursor.getInt(cursor.getColumnIndex("alm_id"));
    	alarm.type = EventAlarm.Type.values()[cursor.getInt(cursor.getColumnIndex("alm_type"))];
    	alarm.setTime(cursor.getLong(cursor.getColumnIndex("alm_time")));
    	alarm.event = event;
    	return alarm;
    }
    
    /*
     * Insert alarm of given event into database.
     * Return id of inserted alarm or -1, if an error has occurred during insertion.
     */
    public long insertEventAlarm(Event event) {
    	EventAlarm alarm = event.getAlarm();
    	if (!alarm.isOk()) {
    		Logger.log("alarm is not Ok");
        	return -1;
    	}
    	return dbWrite.insert("Alarms", null, createContentValuesFromEventAlarm(alarm));
    }
    
    /*
     * Update event's alarm.
     * Return number of updated rows.
     */
    public int updateEventAlarm(Event event) {
    	if (!event.hasAlarm()) {
    		return 0;
    	}
    	return dbWrite.update("Alarms", createContentValuesFromEventAlarm(event.getAlarm()), "alm_id = ?", 
    							new String [] {Integer.toString(event.getAlarm().id)});
    }
    
    /*
     * Delete event's alarm.
     * Return number of deleted rows.
     */
    public int deleteEventAlarm(Event event) {
    	return dbWrite.delete("Alarms","evt_id = ?", new String [] { Integer.toString(event.getId())});
    }
    
    /*
     * Search alarm of given event.
     * Return null if alarm is not found.
     */
    public EventAlarm getEventAlarm(Event event) {
    	String query = "SELECT * FROM Alarms WHERE evt_id = ?";
    	Cursor cursor = dbRead.rawQuery(query, new String [] { Integer.toString(event.getId()) });
    	if (cursor.getCount() != 1) {
    		cursor.close();
    		return null;
    	}
    	cursor.moveToFirst();
    	
    	return getEventAlarmFromCursor(cursor, event);
    
    }
    
    /* 
     * Return number of alarms in the database
     */
    public int getAlarmsCount() {
    	Cursor cursor = dbRead.rawQuery("SELECT COUNT(*) FROM Alarms", new String [] {});
    	if (cursor != null && cursor.getCount() > 0) {
    		cursor.moveToFirst();
    		return cursor.getInt(0);
    	}
    	return 0;
    }
    
    private ContentValues createContentValuesFromEventPeriod(EventPeriod period) {
    	ContentValues values = new ContentValues();
    	values.put("per_type", period.getTypeInt());
    	values.put("per_interval", period.getInterval());
    	values.put("per_week_occurences", period.getWeekOccurrencesInt());
    	values.put("per_end_date", period.getEndDateMillis());
    	values.put("per_num_of_repeats", period.getNumberOfRepeats());
    	return values;
    }
    
    /*
     * Insert period into Periods table.
     * Return id of inserted period, -1 if insertion was unsuccessful
     */
    public long insertEventPeriod(EventPeriod period) {
    	if (!period.isOk()) {
    		return -1;
    	}
    	return dbWrite.insert("Periods",null, createContentValuesFromEventPeriod(period));
    	
    }
    
    /*
     * Update period.
     * Return number of updated rows.
     */
    public int updateEventPeriod(EventPeriod period) {
    	return dbWrite.update("Periods", createContentValuesFromEventPeriod(period), "per_id = ?", 
    							new String [] {Integer.toString(period.getId())} );
    }
    
    /*
     * Delete period.
     * Return number of deleted rows. 
     */
    public int deleteEventPeriod(EventPeriod period) {
    	return dbWrite.delete("Periods", "per_id = ?", new String [] {Integer.toString(period.getId())} );
    }
    
    /*
     * Search period given period id.
     */
    public EventPeriod searchEventPeriodById(int id) {
    	String query = "SELECT * FROM Periods WHERE per_id = ?";
    	Cursor cursor = dbRead.rawQuery(query, new String [] { Integer.toString(id) });
    	if (cursor.getCount() == 0) {
    		Logger.error("TimetableDatabase.serachEventPeriodById: Error. Period with id " + Integer.toString(id) + " is not found.");
    		cursor.close();
    		return null;
    	}
    	cursor.moveToFirst();
    	EventPeriod period = new EventPeriod(id);
    	period.setType(cursor.getInt(cursor.getColumnIndex("per_type")));
    	period.setInterval(cursor.getInt(cursor.getColumnIndex("per_interval")));
    	period.setWeekOccurrences(cursor.getInt(cursor.getColumnIndex("per_week_occurences")));
    	period.setEndDate(cursor.getLong(cursor.getColumnIndex("per_end_date")));
    	period.setNumberOfRepeats(cursor.getInt(cursor.getColumnIndex("per_num_of_repeats")));
    	cursor.close();
    	return period;
    }
    
    /*
     * Insert into Exceptions event id and date, on which this repeated event has no occurrence.
     */
    public long insertException(Event event, Date date) {
    	ContentValues values = new ContentValues();
    	values.put("evt_id", event.getId());
    	values.put("ex_date", dateFormat.format(date));
    	return dbWrite.insert("Exceptions", null, values);
    	}
    /*
     * Get all event exceptions.
     */
    public Set<Date> getEventExceptions(Event event) {
    	String query = "SELECT * FROM Exceptions where evt_id = ?";
    	Cursor cursor = dbRead.rawQuery(query,  new String [] {Integer.toString(event.getId())});
    	Set<Date> exceptions = new TreeSet<Date>(Event.EXCEPTION_COMPARATOR); 
    	if (cursor.getCount() == 0) {
    		cursor.close();
    		return exceptions;
    	}
    	cursor.moveToFirst();
    	do {
    		try {
    			Date exception = dateFormat.parse(cursor.getString(cursor.getColumnIndex("ex_date")));
    			exceptions.add(exception);
    		} catch (Exception e) {	
    			Logger.log("TimetableDatabase.getEventException: Ivalid exception found.");
    		}
    	} while(cursor.moveToNext());
    	cursor.close();
    	return exceptions;
    }
    
    /*
     * Delete all exceptions of given event.
     */
    public int deleteAllEventExceptions(Event event) {
    	return dbWrite.delete("Exceptions", "evt_id = ?", new String [] { Integer.toString(event.getId()) } );
    	}
    
    /*
     * Check if repeated event has no occurrence today.
     */
    public boolean isException(Event event, Date date) {
    	String query = "SELECT * FROM Exceptions where evt_id = ? and ex_date = ?";
    	Cursor cursor = dbRead.rawQuery(query, new String [] {Integer.toString(event.getId()), dateFormat.format(date)});
    	boolean isException = cursor.getCount() != 0;
    	cursor.close();
    	return isException;
    }
    
    private ContentValues createContentValuesFromEvent(Event event) {
    	ContentValues values = new ContentValues();
    	values.put("evt_name", event.getName());
    	values.put("evt_place", event.getPlace());
    	values.put("evt_start_time", event.getStartTimeMillis());
    	values.put("evt_end_time", event.getEndTimeMillis());
    	values.put("evt_date", event.getDateMillis());
    	values.put("per_id", event.getPeriod().getId());
    	values.put("evt_mute_device", event.mutesDevice());
    	values.put("evt_note", event.getNote());
    	return values;
    }
    
    /*
     * Return true, if event with given id is contained in database.
     */
    public boolean existsEvent(Event event) {
    	if (event.isNew()) {
    		return false;
    	}
    	String query = "SELECT EXISTS(SELECT 1 FROM Events WHERE evt_id = ? LIMIT 1)";
    	Cursor cursor = dbRead.rawQuery(query, new String [] { Integer.toString(event.getId()) } );
    	cursor.moveToFirst();
    	return cursor.getInt(0) == 1;
    }
    /*
     * Insert given event into database.
     * Return given event with appropriate values set to fields event.id, event.period.id, event.alarm.id.
     */
    public Event insertEvent(Event event) {
    	event.getPeriod().setId((int) insertEventPeriod(event.getPeriod()));
    	if (event.getPeriod().getId() == -1) {
    		Logger.log("TimetableDatabase.insertEvent: Error inserting event's period.");
    		return null;
    	}
    	event.setId((int) dbWrite.insert("Events", null, createContentValuesFromEvent(event)));
    	Logger.log("TimetablaDatabase.insertEvent: inserted id: " + Integer.toString(event.getId()));
    	if (event.hasAlarm()) {
    		event.getAlarm().id = (int) insertEventAlarm(event);
    		event.getAlarm().event = event;
    		if (event.getAlarm().id == -1) {
        		Logger.log("TimetablseDatabase.insertEvent: Error inserting event's alarm");
        		return null;
        	}
    	}
    	return event;
    }
    
    /*
     * Update event.
     * Return given event, with event.alarm.id field to appropriate value, if needed.
     */
    public Event updateEvent(Event event) {
    	if (event.hasAlarm()) {
    		if (event.getAlarm().isNew()) {
    			event.getAlarm().id = (int) insertEventAlarm(event);
    			if (event.getAlarm().id == -1) {
    				Logger.log("TimetableDatabase.updateEvent: Error inserting alarm.");
    				return null;
    			}
    			
    		}
    		else if (updateEventAlarm(event) != 1) {
    			Logger.log("TimetableDatabase.updateEvent: Error updating alarm.");
    			return null;
    		}
    	}
    	else {
    		deleteEventAlarm(event);
    	}
    	
    	if (updateEventPeriod(event.getPeriod()) != 1) {
    		Logger.log("TimetableDatabase.updateEvent: Error updating period.");
    		return null;
    	}
    	if (dbWrite.update("Events", createContentValuesFromEvent(event), "evt_id = ?", 
    							new String [] {Integer.toString(event.getId())}) != 1) {
    		Logger.log("TimetableDatabase.updateEvent: Error updating event.");
        	return null;
    	}
    	return event;
    }
    
    /*
     * Delete event.
     * Return number of deleted rows. 
     */
    public int deleteEvent(Event event) {
    	deleteEventPeriod(event.getPeriod());
    	if (event.hasAlarm()) {
    		deleteEventAlarm(event);
    	}
    	return dbWrite.delete("Events", "evt_id = ?", new String [] {Integer.toString(event.getId())});
    }
    
    private Event getEventFromCursor(Cursor cursor) {
    	Event.Builder builder = new Event.Builder();
    	builder.setId(cursor.getInt(cursor.getColumnIndex("evt_id")))
    			.setName(cursor.getString(cursor.getColumnIndex("evt_name")))
    			.setPlace(cursor.getString(cursor.getColumnIndex("evt_place")))
    			.setNote(cursor.getString(cursor.getColumnIndex("evt_note")))
    			.setPeriod(searchEventPeriodById(cursor.getInt(cursor.getColumnIndex("per_id"))))
    			.setMuteDevice(cursor.getInt(cursor.getColumnIndex("evt_mute_device")) != 0)
    			.setStartTime(cursor.getLong(cursor.getColumnIndex("evt_start_time")))
				.setDate(cursor.getLong(cursor.getColumnIndex("evt_date")))
				.setEndTime(cursor.getLong(cursor.getColumnIndex("evt_end_time")));
		Event event = builder.build();
		event.setAlarm(getEventAlarm(event));
		event.setExceptions(getEventExceptions(event));
		//Logger.error(event.name + " " + dateFormat.format(event.date) + " - exceptions: " + Integer.toString(event.exceptions.size()));
    	return event;
    }
    
    /*
     * Search event given event id.
     */
    public Event searchEventById(int id) {
    	String query = "SELECT * FROM Events where evt_id = ?";
    	Cursor cursor = dbRead.rawQuery(query, new String [] {Integer.toString(id)});
    	Event event = null;
    	if (cursor.getCount() != 0) {
    		cursor.moveToFirst();
    		event = getEventFromCursor(cursor);
    	}
    	cursor.close();
    	return event;
    	
    }
    
    /*
     * Return all events, that have not finished. 
     */
    public Vector<Event> getAllEvents() {
    	Cursor cursor = dbRead.rawQuery("SELECT * FROM Events order by evt_start_time ASC", new String [] {});
    	Vector<Event> events = new Vector<Event>(); 
    	if (cursor.getCount() == 0) {
    		cursor.close();
    		return events;
    	}
    	cursor.moveToFirst();
    	do {
    		Event event = getEventFromCursor(cursor);
    		if (event == null) {
    			Logger.error("TimetableDatabase.getAllEvents: Error creating event");
    		} else {
    			events.add(event);
    		}
    	} while (cursor.moveToNext());
    	cursor.close();
    	return events;
    }
    
    /*
     * Return events, that have alarm.
     */
    public Vector<Event> searchEventsWithAlarm() {
    	Cursor cursor = dbRead.rawQuery("SELECT * FROM Events INNER JOIN Alarms ON Events.evt_id = Alarms.evt_id", new String [] {});
    	Vector<Event> events = new Vector<Event>(); 
    	if (cursor.getCount() == 0) {
    		cursor.close();
    		return events;
    	}
    	cursor.moveToFirst();
    	do {
    		Event event = getEventFromCursor(cursor);
    		if (event == null) {
    			Logger.error("TimetableDatabase.searchEventsWithAlarm: Error creating event");
    		}
    		events.add(event);
    	} while (cursor.moveToNext());
    	cursor.close();
    	return events;
    }
    
    /*
     * Return events, that mute device.
     */
    public Vector<Event> searchEventsThatMuteDevice() {
    	Cursor cursor = dbRead.rawQuery("SELECT * FROM Events where evt_mute_device = 1", new String [] {});
    	Vector<Event> events = new Vector<Event>(); 
    	if (cursor.getCount() == 0) {
    		cursor.close();
    		return events;
    	}
    	cursor.moveToFirst();
    	do {
    		Event event = getEventFromCursor(cursor);
    		events.add(event);
    	} while (cursor.moveToNext());
    	cursor.close();
    	return events;
    }
    
    /*
     * Return all events, that have occurrence on given date.
     */
    public Vector<Event> searchEventsByDate(Date date) {
    	String query = "SELECT * FROM Events order by evt_start_time ASC";
    	Cursor cursor = dbRead.rawQuery(query, new String [] {});
    	
    	Vector<Event> events = new Vector<Event>();
    	if (cursor.getCount() == 0) {
    		cursor.close();
    		return events;
    	}
    	cursor.moveToFirst();
    	do {
    		Event event = getEventFromCursor(cursor);
    		if (event != null && event.isToday(date)) {
    			events.add(event);
    		}
    	} while(cursor.moveToNext());
    	cursor.close();
    	return events;
    }
    
    /*
     * Delete all content from database.
     * Used for testing.
     */
    public void clear() {
    	dbWrite.delete("Events", null, null);
    	dbWrite.delete("Exceptions", null, null);
    	dbWrite.delete("Alarms", null, null);
    	dbWrite.delete("Periods", null, null);
    }
    
    @Override
    @Deprecated
    public void close() {
    	//super.close();
    }
}
