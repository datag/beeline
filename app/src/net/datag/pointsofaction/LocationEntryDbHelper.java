package net.datag.pointsofaction;

import net.datag.pointsofaction.LocationEntryContract.LocationEntry;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class LocationEntryDbHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "LocationEntries.db";
    
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
        		LocationEntry._ID + " INTEGER PRIMARY KEY," +
        		//LocationEntry.COLUMN_NAME_ENTRY_ID + " INTEGER," +
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

}
