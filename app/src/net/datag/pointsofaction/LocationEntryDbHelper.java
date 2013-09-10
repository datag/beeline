package net.datag.pointsofaction;

import net.datag.pointsofaction.LocationEntryContract.LocationEntry;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocationEntryDbHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "LocationEntries.db";
    
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
        		LocationEntry._ID + " INTEGER PRIMARY KEY," +
        		LocationEntry.COLUMN_NAME_NAME + " TEXT," +
        		LocationEntry.COLUMN_NAME_LATITUDE + " REAL," +
        		LocationEntry.COLUMN_NAME_LONGITUDE + " REAL" +
        		" )";

    private static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME;
    
    public LocationEntryDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// FIXME
		db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
	}

	public Entry find(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] columns = new String[] {
			LocationEntry._ID,
			LocationEntry.COLUMN_NAME_NAME,
			LocationEntry.COLUMN_NAME_LATITUDE,
			LocationEntry.COLUMN_NAME_LONGITUDE
		};
		
		String selection = LocationEntry._ID + " = ?";
		String[] selectionArgs = { String.valueOf(id) };
		String limit = "1";
		
		Cursor c = db.query(LocationEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null, limit);
		
		if (c.moveToFirst() == false) {
			return null;
		}
		
		// FIXME: use column-mapping
		return new Entry(c.getInt(0), c.getString(1), c.getDouble(2), c.getDouble(3));
	}
	
	public void save(Entry entry) throws Exception {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(LocationEntry.COLUMN_NAME_NAME, entry.name);
		values.put(LocationEntry.COLUMN_NAME_LATITUDE, entry.latitude);
		values.put(LocationEntry.COLUMN_NAME_LONGITUDE, entry.longitude);
		
		if (entry.id != null) {
			String whereClause = LocationEntry._ID + " = ?";
			String[] whereArgs = { String.valueOf(entry.id) };
			db.update(LocationEntry.TABLE_NAME, values, whereClause, whereArgs);
		} else {
			if (db.insert(LocationEntry.TABLE_NAME, null, values) == -1) {
				throw new Exception("No resulting row for entry insert.");
			}
		}
	}
	
	final public class Entry {
		public Integer id;
		public String name;
		public Double latitude;
		public Double longitude;

		public Entry(Integer id, String name, Double latitude, Double longitude) {
			this.id = id;
			this.name = name;
			this.latitude = latitude;
			this.longitude = longitude;
		}
	}
}
