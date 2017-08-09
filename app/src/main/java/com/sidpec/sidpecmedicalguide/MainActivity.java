package com.sidpec.sidpecmedicalguide;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sidpec.sidpecmedicalguide.adapters.medicalEntityArrayAdapter;
import com.sidpec.sidpecmedicalguide.models.Category;
import com.sidpec.sidpecmedicalguide.models.MedicalEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;
    private static final int GROUP_CAT = 1000;
    private static final int NAV_FAV = -1;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location my_location;
    boolean inLocSortMode = false;
    int catID = NAV_FAV;
    private DatabaseReference mDatabase;
    private DatabaseReference mCats;
    private DatabaseReference mEntity;
    private DatabaseReference mFavs;

    private NavigationView navigationView;
    private Menu menu;
    private ListView listView;
    private ArrayList arrayList;
    private ArrayAdapter<MedicalEntity> adapter;

    private HashMap<DatabaseReference, ValueEventListener> mListenerMap = new HashMap<>();

    //    private void submitData() {
//
//        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();
//
//        // [START single_value_read]
//        final String userId = getUid();
//        writeMedicalEntity(1, "صيدلية خليل", "23 شارع ابن ماجد، سيدي جابر", "01001661239;034274435", 30.0, 31.0, 1, "", "مواعيد العمل من 4 ل 6 ويوجد استقبال");
//        writeMedicalEntity(2, "مستشفي اسمها طويل جدا وكمان محطوط في الاسم التخصص", "33 شارع محمد فوزي معاذ، سموحة او اي مكان تاني غير ده عشان نملأ الصفحة", "01224990651;035226784;01001661239;034246857;034274435;01227413179;01228403933;01224990651;035226784;01001661239;034246857;034274435;01227413179;01228403933", 29.5, 30.5, 2, "قلب;عظام;أطفال", "مواعيد العمل يوم الجمعة من 2 ل 3 وباقي الايام اجازة");
//        writeMedicalEntity(3, "مركز نجا للأسنان", "شارع كنيسة الدبانة، محطة الرمل", "01096466219", 31.4, 30.45, 1, "عظام", "المركز لا يعمل اي يوم من ايام الاسبوع\nده سطر جديد بقي");
//
//    }
//
//    private void writeMedicalEntity(int entityID, String name, String address, String phone, double lat, double lon, int cat_id, String metadata, String details) {
//
//        // Create new post at /user-posts/$userid/$postid and at
//        // /posts/$postid simultaneously
//        String key = mDatabase.child("medical_entities").push().getKey();
//        MedicalEntity medicalEntity = new MedicalEntity(entityID, name, address, phone, lat, lon, cat_id, metadata, details);
//
//        //Map<String, Object> postValues = post.toMap();
//        Map<String, Object> childUpdates = new HashMap<>();
//        childUpdates.put("/medical_entities/" + key, medicalEntity);
//        //childUpdates.put("/user-posts/" + userId + "/" + key, postValues);
//
//        mDatabase.updateChildren(childUpdates);
//    }
    private ValueEventListener entityValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.content_home);
            listView = new ListView(getApplicationContext());
            arrayList = new ArrayList<MedicalEntity>();

            relativeLayout.removeAllViews();

            for (DataSnapshot dSnapshot : dataSnapshot.getChildren()) {
                MedicalEntity ent = dSnapshot.getValue(MedicalEntity.class);
                arrayList.add(ent);
            }

            Collections.sort(arrayList, new Comparator<MedicalEntity>() {
                public int compare(MedicalEntity obj1, MedicalEntity obj2) {
                    if (inLocSortMode) {
                        int d1 = obj1.getDistance(my_location.getLatitude(), my_location.getLongitude());
                        int d2 = obj2.getDistance(my_location.getLatitude(), my_location.getLongitude());

                        return Integer.compare(d1, d2);
                    } else {
                        return obj1.name.compareTo(obj2.name);
                    }
                }
            });

            adapter = new medicalEntityArrayAdapter(getApplicationContext(), 0, arrayList, inLocSortMode ? my_location : null);
            listView.setAdapter(adapter);
            relativeLayout.addView(listView);

            AdapterView.OnItemClickListener adapterViewListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    showEntity(view);
                }
            };

            listView.setOnItemClickListener(adapterViewListener);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private ValueEventListener favsEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            MedicalEntity ent = dataSnapshot.getValue(MedicalEntity.class);
            if (ent != null) {
                arrayList.add(ent);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private ValueEventListener entityFavValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            favChanged(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference(dataNode);
        mDatabase.keepSynced(true);
        mCats = mDatabase.child("medical_entities/cats");

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inLocSortMode = !inLocSortMode;
                fab.setImageResource(inLocSortMode ? android.R.drawable.ic_menu_mylocation : android.R.drawable.ic_menu_sort_alphabetically);
                if (catID >= 0)
                    showEntities(catID);
                else if (catID == -1)
                    showFavs();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menu = navigationView.getMenu();

        ValueEventListener menuCatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                menu.removeGroup(GROUP_CAT);
                for (DataSnapshot catSnapshot : dataSnapshot.getChildren()) {
                    Category cat = catSnapshot.getValue(Category.class);
                    menu.add(GROUP_CAT, cat.catId, 1, cat.display).setChecked(cat.catId == NAV_FAV);
                }
                menu.setGroupCheckable(GROUP_CAT, true, true);
                menu.setGroupVisible(GROUP_CAT, true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mCats.orderByChild("order").addValueEventListener(menuCatListener);

        Bundle b = getIntent().getExtras();
        int value = -1;
        if (b != null) {
            value = b.getInt("show_fav");
            if (value == 1)
                showFavs();
        }


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        requestPermissions();
    }

    private void requestPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onConnected(Bundle connectionHint) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.i(TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        //mLocationView.setText("Location received: " + location.toString());
        Toast.makeText(this, location.toString(), Toast.LENGTH_SHORT).show();
        my_location = location;
        if (inLocSortMode) {
            //displayContent();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_sign_out) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        Bundle b = new Bundle();
        b.putInt("sign_out", 1);
        intent.putExtras(b);
        startActivity(intent);
        finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //Toast.makeText(this, String.valueOf(id), Toast.LENGTH_LONG).show();
        this.setTitle(item.getTitle());
        catID = id;
        if (id == NAV_FAV) {
            // Handle favs
            showFavs();
        } else {
            //handle cat
            showEntities(id);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showEntities(int id) {
        cleanUpListeners();
        mEntity = mDatabase.child("medical_entities/entities");
        mListenerMap.put(mEntity, entityValueEventListener);
        mEntity.orderByChild("catId").equalTo(id).addValueEventListener(entityValueEventListener);
    }

    private void showFavs() {
        cleanUpListeners();
        String user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String f_email = user_email.replaceAll("[.]", ",");
        mEntity = mDatabase.child("users").child(f_email);
        mEntity.addValueEventListener(entityFavValueEventListener);
        mListenerMap.put(mEntity, entityFavValueEventListener);
    }

    private void favChanged(DataSnapshot dataSnapshot) {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.content_home);
        listView = new ListView(getApplicationContext());
        arrayList = new ArrayList<MedicalEntity>();
        relativeLayout.removeAllViews();

        adapter = new medicalEntityArrayAdapter(this, 0, arrayList, inLocSortMode ? my_location : null);
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            mFavs = mDatabase.child("medical_entities/entities");
            mFavs.child(snapshot.getKey()).addListenerForSingleValueEvent(favsEventListener);
        }


        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        relativeLayout.addView(listView);


        AdapterView.OnItemClickListener adapterViewListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEntity(view);
            }
        };

        listView.setOnItemClickListener(adapterViewListener);
    }

    private void cleanUpListeners() {
        if (mListenerMap == null)
            return;
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : mListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }

    private void showEntity(View view) {
        TextView entityID = (TextView) view.findViewById(R.id.entityID);
        Intent intent = new Intent(this, EntityDisplayActivity.class);
        intent.putExtra(EXTRA_MESSAGE_TO_ENTITY, entityID.getText());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanUpListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (catID == -1)
            showFavs();
        else
            showEntities(catID);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        cleanUpListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
