package com.example.location;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;



public class MainActivity extends FragmentActivity implements ConnectionCallbacks,OnAddGeofencesResultListener,OnConnectionFailedListener {

	/*
	 * Use to set an expiration time for a geofence. After this amount
	 * of time Location Services will stop tracking the geofence.
	 */
	private static final long SECONDS_PER_HOUR = 60;
	private static final long MILLISECONDS_PER_SECOND = 1000;
	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
	private static final long GEOFENCE_EXPIRATION_TIME =
			GEOFENCE_EXPIRATION_IN_HOURS *
			SECONDS_PER_HOUR *
			MILLISECONDS_PER_SECOND;

	/*
	 * Handles to UI views containing geofence data
	 */
	// Handle to geofence 1 latitude in the UI
	private EditText mLatitude1;
	// Handle to geofence 1 longitude in the UI
	private EditText mLongitude1;
	// Handle to geofence 1 radius in the UI
	private EditText mRadius1;
	// Handle to geofence 2 latitude in the UI
	private EditText mLatitude2;
	// Handle to geofence 2 longitude in the UI
	private EditText mLongitude2;
	// Handle to geofence 2 radius in the UI
	private EditText mRadius2;
	/*
	 * Internal geofence objects for geofence 1 and 2
	 */
	private SimpleGeofence mUIGeofence1;
	private SimpleGeofence mUIGeofence2;

	// Internal List of Geofence objects
	List<Geofence> mGeofenceList;
	// Persistent storage for geofences
	private SimpleGeofenceStore mGeofenceStorage;

	PendingIntent mTransitionPendingIntent;
	// Holds the location client
	private LocationClient mLocationClient;
	// Stores the PendingIntent used to request geofence monitoring
	private PendingIntent mGeofenceRequestIntent;
	// Defines the allowable request types.
	public enum REQUEST_TYPE {ADD};
	private REQUEST_TYPE mRequestType;
	// Flag that indicates if a request is underway.
	private boolean mInProgress;
	// Global constants
	/*
	 * Define a request code to send to Google Play services
	 * This code is returned in Activity.onActivityResult
	 */
	private final static int
	CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Start with the request flag set to false
		mInProgress = false;
		// Instantiate the current List of geofences
		mGeofenceList = new ArrayList<Geofence>();

		// Instantiate a new geofence storage area
		mGeofenceStorage = new SimpleGeofenceStore(this);
		createGeofences();
		
		addGeofences();


	}

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}

	}

	/*
	 * Handle results returned to the FragmentActivity
	 * by Google Play services
	 */
	@Override
	protected void onActivityResult(
			int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {
		case CONNECTION_FAILURE_RESOLUTION_REQUEST :
			/*
			 * If the result code is Activity.RESULT_OK, try
			 * to connect again
			 */
			switch (resultCode) {

			case Activity.RESULT_OK :
				/*
				 * Try the request again
				 */

				break;
			}

		}

	}
	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode =
				GooglePlayServicesUtil.
				isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Geofence Detection",
					"Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} 
		return false;
		//		else {
		//			// Get the error code
		//			int errorCode = connectionResult.getErrorCode();
		//			// Get the error dialog from Google Play services
		//			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
		//					errorCode,
		//					this,
		//					CONNECTION_FAILURE_RESOLUTION_REQUEST);
		//
		//			// If Google Play services can provide an error dialog
		//			if (errorDialog != null) {
		//				// Create a new DialogFragment for the error dialog
		//				ErrorDialogFragment errorFragment =
		//						new ErrorDialogFragment();
		//				// Set the dialog in the DialogFragment
		//				errorFragment.setDialog(errorDialog);
		//				// Show the error dialog in the DialogFragment
		//				errorFragment.show(
		//						getSupportFragmentManager(),
		//						"Geofence Detection");
		//			}
		//		}
	}



	/**
	 * A single Geofence object, defined by its center and radius.
	 */
	public class SimpleGeofence {
		// Instance variables
		private final String mId;
		private final double mLatitude;
		private final double mLongitude;
		private final float mRadius;
		private long mExpirationDuration;
		private int mTransitionType;

		/**
		 * @param geofenceId The Geofence's request ID
		 * @param latitude Latitude of the Geofence's center.
		 * @param longitude Longitude of the Geofence's center.
		 * @param radius Radius of the geofence circle.
		 * @param expiration Geofence expiration duration
		 * @param transition Type of Geofence transition.
		 */
		public SimpleGeofence(
				String geofenceId,
				double latitude,
				double longitude,
				float radius,
				long expiration,
				int transition) {
			// Set the instance fields from the constructor
			this.mId = geofenceId;
			this.mLatitude = latitude;
			this.mLongitude = longitude;
			this.mRadius = radius;
			this.mExpirationDuration = expiration;
			this.mTransitionType = transition;
		}
		// Instance field getters
		public String getId() {
			return mId;
		}
		public double getLatitude() {
			return mLatitude;
		}
		public double getLongitude() {
			return mLongitude;
		}
		public float getRadius() {
			return mRadius;
		}
		public long getExpirationDuration() {
			return mExpirationDuration;
		}
		public int getTransitionType() {
			return mTransitionType;
		}
		/**
		 * Creates a Location Services Geofence object from a
		 * SimpleGeofence.
		 *
		 * @return A Geofence object
		 */
		public Geofence toGeofence() {
			// Build a new Geofence object
			return new Geofence.Builder()
			.setRequestId(getId())
			.setTransitionTypes(mTransitionType)
			.setCircularRegion(
					getLatitude(), getLongitude(), getRadius())
					.setExpirationDuration(mExpirationDuration)
					.build();
		}
	}

	/**
	 * Storage for geofence values, implemented in SharedPreferences.
	 */
	public class SimpleGeofenceStore {
		// Keys for flattened geofences stored in SharedPreferences
		public static final String KEY_LATITUDE =
				"com.example.android.geofence.KEY_LATITUDE";
		public static final String KEY_LONGITUDE =
				"com.example.android.geofence.KEY_LONGITUDE";
		public static final String KEY_RADIUS =
				"com.example.android.geofence.KEY_RADIUS";
		public static final String KEY_EXPIRATION_DURATION =
				"com.example.android.geofence.KEY_EXPIRATION_DURATION";
		public static final String KEY_TRANSITION_TYPE =
				"com.example.android.geofence.KEY_TRANSITION_TYPE";
		// The prefix for flattened geofence keys
		public static final String KEY_PREFIX =
				"com.example.android.geofence.KEY";
		/*
		 * Invalid values, used to test geofence storage when
		 * retrieving geofences
		 */
		public static final long INVALID_LONG_VALUE = -999l;
		public static final float INVALID_FLOAT_VALUE = -999.0f;
		public static final int INVALID_INT_VALUE = -999;
		// The SharedPreferences object in which geofences are stored
		private final SharedPreferences mPrefs;
		// The name of the SharedPreferences
		private static final String SHARED_PREFERENCES =
				"SharedPreferences";
		// Create the SharedPreferences storage with private access only
		public SimpleGeofenceStore(Context context) {
			mPrefs =
					context.getSharedPreferences(
							SHARED_PREFERENCES,
							Context.MODE_PRIVATE);
		}
		/**
		 * Returns a stored geofence by its id, or returns null
		 * if it's not found.
		 *
		 * @param id The ID of a stored geofence
		 * @return A geofence defined by its center and radius. See
		 */
		public SimpleGeofence getGeofence(String id) {
			/*
			 * Get the latitude for the geofence identified by id, or
			 * INVALID_FLOAT_VALUE if it doesn't exist
			 */
			double lat = mPrefs.getFloat(
					getGeofenceFieldKey(id, KEY_LATITUDE),
					INVALID_FLOAT_VALUE);
			/*
			 * Get the longitude for the geofence identified by id, or
			 * INVALID_FLOAT_VALUE if it doesn't exist
			 */
			double lng = mPrefs.getFloat(
					getGeofenceFieldKey(id, KEY_LONGITUDE),
					INVALID_FLOAT_VALUE);
			/*
			 * Get the radius for the geofence identified by id, or
			 * INVALID_FLOAT_VALUE if it doesn't exist
			 */
			float radius = mPrefs.getFloat(
					getGeofenceFieldKey(id, KEY_RADIUS),
					INVALID_FLOAT_VALUE);
			/*
			 * Get the expiration duration for the geofence identified
			 * by id, or INVALID_LONG_VALUE if it doesn't exist
			 */
			long expirationDuration = mPrefs.getLong(
					getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
					INVALID_LONG_VALUE);
			/*
			 * Get the transition type for the geofence identified by
			 * id, or INVALID_INT_VALUE if it doesn't exist
			 */
			int transitionType = mPrefs.getInt(
					getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
					INVALID_INT_VALUE);
			// If none of the values is incorrect, return the object
			if (
					lat != INVALID_FLOAT_VALUE &&
					lng != INVALID_FLOAT_VALUE &&
					radius != INVALID_FLOAT_VALUE &&
					expirationDuration !=
					INVALID_LONG_VALUE &&
					transitionType != INVALID_INT_VALUE) {

				// Return a true Geofence object
				return new SimpleGeofence(
						id, lat, lng, radius, expirationDuration,
						transitionType);
				// Otherwise, return null.
			} else {
				return null;
			}
		}
		/**
		 * Save a geofence.
		 * @param geofence The SimpleGeofence containing the
		 * values you want to save in SharedPreferences
		 */
		public void setGeofence(String id, SimpleGeofence geofence) {
			/*
			 * Get a SharedPreferences editor instance. Among other
			 * things, SharedPreferences ensures that updates are atomic
			 * and non-concurrent
			 */
			Editor editor = mPrefs.edit();
			// Write the Geofence values to SharedPreferences
			editor.putFloat(
					getGeofenceFieldKey(id, KEY_LATITUDE),
					(float) geofence.getLatitude());
			editor.putFloat(
					getGeofenceFieldKey(id, KEY_LONGITUDE),
					(float) geofence.getLongitude());
			editor.putFloat(
					getGeofenceFieldKey(id, KEY_RADIUS),
					geofence.getRadius());
			editor.putLong(
					getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
					geofence.getExpirationDuration());
			editor.putInt(
					getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
					geofence.getTransitionType());
			// Commit the changes
			editor.commit();
		}
		public void clearGeofence(String id) {
			/*
			 * Remove a flattened geofence object from storage by
			 * removing all of its keys
			 */
			Editor editor = mPrefs.edit();
			editor.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
			editor.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
			editor.remove(getGeofenceFieldKey(id, KEY_RADIUS));
			editor.remove(getGeofenceFieldKey(id,
					KEY_EXPIRATION_DURATION));
			editor.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
			editor.commit();
		}
		/**
		 * Given a Geofence object's ID and the name of a field
		 * (for example, KEY_LATITUDE), return the key name of the
		 * object's values in SharedPreferences.
		 *
		 * @param id The ID of a Geofence object
		 * @param fieldName The field represented by the key
		 * @return The full key name of a value in SharedPreferences
		 */
		private String getGeofenceFieldKey(String id,
				String fieldName) {
			return KEY_PREFIX + "_" + id + "_" + fieldName;
		}
	}


	/*
	 * Provide the implementation of
	 * OnAddGeofencesResultListener.onAddGeofencesResult.
	 * Handle the result of adding the geofences
	 *
	 */
	@Override
	public void onAddGeofencesResult(
			int statusCode, String[] geofenceRequestIds) {

		// If adding the geofences was successful
		if (LocationStatusCodes.SUCCESS == statusCode) {
			System.out.println("onAddGeofencesResult");
//			android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setMessage("radius crossed");
//			builder.setCancelable(true);
//			builder.setPositiveButton("OK", null);			    
//			AlertDialog dialog = builder.create();
//			dialog.show();
			/*
			 * Handle successful addition of geofences here.
			 * You can send out a broadcast intent or update the UI.
			 * geofences into the Intent's extended data.
			 */
		} else {
			// If adding the geofences failed
			/*
			 * Report errors here.
			 * You can log the error using Log.e() or update
			 * the UI.
			 */
		}
		// Turn off the in progress flag and disconnect the client
		mInProgress = false;
		mLocationClient.disconnect();
	}


	/*
	 * Provide the implementation of ConnectionCallbacks.onConnected()
	 * Once the connection is available, send a request to add the
	 * Geofences
	 */
	@Override
	public void onConnected(Bundle dataBundle) {

		switch (mRequestType) {
		case ADD :
			// Get the PendingIntent for the request
			mTransitionPendingIntent =
			getTransitionPendingIntent();
			// Send a request to add the current geofences
			mLocationClient.addGeofences(
					mGeofenceList, mTransitionPendingIntent, this);

		}
	}

	public void addGeofences() {
		// Start a request to add geofences
		mRequestType = REQUEST_TYPE.ADD;
		/*
		 * Test for Google Play services after setting the request type.
		 * If Google Play services isn't present, the proper request
		 * can be restarted.
		 */
		if (!servicesConnected()) {
			return;
		}
		System.out.println("connected");
		/*
		 * Create a new location client object. Since the current
		 * activity class implements ConnectionCallbacks and
		 * OnConnectionFailedListener, pass the current activity object
		 * as the listener for both parameters
		 */
		mLocationClient = new LocationClient(this, this, (OnConnectionFailedListener) this);
		// If a request is not already underway
		if (!mInProgress) {
			// Indicate that a request is underway
			mInProgress = true;
			// Request a connection from the client to Location Services
			mLocationClient.connect();
		} else {
			/*
			 * A request is already underway. You can handle
			 * this situation by disconnecting the client,
			 * re-setting the flag, and then re-trying the
			 * request.
			 */
		}
	}



	/**
	 * Get the geofence parameters for each geofence from the UI
	 * and add them to a List.
	 */
	public void createGeofences() {
		//getting lat and long
		Criteria criteria = new Criteria();

		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
		String provider = lm.getBestProvider(criteria, false);

		Location location = lm.getLastKnownLocation(provider);
		double longitude = location.getLongitude(); 
		double latitude = location.getLatitude();
		System.out.println("lat= "+latitude + "long= "+longitude);
		
		/*
		 * Create an internal object to store the data. Set its
		 * ID to "1". This is a "flattened" object that contains
		 * a set of strings
		 */
		mUIGeofence1 = new SimpleGeofence(
				"1",
//				Double.valueOf(mLatitude1.getText().toString()),
//				Double.valueOf(mLongitude1.getText().toString()),
//				Float.valueOf(mRadius1.getText().toString()),
//				GEOFENCE_EXPIRATION_TIME,
//				// This geofence records only entry transitions
//				Geofence.GEOFENCE_TRANSITION_ENTER);
				
				
				
				Double.valueOf(latitude+""),
				Double.valueOf(longitude+""),
				Float.valueOf("5"),
				GEOFENCE_EXPIRATION_TIME,
				// This geofence records only entry transitions
				Geofence.GEOFENCE_TRANSITION_ENTER);
		// Store this flat version
		mGeofenceStorage.setGeofence("1", mUIGeofence1);
		// Create another internal object. Set its ID to "2"
//		mUIGeofence2 = new SimpleGeofence(
//				"2",
//				Double.valueOf(mLatitude2.getText().toString()),
//				Double.valueOf(mLongitude2.getText().toString()),
//				Float.valueOf(mRadius2.getText().toString()),
//				GEOFENCE_EXPIRATION_TIME,
//				// This geofence records both entry and exit transitions
//				Geofence.GEOFENCE_TRANSITION_ENTER |
//				Geofence.GEOFENCE_TRANSITION_EXIT);
		// Store this flat version
		//mGeofenceStorage.setGeofence("2", mUIGeofence2);
		mGeofenceList.add(mUIGeofence1.toGeofence());
//		mGeofenceList.add(mUIGeofence2.toGeofence());
	}

	/*
	 * Create a PendingIntent that triggers an IntentService in your
	 * app when a geofence transition occurs.
	 */
	private PendingIntent getTransitionPendingIntent() {
		// Create an explicit Intent
		Intent intent = new Intent(this,
				ReceiveTransitionsIntentService.class);
		/*
		 * Return the PendingIntent
		 */
		return PendingIntent.getService(
				this,
				0,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		System.out.println("failed");
		
	}

}