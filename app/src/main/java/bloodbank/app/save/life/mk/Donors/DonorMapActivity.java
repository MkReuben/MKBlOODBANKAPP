package bloodbank.app.save.life.mk.Donors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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

import java.util.List;
import java.util.Map;

import bloodbank.app.save.life.mk.MainActivity;
import bloodbank.app.save.life.mk.R;


public class DonorMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private Button logoutDonorBtn,settingsDonorBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private  Boolean currentLogoutDriverStatus = false;

    private DatabaseReference assignedRecipientRef,AssignedRecipientDonationRef;
    private String donorID,recipientId="";

    Marker DonationMarker;
    private ValueEventListener AssignedRecipientDonationReListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_map);

        mAuth =FirebaseAuth.getInstance();
        currentUser =mAuth.getCurrentUser();
        donorID= mAuth.getCurrentUser().getUid();

        logoutDonorBtn =(Button) findViewById(R.id.donors_logout_btn);
//        settingsDonorBtn=(Button) findViewById(R.id.donors_settings_btn);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        logoutDonorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(DonorMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;

            }
        });

        getAssignedRecipient();


    }

    private void getAssignedRecipient()
    {

        String donorID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedRecipientRef= FirebaseDatabase.getInstance().getReference().child("Users").child("Donors").child(donorID).child("recipientDonationID");
        assignedRecipientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {

                        recipientId = dataSnapshot.getValue().toString();
                        getAssignedRecipientDonationLocation();

                }
                else
                {
                    recipientId = "";

                    if (donationMarker !=null)
                    {
                        donationMarker.remove();

                    }

                    if (assignedRecipientDonationLocationRefListener != null) {
                        assignedRecipientDonationLocationRef.removeEventListener(assignedRecipientDonationLocationRefListener);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    Marker donationMarker;
    private ValueEventListener assignedRecipientDonationLocationRefListener;
    private DatabaseReference   assignedRecipientDonationLocationRef;
    private void getAssignedRecipientDonationLocation()
    {

        assignedRecipientDonationLocationRef= FirebaseDatabase.getInstance().getReference().child("RecipientRequest").child(recipientId).child("l");
        assignedRecipientDonationLocationRefListener=  assignedRecipientDonationLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists() && !recipientId.equals(""))
                {
                    List<Object> map =map = (List<Object>)dataSnapshot.getValue();
                    double locationlat=0;
                    double locationlng = 0;

                    if (map.get(0) != null)
                    {
                        locationlat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null)
                    {
                        locationlng = Double.parseDouble(map.get(1).toString()); }
                    LatLng donorLatLng = new LatLng(locationlat,locationlng);

                    donationMarker = mMap.addMarker(new MarkerOptions().position(donorLatLng).title("Donation Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.heart)));


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


            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference DonorAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("DonorsAvailable");
            DatabaseReference DonorsActiveRef= FirebaseDatabase.getInstance().getReference().child("DonorsActive");

            GeoFire geoFireAvailability = new GeoFire(DonorAvailabilityRef);

            GeoFire geoFireActive = new GeoFire(DonorsActiveRef);




            geoFireActive.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error!=null)
                    {
                        Toast.makeText(DonorMapActivity.this,"Can't go Active",Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(DonorMapActivity.this,"You are Active",Toast.LENGTH_SHORT).show();
                }
            });


            switch (recipientId){

                case"": //No recipients assigned



                    geoFireActive.removeLocation(userID, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                        }
                    });


                    geoFireAvailability.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            }

                    });

                    break;

                    default:

                        geoFireAvailability.removeLocation(userID, new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                            }
                        });


                        geoFireActive.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                            }

                        });

                        break;



            }





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


        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference DonorAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("DonorsAvailable");
        GeoFire geoFireAvailability = new GeoFire(DonorAvailabilityRef);
        geoFireAvailability.removeLocation(userID, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
            }
        });


    }

    //    private void logoutDonor()
//    {
//        Intent welcomeIntent = new Intent(DonorsMapActivity.this,WelcomeActivity.class);
//        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(welcomeIntent);
//        finish();
//    }

}
