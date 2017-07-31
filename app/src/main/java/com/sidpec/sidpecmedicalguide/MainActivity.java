package com.sidpec.sidpecmedicalguide;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sidpec.sidpecmedicalguide.models.Category;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int GROUP_CAT = 1000;
    private static final int NAV_FAV = -1;

    private DatabaseReference mDatabase;
    private DatabaseReference mCats;
    private DatabaseReference mEntity;

    private NavigationView navigationView;
    private Menu menu;
    private ValueEventListener entityValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference(dataNode);
        mCats = mDatabase.child("medical_entities/cats");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //submitData();
                String uemail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                Snackbar.make(view, "Replace with your own action " + uemail, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

    private void test() {
        mEntity.removeEventListener(entityValueEventListener);
        mEntity = mDatabase.child("medical_entities/entities");
        mEntity.orderByChild("name").addValueEventListener(entityValueEventListener);
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
        Toast.makeText(this, String.valueOf(id), Toast.LENGTH_LONG).show();

        if (id == NAV_FAV) {
            // Handle favs

        } else {
            //handle cat

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
