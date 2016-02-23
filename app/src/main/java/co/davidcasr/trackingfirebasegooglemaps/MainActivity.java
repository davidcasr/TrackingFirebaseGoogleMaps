package co.davidcasr.trackingfirebasegooglemaps;

import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.Firebase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String TAG = "TrackingFirebase";

    private String FIREBASE_URL = "https://trackingfirebase.firebaseio.com/";
    private String FIREBASE_USER = "user1";

    private static final int SETINTERVAL = 20000;
    private static final int FASTESTINTERVAL = 10000;

    private boolean mRequestingLocationUpdates = false;

    private MenuItem mShareButton;

    // Google API - Locations
    private GoogleApiClient mGoogleApiClient;

    // Google Maps
    private GoogleMap mGoogleMap;
    private PolylineOptions mPolylineOptions;
    private LatLng mLatLng;

    Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start Google Client
        this.buildGoogleApiClient();

        // Start Firebase
        Firebase.setAndroidContext(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mShareButton = menu.findItem(R.id.share_locations);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_locations:
                Log.e(TAG, "'Share Your Location' Button Pressed");
                mRequestingLocationUpdates = !mRequestingLocationUpdates;
                if (mRequestingLocationUpdates) {
                    startSharingLocation();
                    mShareButton.setTitle("Stop Sharing Your Location");
                }
                if (!mRequestingLocationUpdates) {
                    stopSharingLocation();
                    mShareButton.setTitle("Start Sharing Your Location");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
        mGoogleMap.setMyLocationEnabled(true);
        Log.e(TAG, "Map Ready");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "Connected to Google API for Location Management");
        if (mRequestingLocationUpdates) {
            LocationRequest mLocationRequest = createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            initializePolyline();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(TAG, "Connection to Google API suspended" + cause);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "Location Detected" );
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Save in Firebase

        firebase = new Firebase(FIREBASE_URL).child(FIREBASE_USER).child("username");
        firebase.setValue("user1");
        firebase = new Firebase(FIREBASE_URL).child(FIREBASE_USER).child("latitude");
        firebase.setValue(location.getLatitude());
        firebase = new Firebase(FIREBASE_URL).child(FIREBASE_USER).child("longitude");
        firebase.setValue(location.getLongitude());
        firebase = new Firebase(FIREBASE_URL).child(FIREBASE_USER).child("altitude");
        firebase.setValue(location.getAltitude());
        // getAccuracy Get the estimated accuracy of this location, in meters.
        firebase = new Firebase(FIREBASE_URL).child(FIREBASE_USER).child("accuracy");
        firebase.setValue(location.getAccuracy());
        // getBearing Get the bearing, in degrees.
        firebase = new Firebase(FIREBASE_URL).child(FIREBASE_USER).child("bearing");
        firebase.setValue(location.getBearing());
        // getSpeed Get the speed if it is available, in meters/second over ground.
        firebase = new Firebase(FIREBASE_URL).child(FIREBASE_USER).child("speed");
        firebase.setValue(location.getSpeed());
        // getTime() Return the UTC time of this fix, in milliseconds since January 1, 1970.
        firebase = new Firebase(FIREBASE_URL).child(FIREBASE_USER).child("time");
        firebase.setValue(location.getTime());
        firebase = new Firebase(FIREBASE_URL).child(FIREBASE_USER).child("routename");
        firebase.setValue("Route1");

        // Update Map
        updateCamera();
        updatePolyline();
    }

    private LocationRequest createLocationRequest() {
        Log.d(TAG, "Building request");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(SETINTERVAL);
        mLocationRequest.setFastestInterval(FASTESTINTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    public void startSharingLocation() {
        Log.e(TAG, "Starting Location Updates");
        mGoogleApiClient.connect();
    }

    public void stopSharingLocation() {
        Log.e(TAG, "Stop Location Updates & Disconect to Google API");
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    // Map Editing Methods

    private void initializePolyline() {
        mGoogleMap.clear();
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.BLUE).width(10);
        mGoogleMap.addPolyline(mPolylineOptions);
    }

    private void updatePolyline() {
        mPolylineOptions.add(mLatLng);
        mGoogleMap.clear();
        mGoogleMap.addPolyline(mPolylineOptions);
    }

    private void updateCamera() {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
    }
}
