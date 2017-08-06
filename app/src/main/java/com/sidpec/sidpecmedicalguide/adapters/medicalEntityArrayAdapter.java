package com.sidpec.sidpecmedicalguide.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sidpec.sidpecmedicalguide.R;
import com.sidpec.sidpecmedicalguide.models.Entity;
import com.sidpec.sidpecmedicalguide.models.MedicalEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by mena on 8/1/2017.
 */

public class medicalEntityArrayAdapter extends ArrayAdapter<MedicalEntity> {
    private Context context;
    private List<MedicalEntity> medicalEntityList;
    private Location currentLocation;

    public medicalEntityArrayAdapter(Context context, int resource, ArrayList<MedicalEntity> objects, Location currentLocation) {
        super(context, resource, objects);

        this.context = context;
        this.medicalEntityList = objects;
        this.currentLocation = currentLocation;
    }

    @Override
    public void sort(@NonNull Comparator<? super MedicalEntity> comparator) {
        super.sort(comparator);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        MedicalEntity medicalEntity = medicalEntityList.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.entity_list_item_layout, null);

        TextView entityID = (TextView) view.findViewById(R.id.entityID);
        TextView entityName = (TextView) view.findViewById(R.id.entityName);
        TextView distance = (TextView) view.findViewById(R.id.distance);

        entityID.setText(String.valueOf(medicalEntity.getEntityID()));
        entityName.setText(medicalEntity.getName());
        int dist = 0;
        String sDist = "";
        if (currentLocation != null) {
            dist = medicalEntity.getDistance(this.currentLocation.getLatitude(), this.currentLocation.getLongitude());
            int km = dist / 1000;
            int m = dist % 1000;
            Resources res = context.getResources();
            sDist = ((km > 0) ? String.format("%1$d %2$s", km, res.getString(R.string.km_units)) : "") + ((m > 0) ? String.format(" %1$d %2$s", m, res.getString(R.string.m_units)) : "");
            distance.setVisibility(View.VISIBLE);
        }
        distance.setText(sDist);

        return view;
    }
}
