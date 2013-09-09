package net.datag.pointsofaction;

import java.text.DecimalFormat;

import net.datag.pointsofaction.LocationEntryContract.LocationEntry;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class MainActivity extends Activity implements
		LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 1000;
    private static final float SMALLEST_DISPLACEMENT = 1.0f;
    
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
    
	private TextView textLatLng;
	//private TextView textInfo;
	private ListView listLocations;
	private Location lastLocation;
	
	SimpleCursorAdapter mAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textLatLng = (TextView) findViewById(R.id.text_latlng);
		//textInfo = (TextView) findViewById(R.id.text_info);
		listLocations = (ListView) findViewById(R.id.list_locations);
		
		
		// configure location request
		mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        
        // initialize location client
        mLocationClient = new LocationClient(this, this, this);
        
        initListView();
	}
	
	protected void initListView() {
        LocationEntryDbHelper mDbHelper = new LocationEntryDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();


	     // Define a projection that specifies which columns from the database
	     // you will actually use after this query.
	     String[] projection = {
	    		 LocationEntry._ID,
	    		 LocationEntry.COLUMN_NAME_NAME,
	    		 LocationEntry.COLUMN_NAME_LATITUDE,
	    		 LocationEntry.COLUMN_NAME_LONGITUDE
	     };
	     
	     // How you want the results sorted in the resulting Cursor
	     String sortOrder =
	    		 LocationEntry.COLUMN_NAME_NAME + " ASC";
	
	     Cursor c = db.query(
	         LocationEntry.TABLE_NAME,  // The table to query
	         projection,                               // The columns to return
	         null /*selection*/,                                // The columns for the WHERE clause
	         null /*selectionArgs*/,                            // The values for the WHERE clause
	         null,                                     // don't group the rows
	         null,                                     // don't filter by row groups
	         sortOrder                                 // The sort order
	         );

	     
	     // add to listview
	     final class EntryCursorAdapter extends CursorAdapter {
	    	private LayoutInflater inflater;
	    	private int layout;
	    	private int columnIndexName;
	    	private int columnIndexLat;
	    	private int columnIndexLng;

			public EntryCursorAdapter(Context context, Cursor c) {
				super(context, c, 0);
				inflater = LayoutInflater.from(context);
				layout = R.layout.view_listitem;
				columnIndexName = c.getColumnIndexOrThrow(LocationEntry.COLUMN_NAME_NAME);
				columnIndexLat = c.getColumnIndexOrThrow(LocationEntry.COLUMN_NAME_LATITUDE);
				columnIndexLng = c.getColumnIndexOrThrow(LocationEntry.COLUMN_NAME_LONGITUDE);
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				TextView viewText1 = (TextView) view.getTag(R.id.view_listitem_text1);
				viewText1.setText(cursor.getString(columnIndexName));
				
				
		 		TextView viewText2 = (TextView) view.getTag(R.id.view_listitem_text2);
		 		
		 		String strInfo = "";
		 		DecimalFormat fmtKm = new DecimalFormat("0.00");
	 			if (lastLocation != null) {
	 				Location dest = new Location("app");
	 				dest.setLatitude(cursor.getDouble(columnIndexLat));
	 				dest.setLongitude(cursor.getDouble(columnIndexLng));
	 				
	 				float d = lastLocation.distanceTo(dest);

	 				if (d < 1000) {
	 					strInfo = Math.round(d) + " m ";
	 				} else {
	 					strInfo = fmtKm.format(d / 1000) + " km";
	 				}
	 			} else {
	 				strInfo = "?";
	 			}
	 			viewText2.setText(strInfo);
			}
			
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				View view = inflater.inflate(layout, parent, false);
				
				// set tag
				view.setTag(cursor.getString(columnIndexName));
				
				view.setTag(R.id.view_listitem_text1, view.findViewById(R.id.view_listitem_text1));
				view.setTag(R.id.view_listitem_text2, view.findViewById(R.id.view_listitem_text2));
				
				bindView(view, context, cursor);
				
				return view;
			}
	    	 
	     };
		 
	     listLocations.setAdapter(new EntryCursorAdapter(this, c));
	}
	
	public void doTest(View view) {
		final class TestEntry {
			public String name;
			public double lat;
			public double lng;
			
			public TestEntry(String name, double lat, double lng) {
				this.name = name;
				this.lat = lat;
				this.lng = lng;
			}
		};
		
		TestEntry[] entries = {
			new TestEntry("Home", 48.03504, 10.73137),
			new TestEntry("Parents", 47.99843, 10.78052),
			new TestEntry("Work", 48.03301, 10.73231)
		};
		
		try {
			LocationEntryDbHelper mDbHelper = new LocationEntryDbHelper(this);
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			
			for (TestEntry entry: entries) {
				// Create a new map of values, where column names are the keys
				ContentValues values = new ContentValues();
				values.put(LocationEntry.COLUMN_NAME_NAME, entry.name);
				values.put(LocationEntry.COLUMN_NAME_LATITUDE, entry.lat);
				values.put(LocationEntry.COLUMN_NAME_LONGITUDE, entry.lng);
				
				// Insert the new row, returning the primary key value of the new row
				/* long newRowId = */ db.insert(LocationEntry.TABLE_NAME, null, values);
			}
        
		} catch (Exception e) {
        	Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        	System.out.println(e.getMessage());
        }
	}
	
//	final private class Entry {
//		private String name;
//		private Location location;
//		
//		public Entry(String name, Location location) {
//			this.name = name;
//			this.location = location;
//		}
//		
//		public String getName() {
//			return name;
//		}
//		
//		public Location getLocation() {
//			return location;
//		}
//	};

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		// unregister updates for location, if connected
		if (mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(this);
		}
		
		// disconnect the location client
		mLocationClient.disconnect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// connect location client on start
		mLocationClient.connect();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			openNewEntry();
			return true;
		case R.id.action_settings:
			Toast.makeText(this, "Not yet implemented.", Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    private void openNewEntry() {
    	Intent intent = new Intent(this, EntryActivity.class);
    	startActivity(intent);
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                case Activity.RESULT_OK :
                    // TODO: retry request
                	Toast.makeText(this, "Resolution now ok, could retry now.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                	Toast.makeText(this, "Resolution failed.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
            	Toast.makeText(this, "Unknown activity result.", Toast.LENGTH_SHORT).show();
        }
     }
    
    // should be called before any request
    private boolean servicesConnected() {
        // check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        if (ConnectionResult.SUCCESS != resultCode) {
        	// Google Play services was not available for some reason
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            if (errorDialog != null) {
                errorDialog.show();
            }
            return false;
        }
        
        return true;
    }

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
        // if Google Play can help the user to resolve the problem
        if (connectionResult.hasResolution()) {
            try {
                // start activity for error resolution and ask for activity result (onActivityResult)
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
            	// Thrown if Google Play services canceled the original PendingIntent
                e.printStackTrace();
            }
        } else {
            // if no resolution is available, display an error dialog
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            if (errorDialog != null) {
            	errorDialog.show();
            }
        }
	}

	@Override
	public void onConnected(Bundle bundle) {
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		
		// one-shot update
		updateLocations(mLocationClient.getLastLocation());
		
		// request periodic updates
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLocationChanged(Location location) {
		String msg = "Location has been updated: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        
		updateLocations(location);
	}
	
	protected void updateLocations(Location location) {
        // display location as latitude/longitude
		if (location != null) {
	        double lat = location.getLatitude();
	 		double lng = location.getLongitude();
	 		
			DecimalFormat fmtLatLng = new DecimalFormat("0.00000");
	 		String strLatLng = fmtLatLng.format(lat) + "," + fmtLatLng.format(lng);
	 		textLatLng.setText(strLatLng);
		} else {
			textLatLng.setText(R.string.latlng_unknown);
		}
		
		// remember last position
		lastLocation = location;
		
		// force list view to update
		((BaseAdapter) listLocations.getAdapter()).notifyDataSetChanged();
	}



}
