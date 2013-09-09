package net.datag.pointsofaction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.datag.pointsofaction.LocationEntryContract.LocationEntry;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
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
	private TextView textInfo;
	private ListView listLocations;
	
	SimpleCursorAdapter mAdapter;
	private List<Location> locations = new ArrayList<Location>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textLatLng = (TextView) findViewById(R.id.text_latlng);
		textInfo = (TextView) findViewById(R.id.text_info);
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
	    		 LocationEntry.COLUMN_NAME_NAME + " DESC";
	
	     Cursor c = db.query(
	         LocationEntry.TABLE_NAME,  // The table to query
	         projection,                               // The columns to return
	         null /*selection*/,                                // The columns for the WHERE clause
	         null /*selectionArgs*/,                            // The values for the WHERE clause
	         null,                                     // don't group the rows
	         null,                                     // don't filter by row groups
	         sortOrder                                 // The sort order
	         );
        
	  
	     
	     SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.view_listitem,
	    		 	c, new String[] {LocationEntry.COLUMN_NAME_NAME, LocationEntry.COLUMN_NAME_LATITUDE}, new int[] { R.id.view_listitem_text1, R.id.view_listitem_text2 }, 0);

	    	
	     listLocations.setAdapter(adapter);
	}
	
	public void doTest(View view) {
		class Entry {
			public String name;
			public double lat;
			public double lng;
			
			public Entry(String name, double lat, double lng) {
				this.name = name;
				this.lat = lat;
				this.lng = lng;
			}
		};
		
		Entry[] entries = {
			new Entry("Home", 48.03504, 10.73137),
			new Entry("Parents", 47.99843, 10.78052),
			new Entry("Work", 48.03301, 10.73231)
		};
		
		try {
			LocationEntryDbHelper mDbHelper = new LocationEntryDbHelper(this);
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			
			for (Entry entry: entries) {
				// Create a new map of values, where column names are the keys
				ContentValues values = new ContentValues();
				values.put(LocationEntry.COLUMN_NAME_NAME, entry.name);
				values.put(LocationEntry.COLUMN_NAME_LATITUDE, entry.lat);
				values.put(LocationEntry.COLUMN_NAME_LONGITUDE, entry.lng);
				
				// Insert the new row, returning the primary key value of the new row
				long newRowId = db.insert(LocationEntry.TABLE_NAME, null, values);
			}
        
		} catch (Exception e) {
        	Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        	System.out.println(e.getMessage());
        }
	}

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
	 		//strLatLng += "\n(" + location.getProvider() + ")";
	 		textLatLng.setText(strLatLng);
		} else {
			textLatLng.setText(R.string.latlng_unknown);
		}
		
		///////////////////////////////////
		String strInfo = "";
		DecimalFormat fmtKm = new DecimalFormat("0.00");
		for (Location l: locations) {
			if (location != null) {
				float d = location.distanceTo(l);
				
				if (d < 1000) {
					strInfo += Math.round(d) + " m \n";
				} else {
					strInfo += fmtKm.format(d / 1000) + " km\n";
				}
			} else {
				strInfo += "?\n";
			}
		}
		textInfo.setText(strInfo);
	}



}
