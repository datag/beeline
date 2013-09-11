package net.datag.beeline;

import android.provider.BaseColumns;

public final class LocationEntryContract {
	public LocationEntryContract() {}

	public static abstract class LocationEntry implements BaseColumns {
		public static final String TABLE_NAME = "location";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_LATITUDE = "latitude";
		public static final String COLUMN_NAME_LONGITUDE = "longitude";
	}
}
