package com.sidpec.sidpecmedicalguide;

import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sidpec.sidpecmedicalguide.models.MedicalEntity;

import java.util.HashMap;
import java.util.Map;

public class EntityDisplayActivity extends BaseActivity {

    String entityID;
    double lon;
    double lat;
    String name;
    String phones[];
    String details;

    ValueEventListener entityValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            MedicalEntity entity = dataSnapshot.getValue(MedicalEntity.class);
            setMedicalEntity(entity);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private DatabaseReference mDatabase;
    private DatabaseReference mEntity;
    private DatabaseReference mIsFav;
    ValueEventListener entityIsFav = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            favoriteButton(dataSnapshot.getValue() != null);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private HashMap<DatabaseReference, ValueEventListener> mListenerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_display);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.enToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        entityID = intent.getStringExtra(EXTRA_MESSAGE_TO_ENTITY);

        mDatabase = FirebaseDatabase.getInstance().getReference(dataNode);
        mEntity = mDatabase.child("medical_entities/entities/" + entityID);
        mEntity.addValueEventListener(entityValueEventListener);
        mListenerMap.put(mEntity, entityValueEventListener);
        String user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String f_email = user_email.replaceAll("[.]", ",");

        mIsFav = mDatabase.child("users/" + f_email + "/" + entityID);
        mIsFav.addValueEventListener(entityIsFav);
        mListenerMap.put(mIsFav, entityIsFav);
    }

    private void setMedicalEntity(MedicalEntity medicalEntity) {
        lon = medicalEntity.getLon();
        lat = medicalEntity.getLat();
        name = medicalEntity.getName();
        details = medicalEntity.getDetails();

        this.setTitle(name);

        TextView entityName = (TextView) findViewById(R.id.tvEntityName);
        entityName.setText(medicalEntity.getName());
        TextView entityAddress = (TextView) findViewById(R.id.tvEntityAddress);
        entityAddress.setText(medicalEntity.getAddress());
        String string_phones = medicalEntity.getPhone();
        phones = string_phones.equals("") ? new String[0] : string_phones.split("[|]");
        TextView entityDetails = (TextView) findViewById(R.id.tvEntityDetails);
        entityDetails.setText(medicalEntity.getDetails());
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void showPhones(View view) {

        if (phones.length > 0) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View convertView = (View) inflater.inflate(R.layout.phone_list_layout, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle(R.string.title_phones);
            ListView lv = (ListView) convertView.findViewById(R.id.lvPhones);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.phone_list_item_layout, R.id.phoneNumber, phones);
            lv.setAdapter(adapter);
            AdapterView.OnItemClickListener adapterViewListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView tvPhone = (TextView) view.findViewById(R.id.phoneNumber);
                    dialPhoneNumber(tvPhone.getText().toString());
                }
            };
            lv.setOnItemClickListener(adapterViewListener);
            alertDialog.show();
        } else {
            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.no_phones, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    public void showMap(View view) {
        showMapIntent(Uri.parse("geo:0,0?q=" + lat + "," + lon + "(" + name + ")"));
    }

    private void showMapIntent(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void favoriteButton(boolean isFav) {
        final ToggleButton tbFav = (ToggleButton) findViewById(R.id.tbFav);

        tbFav.setChecked(isFav);
        checkFavButton(tbFav, isFav);
        tbFav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkFavButton(tbFav, isChecked);
                mIsFav.setValue(isChecked ? 0 : null);
                Snackbar.make(getWindow().getDecorView().getRootView(), isChecked ? R.string.added_to_fav : R.string.removed_from_fav, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    private void checkFavButton(ToggleButton tbFav, boolean isChecked) {
        if (isChecked)
            tbFav.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.heart_on));
        else
            tbFav.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.heart_off));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cleanUpListeners();
    }
}
