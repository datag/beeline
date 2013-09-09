package net.datag.pointsofaction;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class EntryActivity extends Activity {
	public final static String EXTRA_ENTRY_DETAILS = "net.datag.pointsofaction.EntryDetails";
	
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
		
		if (bundle.getBoolean("create") == true) {
			if (bundle.getBoolean("useLocation")) {
				DecimalFormat fmtLatLng = new DecimalFormat("0.00000");
				editLatitude.setText(fmtLatLng.format(bundle.getDouble("latitude")));
				editLongitude.setText(fmtLatLng.format(bundle.getDouble("longitude")));
			}
		} else {
			editName.setText("EDIT");
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
			Bundle bundle = new Bundle();
			bundle.putString("foo", "bar");
			
			Intent intent = new Intent();
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
