package co.tashawych.where;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, 
	GooglePlayServicesClient.OnConnectionFailedListener {
	
	GoogleMap map;
	Marker bikeMarker;
	Marker carMarker;
	
	LocationClient locationClient;
	Location currentLocation;
	
	SharedPreferences prefs;
	double bikeLat, bikeLng, carLat, carLng;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		prefs = getPreferences(0);
		
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);
		
		locationClient = new LocationClient(this, this, this);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		locationClient.connect();
	}
	
	@Override
	public void onStop() {
		locationClient.disconnect();
		super.onStop();
	}
	
	/**
	 * Center the camera on the bike
	 * @param v
	 */
	public void findBike(View v) {
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(bikeLat, bikeLng), 16));
		bikeMarker.showInfoWindow();
	}
	
	/**
	 * Center the camera on the car
	 * @param v
	 */
	public void findCar(View v) {
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(carLat, carLng), 16));
		carMarker.showInfoWindow();
	}
	
	/**
	 * Place the bike marker at the user's current location
	 * @param v
	 */
	public void placeBike(View v) {
		bikeLat = map.getMyLocation().getLatitude();
		bikeLng = map.getMyLocation().getLongitude();
		saveDouble("bikeLat", bikeLat);
		saveDouble("bikeLng", bikeLng);
		bikeMarker.setPosition(new LatLng(bikeLat, bikeLng));
		bikeMarker.showInfoWindow();
	}

	/**
	 * Place the car marker at the user's current location
	 * @param v
	 */
	public void placeCar(View v) {
		carLat = map.getMyLocation().getLatitude();
		carLng = map.getMyLocation().getLongitude();
		saveDouble("carLat", carLat);
		saveDouble("carLng", carLng);
		carMarker.setPosition(new LatLng(carLat, carLng));
		carMarker.showInfoWindow();
	}

	public void mapSetup() {
	    final double latitude = currentLocation.getLatitude();
	    final double longitude = currentLocation.getLongitude();
	    
	    // Show the current location on the Google Map
	    LatLng latLng = new LatLng(latitude, longitude);     
	    map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
	    
	    map.setOnMarkerDragListener(getOnMarkerDragListener());
	    
		bikeLat = getDouble("bikeLat", latitude + 0.001);
		bikeLng = getDouble("bikeLng", longitude);
		carLat = getDouble("carLat", latitude);
		carLng = getDouble("carLng", longitude + 0.001);

	    // Add a marker to the bike's location
	    if (bikeMarker == null) {
	    	bikeMarker = map.addMarker(new MarkerOptions()
	    		.position(new LatLng(bikeLat, bikeLng))
	    		.title("Bike")
	    		.snippet("Arlington Apartments")
	    		.draggable(true)
	    		.icon(BitmapDescriptorFactory.fromResource(R.drawable.bicycle)));
	    }
	    bikeMarker.showInfoWindow();
	    
	    // Add a marker to the car's location
	    if (carMarker == null) {
	    	carMarker = map.addMarker(new MarkerOptions()
	    		.position(new LatLng(carLat, carLng))
	    		.title("Car")
	    		.snippet("The Palms")
	    		.draggable(true)
	    		.icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
	    }
	}
	
	public GoogleMap.OnMarkerDragListener getOnMarkerDragListener() {
		return new GoogleMap.OnMarkerDragListener() {
			
			@Override
			public void onMarkerDragStart(Marker marker) {
				
			}
			
			@Override
			public void onMarkerDragEnd(Marker marker) {
				if (marker.equals(bikeMarker)) {
					bikeLat = marker.getPosition().latitude;
					bikeLng = marker.getPosition().longitude;
					saveDouble("bikeLat", bikeLat);
					saveDouble("bikeLng", bikeLng);
				}
				else if (marker.equals(carMarker)) {
					carLat = marker.getPosition().latitude;
					carLng = marker.getPosition().longitude;
					saveDouble("carLat", carLat);
					saveDouble("carLng", carLng);
				}
			}
			
			@Override
			public void onMarkerDrag(Marker marker) {
				
			}
		};
	}
	
	public double getDouble(String name, double defaultValue) {
		return Double.longBitsToDouble(prefs.getLong(name, Double.doubleToLongBits(defaultValue)));
	}
	
	public void saveDouble(String name, double value) {
		prefs.edit().putLong(name, Double.doubleToLongBits(value)).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
	
	@Override
	public void onConnected(Bundle connectionHint) {
		currentLocation = locationClient.getLastLocation();
		mapSetup();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		
	}

	@Override
	public void onDisconnected() {
		
	}

}
