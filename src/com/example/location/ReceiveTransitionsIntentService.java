package com.example.location;

import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class ReceiveTransitionsIntentService extends IntentService {

	/**
	 * Sets an identifier for the service
	 */
	public ReceiveTransitionsIntentService() {
		super("ReceiveTransitionsIntentService");
	}
	/**
	 * Handles incoming intents
	 *@param intent The Intent sent by Location Services. This
	 * Intent is provided
	 * to Location Services (inside a PendingIntent) when you call
	 * addGeofences()
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		
		System.out.println("onHandleIntent");
		android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Barcode Timeout");
		builder.setCancelable(true);
		builder.setPositiveButton("OK", null);			    
		AlertDialog dialog = builder.create();
		dialog.show();
		// First check for errors
		if (LocationClient.hasError(intent)) {
			// Get the error code with a static method
			int errorCode = LocationClient.getErrorCode(intent);
			System.out.println("onHandleIntent" + errorCode);
			// Log the error
			Log.e("ReceiveTransitionsIntentService",
					"Location Services error: " +
							Integer.toString(errorCode));
			/*
			 * You can also send the error code to an Activity or
			 * Fragment with a broadcast Intent
			 */
			/*
			 * If there's no error, get the transition type and the IDs
			 * of the geofence or geofences that triggered the transition
			 */
		} 
		else {
			// Get the type of transition (entry or exit)
			int transitionType =
					LocationClient.getGeofenceTransition(intent);
			// Test that a valid transition was reported
			if (
					(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
					||
					(transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
					) {
				System.out.println("heyyyyyyyyyyyyyyyyyyyyyy");
				android.app.AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setMessage("Barcode Timeout");
				builder2.setCancelable(true);
				builder2.setPositiveButton("OK", null);			    
				AlertDialog dialog2 = builder2.create();
				dialog2.show();

			}
			// An invalid transition was reported
		}
	}

}