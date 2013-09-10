package net.datag.pointsofaction;

import java.text.DecimalFormat;
import java.text.ParseException;

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
	
	private Integer idEntry;
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
		
		double lat = .0;
		double lng = .0;
		
		if (bundle.getBoolean("create") == true) {
			// new entry
			if (bundle.getBoolean("useLocation") == true) {
				lat = bundle.getDouble("latitude");
				lng = bundle.getDouble("longitude");
			}
		} else {
			// edit entry
			idEntry = bundle.getInt("id");
			
			LocationEntryDbHelper dbHelper = new LocationEntryDbHelper(this);
			Entry entry = dbHelper.find(idEntry);
			
			editName.setText(entry.name);
			lat = entry.latitude;
			lng = entry.longitude;
		}
		
		editLatitude.setText(Utilities.geoCoordForInput(lat));
		editLongitude.setText(Utilities.geoCoordForInput(lng));
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.entry, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// navigate one level up
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
		double lat = .0;
		double lng = .0;
		
		try {
			lat = Utilities.parseGeoCoord(editLatitude.getText().toString());
			lng = Utilities.parseGeoCoord(editLongitude.getText().toString());
		} catch (ParseException e) {
			Toast.makeText(this, getResources().getText(R.string.error_latlng_invalid) + " " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			return false;
		}
		
		try {
			LocationEntryDbHelper dbHelper = new LocationEntryDbHelper(this);
			Entry entry = dbHelper.new Entry(idEntry, name, lat, lng);
			
			if (entry.name.trim().isEmpty()) {
				entry.name = String.format("%s (%.2f/%.2f)", getResources().getText(R.string.location), lat, lng);
			}
			
			dbHelper.save(entry);
		} catch (Exception e) {
			Toast.makeText(this, getResources().getText(R.string.error_saving_entry) + " " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
		
		return true;
	}

}
