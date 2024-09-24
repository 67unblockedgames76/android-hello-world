package com.sqisland.android.hello;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import okhttp3.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LocationWorker extends Worker {

    private static final String TAG = "LocationWorker";
    private static final String CLOUD_WORKER_URL = "https://gps-tracking-open-street-map.boysofts.workers.dev/"; // Replace with your Cloudflare Worker URL

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get the location manager
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        try {
            // Check if GPS provider is enabled and get the last known location
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    sendLocationToServer(latitude, longitude);
                } else {
                    Log.e(TAG, "Location is null");
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission is not granted", e);
            return Result.failure();
        }

        return Result.success();
    }

    // Send the location data to the Cloudflare Worker server
    private void sendLocationToServer(double latitude, double longitude) {
        OkHttpClient client = new OkHttpClient();

        // Create JSON object to send to the server
        JSONObject locationData = new JSONObject();
        try {
            locationData.put("id", "driver123"); // Driver unique ID (replace with actual dynamic ID if needed)
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create JSON", e);
            return;
        }

        // Send the data as a POST request
        RequestBody body = RequestBody.create(locationData.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(CLOUD_WORKER_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to send location", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Location sent successfully");
                } else {
                    Log.e(TAG, "Failed to send location, server returned: " + response.code());
                }
            }
        });
    }
}
