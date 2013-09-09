package net.datag.pointsofaction;

import java.text.DecimalFormat;

import net.datag.pointsofaction.LocationEntryDbHelper.Entry;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


public class EntryActivity extends Activity {
	public final static String EXTRA_ENTRY_DETAILS = "net.datag.pointsofaction.EntryDetails";
	
	private int id;
	private EditText editName;
	private EditText editLatitude;
	private EditText editLongitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry);
		// Show the Up button in the action bar.
		setupActionBar();
		
		editName = (EditText) findViewById(R.id.edit_name);
		editLatitude = (EditText) findViewById(R.id.edit_latitude);
		editLongitude = (EditText) findViewById(R.id.edit_longitude);
		
		
		// extract intent data
		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra(EXTRA_ENTRY_DETAILS);
		
		DecimalFormat fmtLatLng = new DecimalFormat("0.00000");
		
		if (bundle.getBoolean("create") == true) {
			id = 0;
			
			if (bundle.getBoolean("useLocation")) {
				editLatitude.setText(fmtLatLng.format(bundle.getDouble("latitude")));
				editLongitude.setText(fmtLatLng.format(bundle.getDouble("longitude")));
			}
		} else {
			id = bundle.getInt("id");
			
			LocationEntryDbHelper dbHelper = new LocationEntryDbHelper(this);
			Entry entry = dbHelper.find(id);
			
			editName.setText(entry.name);
			editLatitude.setText(fmtLatLng.format(entry.latitude));
			editLongitude.setText(fmtLatLng.format(entry.longitude));
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.entry, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_save:
			if (save() == true) {
				setResult(RESULT_OK);
				finish();				
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected boolean save() {
		String name = editName.getText().toString();
		double lat = Double.valueOf(editLatitude.getText().toString());
		double lng = Double.valueOf(editLongitude.getText().toString());
		
		LocationEntryDbHelper dbHelper = new LocationEntryDbHelper(this);
		Entry entry = dbHelper.new Entry(id, name, lat, lng);
		
		if (entry.name.trim().isEmpty()) {
			Toast.makeText(this, "No name given.", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if (dbHelper.save(entry) != true) {
			Toast.makeText(this, "Error saving to database.", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}

}
