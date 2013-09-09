package net.datag.pointsofaction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
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
	
    public static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 1000;
    
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
    
	private TextView textLatLng;
	private TextView textInfo;
	private List<Location> locations = new ArrayList<Location>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textLatLng = (TextView) findViewById(R.id.text_latlng);
		textInfo = (TextView) findViewById(R.id.text_info);
		
		// configure location request
		mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        
        // initialize location client
        mLocationClient = new LocationClient(this, this, this);
        
        ///////////////////////////////////
        Location locationTest;
        String provider = "app";

        // Parent home
        locationTest = new Location(provider);
        locationTest.setLatitude(47.99843);
        locationTest.setLongitude(10.78052);
        locations.add(locationTest);

        // Work
        locationTest = new Location(provider);
        locationTest.setLatitude(48.03301);
        locationTest.setLongitude(10.73231);
        locations.add(locationTest);
        ///////////////////////////////////
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		// reset UI
		textLatLng.setText(R.string.latlng_unknown);
		textInfo.setText("");
		
		// connect location client on start
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		// unregister updates for location, if connected
		if (mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(this);
		}
		
		// disconnect the location client
		mLocationClient.disconnect();
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
        
        
        // display location as latitude/longitude
        double lat = location.getLatitude();
 		double lng = location.getLongitude();
 		
		DecimalFormat fmt = new DecimalFormat("0.00000");
 		String strLatLng = fmt.format(lat) + "," + fmt.format(lng);
 		textLatLng.setText(strLatLng);

		///////////////////////////////////
		String strInfo = "";
		for (Location l: locations) {
			float d = location.distanceTo(l);
			
			if (d < 1000) {
				strInfo += Math.round(d) + " m\n";
			} else {
				strInfo += fmt.format(d / 1000) + " km\n";
			}
		}
		textInfo.setText(strInfo);
	}
}
