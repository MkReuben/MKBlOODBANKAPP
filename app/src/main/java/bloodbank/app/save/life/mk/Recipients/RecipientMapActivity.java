package bloodbank.app.save.life.mk.Recipients;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import bloodbank.app.save.life.mk.MainActivity;
import bloodbank.app.save.life.mk.R;

public class RecipientMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private Button logoutRecipientBtn,settingsRecipientBtn,mRequest;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private LatLng donationLocation;

    private Boolean requestBol = false;
     private  Marker donationMarker;

    private String donorID,recipientId="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient_map);

        mAuth =FirebaseAuth.getInstance();
        currentUser =mAuth.getCurrentUser();
        donorID= mAuth.getCurrentUser().getUid();

        logoutRecipientBtn =(Button) findViewById(R.id.recipient_logout_btn);
        mRequest=(Button)findViewById(R.id.calldonor);
//        settingsDonorBtn=(Button) findViewById(R.id.donors_settings_btn);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        logoutRecipientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(RecipientMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;

            }
        });
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {


                if (requestBol)
                {
                    requestBol =false;
                    geoQuery.removeAllListeners();
                    donorLocationRef.removeEventListener(donorLocationListener);

                    if (donorFoundId !=null)
                    {
                        DatabaseReference donorRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Donors").child(donorFoundId);
                        donorRef.setValue(true);
                        donorFoundId = null;

                    }

                    donorFound = false;
                    radius =1;

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RecipientRequest");

                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                        }
                    });

                    if (donationMarker !=null)
                    {
                        donationMarker.remove();
                    }
                    mRequest.setText("Call Donor");



                }
                else
                {
                    requestBol = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RecipientRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error!=null)
                            {
                                Toast.makeText(RecipientMapActivity.this,"Can't go Active",Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(RecipientMapActivity.this,"You are Active",Toast.LENGTH_SHORT).show();
                        }
                    });

                    donationLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    donationMarker= mMap.addMarker(new MarkerOptions().position(donationLocation).title("Donation Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_heart)));

                    mRequest.setText("Getting your donor...");

                    getClosestDonor();

                }


            }
        });


    }

    private int radius = 1;
    private Boolean donorFound = false;
    private String donorFoundId;
    GeoQuery geoQuery;

    private void getClosestDonor()
    {
    DatabaseReference donorLocation = FirebaseDatabase.getInstance().getReference().child("DonorsAvailable");
    GeoFire geoFire = new GeoFire(donorLocation);

    geoQuery = geoFire.queryAtLocation(new GeoLocation(donationLocation.latitude,donationLocation.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            //Anytime the nearest donor onKeyEntered  method  is called
            public void onKeyEntered(String key, GeoLocation location)
            {
                if (!donorFound && requestBol)
                {
                    donorFound = true;
                    donorFoundId=key;

                    DatabaseReference donorRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Donors").child(donorFoundId);
                    String recipientId=FirebaseAuth.getInstance().getCurrentUser().getUid();

                    HashMap map = new HashMap();
                    map.put("recipientDonationID",recipientId);
                    donorRef.updateChildren(map);

                    getDonorLocation();
                    mRequest.setText("Looking for Donor Location");
                }

            }

            @Override
            public void onKeyExited(String key)
            {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location)
            {

            }

            @Override
            public void onGeoQueryReady()
            {

                if (!donorFound)
                {
                    radius++;
                    getClosestDonor();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error)
            {

            }
        });
    }

    private    DatabaseReference donorLocationRef;
    Marker mDonationMarker;
    private ValueEventListener donorLocationListener;
    private void getDonorLocation()
    {

         donorLocationRef = FirebaseDatabase.getInstance().getReference().child("DonorsActive").child(donorFoundId).child("l");
        donorLocationListener=  donorLocationRef.addValueEventListener(new ValueEventListener() {

            //Every time the location changes this function is called
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()&& requestBol)
                {
                    List<Object> map = (List<Object>)dataSnapshot.getValue();
                    double locationlat=0;
                    double locationlng = 0;

                    mRequest.setText("Donor Found");

                    if (map.get(0) != null)

                    {
                        locationlat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null)
                    {
                        locationlng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng donorLatLng = new LatLng(locationlat,locationlng);
                    if (mDonationMarker != null)
                    {
                        mDonationMarker.remove();
                    }

                    Location loc1 = new Location("");
                    loc1.setLatitude(donationLocation.latitude);
                    loc1.setLongitude(donationLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(donorLatLng.latitude);
                    loc2.setLongitude(donorLatLng.longitude);

                    float distance =loc1.distanceTo(loc2);

                    if (distance<100) {
                        mRequest.setText("Donor has arrived...");
                    }
                    else
                    {
                        mRequest.setText("Donor Found: " + String.valueOf(distance)+" metres away");
                    }


                    mDonationMarker = mMap.addMarker(new MarkerOptions().position(donorLatLng).title("Your Donor").icon(BitmapDescriptorFactory.fromResource(R.mipmap.donor)));

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApi();

        mMap.setMyLocationEnabled(true);




    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);


    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location)
    {

        if (getApplicationContext() !=null) {

            lastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        }}








    protected synchronized  void buildGoogleApi()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();


    }



}
