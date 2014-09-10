package co.tashawych.where;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

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
	String bikeNote, carNote;

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

		if (prefs.getString("carIcon", "").equals("")) {
			DialogFragment pickCarFragment = new PickCarFragment();
			pickCarFragment.show(getFragmentManager(), "pickCar");
		}
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
	 * Open a dialog to add a note about the bike and/or car's location
	 * @param v
	 */
	public void addNote(View v) {
		bikeMarker.hideInfoWindow();
		carMarker.hideInfoWindow();
		DialogFragment addNoteFragment = new AddNoteFragment();
		addNoteFragment.show(getFragmentManager(), "addNote");
	}

	/**
	 * Place the bike marker at the user's current location
	 * @param v
	 */
	public void placeBike(View v) {
		// Set new location
		bikeLat = map.getMyLocation().getLatitude();
		bikeLng = map.getMyLocation().getLongitude();
		saveDouble("bikeLat", bikeLat);
		saveDouble("bikeLng", bikeLng);
		bikeMarker.setPosition(new LatLng(bikeLat, bikeLng));

		// Add * to note to denote that location has changed
		bikeNote = prefs.getString("bikeNote", "");
		if (!bikeNote.endsWith("*")) {
			saveBikeNote(bikeNote + "*");
		}
		bikeMarker.showInfoWindow();
	}

	/**
	 * Place the car marker at the user's current location
	 * @param v
	 */
	public void placeCar(View v) {
		// Set new location
		carLat = map.getMyLocation().getLatitude();
		carLng = map.getMyLocation().getLongitude();
		saveDouble("carLat", carLat);
		saveDouble("carLng", carLng);
		carMarker.setPosition(new LatLng(carLat, carLng));

		// Add * to note to denote that location has changed
		carNote = prefs.getString("carNote", "");
		if (!carNote.endsWith("*")) {
			saveCarNote(carNote + "*");
		}
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

		bikeNote = prefs.getString("bikeNote", "");
		carNote = prefs.getString("carNote", "");

		// Add a marker to the bike's location
	    if (bikeMarker == null) {
			bikeMarker = map.addMarker(new MarkerOptions()
					.position(new LatLng(bikeLat, bikeLng))
					.title("Bike")
					.snippet(bikeNote)
					.draggable(true)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.bicycle)));
	    }
	    bikeMarker.showInfoWindow();
	    
		// Add a marker to the car's location
		if (carMarker == null) {
			// Check if user selected Scion or Subaru icon
			boolean scion = prefs.getString("carIcon", "scion").equals("scion");
			carMarker = map.addMarker(new MarkerOptions()
					.position(new LatLng(carLat, carLng))
					.title("Car")
					.snippet(carNote)
					.draggable(true)
					.icon(BitmapDescriptorFactory.fromResource((scion) ? R.drawable.car : R.drawable.subaru)));
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

	public void saveBikeNote(String note) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString("bikeNote", note);
		edit.commit();
		bikeMarker.setSnippet(note);
	}

	public void saveCarNote(String note) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString("carNote", note);
		edit.commit();
		carMarker.setSnippet(note);
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

	public class AddNoteFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    // Get the layout inflater
		    LayoutInflater inflater = getActivity().getLayoutInflater();
		    // Inflate and set the layout for the dialog
		    // Pass null as the parent view because its going in the dialog layout
		    final View view = inflater.inflate(R.layout.dialog_add_note, null);

		    final EditText edit_bike_note = (EditText) view.findViewById(R.id.edit_bike_note);
			final EditText edit_car_note = (EditText) view.findViewById(R.id.edit_car_note);
			edit_bike_note.setText(prefs.getString("bikeNote", ""));
			edit_car_note.setText(prefs.getString("carNote", ""));

			builder.setView(view)
					.setPositiveButton(R.string.submit,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// Save changes to marker snippets
									saveBikeNote(edit_bike_note.getText().toString());
									saveCarNote(edit_car_note.getText().toString());
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// User cancelled the dialog
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

	public class PickCarFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();
			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog layout
			final View view = inflater.inflate(R.layout.dialog_pick_car, null);

			final ImageView car_scion = (ImageView) view.findViewById(R.id.car_scion);
			final ImageView car_subaru = (ImageView) view.findViewById(R.id.car_subaru);

			car_scion.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					prefs.edit().putString("carIcon", "scion").commit();
					carMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
					getDialog().dismiss();
				}
			});

			car_subaru.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					prefs.edit().putString("carIcon", "subaru").commit();
					carMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.subaru));
					getDialog().dismiss();
				}
			});

			builder.setView(view);
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

}
