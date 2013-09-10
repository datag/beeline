package net.datag.beeline;

import java.text.DecimalFormat;
import java.text.ParseException;

import android.annotation.SuppressLint;
import android.location.Location;

@SuppressLint("DefaultLocale")
public class Utilities {
	private Utilities() {}
	
	public static String formatLatitudeLongitude(Location location) {
		double latitude = location.getLatitude();
 		double longitude = location.getLongitude();
 		
 		return Utilities.formatLatLon(latitude, longitude);
	}
	
	// TODO: format degree/minutes/seconds + direction, e.g. 51° 28' 38" N
	public static String formatLatLon(double latitude, double longitude) {
 		return String.format("lat (φ): % 10.5f" + "%n" + "lon (λ): % 10.5f",
 				latitude, longitude);
	}
	
	// TODO: switch for meters/miles
	public static String formatDistance(float distance) {
		if (distance < 1000) {
			return String.format("%d  m", Math.round(distance));
		} else {
			return String.format("%.2f km", distance / 1000);
		}
	}
	
	public static String geoCoordForInput(Location location, boolean useLatitude) {
		if (location == null) {
			return "";
		}
		
		return Utilities.geoCoordForInput((useLatitude == true) ? location.getLatitude() : location.getLongitude());
	}
	
	public static String geoCoordForInput(double value) {
		return String.format("%.5f", value);
	}
	
	public static Location lonLat2Location(double latitude, double longitude) {
		Location l = new Location("app");	// fake provider name
		l.setLatitude(latitude);
		l.setLongitude(longitude);
		return l;
	}
	
	public static double parseGeoCoord(String str) throws ParseException {
		DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
		return df.parse(str).doubleValue();
	}
}
