package net.datag.pointsofaction;

import net.datag.pointsofaction.LocationEntryContract.LocationEntry;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
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

	private final static int ENTRY_ACTION_REQUEST = 100;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 1000;
    private static final float SMALLEST_DISPLACEMENT = 1.0f;
    
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
    
	private TextView textLatLng;
	private ListView listLocations;
	private Location lastLocation;
	
	SimpleCursorAdapter mAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textLatLng = (TextView) findViewById(R.id.text_latlng);
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
	
	protected Cursor queryEntries() {
		LocationEntryDbHelper dbHelper = new LocationEntryDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();


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
	     
	     return c;
	}
	
	protected void initListView() {
         Cursor c = queryEntries();
	     
	     // add to list-view
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
	 			if (lastLocation != null) {
	 				Location dest = Utilities.lonLat2Location(cursor.getDouble(columnIndexLat), cursor.getDouble(columnIndexLng));
	 				
	 				float d = lastLocation.distanceTo(dest);
	 				strInfo = Utilities.formatDistance(d);
	 			} else {
	 				strInfo = "?";
	 			}
	 			viewText2.setText(strInfo);
			}
			
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				View view = inflater.inflate(layout, parent, false);
				
				// set tag
				int id = cursor.getInt(cursor.getColumnIndexOrThrow(LocationEntry._ID));
				view.setTag(Integer.valueOf(id));
				
				view.setTag(R.id.view_listitem_text1, view.findViewById(R.id.view_listitem_text1));
				view.setTag(R.id.view_listitem_text2, view.findViewById(R.id.view_listitem_text2));
				
				bindView(view, context, cursor);
				
				return view;
			} 
	     };
		 
	     listLocations.setAdapter(new EntryCursorAdapter(this, c));
	     
	     registerForContextMenu(listLocations);
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			openEntry(null);
			return true;
		case R.id.action_settings:
			Toast.makeText(this, "TODO: settings.", Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
    
    private void openEntry(Integer id) {
    	Intent intent = new Intent(this, EntryActivity.class);
    	Bundle extra = new Bundle();
    	
    	if (id == null) {
    		extra.putBoolean("create", true);
        	extra.putBoolean("useLocation", lastLocation != null);
        	if (lastLocation != null) {
        		extra.putDouble("latitude", lastLocation.getLatitude());
        		extra.putDouble("longitude", lastLocation.getLongitude());
        	}
    	} else {
    		extra.putBoolean("create", false);
    		extra.putInt("id", id);
    	}
    	
    	intent.putExtra(EntryActivity.EXTRA_ENTRY_DETAILS, extra);
    	startActivityForResult(intent, ENTRY_ACTION_REQUEST);
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            	if (resultCode == RESULT_OK) {
                	Toast.makeText(this, "Resolution now ok, could retry now.", Toast.LENGTH_SHORT).show();
            	} else {
                	Toast.makeText(this, "Resolution failed.", Toast.LENGTH_SHORT).show();
                }
                break;
            case ENTRY_ACTION_REQUEST:
            	if (resultCode == RESULT_OK) {
            		System.out.println("entry action; resultCode=" + resultCode);
            		
            		// refresh
            		Cursor c = queryEntries();
            		((CursorAdapter) listLocations.getAdapter()).changeCursor(c);
            	}
            	break;
            default:
            	Toast.makeText(this, "Unknown activity result.", Toast.LENGTH_SHORT).show();
        }
     }
    
//    // should be called before any request
//    private boolean servicesConnected() {
//        // check that Google Play services is available
//        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        
//        if (ConnectionResult.SUCCESS != resultCode) {
//        	// Google Play services was not available for some reason
//            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
//            if (errorDialog != null) {
//                errorDialog.show();
//            }
//            return false;
//        }
//        
//        return true;
//    }

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
		System.out.println("Location has been updated: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude()));
        
		updateLocations(location);
	}
	
	protected void updateLocations(Location location) {
        // display location as latitude/longitude
		if (location != null) {
	 		textLatLng.setText(Utilities.formatLatitudeLongitude(location));
		} else {
			textLatLng.setText(R.string.latlng_unknown);
		}
		
		// remember last position
		lastLocation = location;
		
		// force list view to update
		((CursorAdapter) listLocations.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.action_edit:
	        	openEntry((int) info.id);
	            return true;
	        case R.id.action_delete:
	        	Toast.makeText(this, "TODO: delete item #" + info.id, Toast.LENGTH_SHORT).show();
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
}
