package com.tarunsmalviya.solarcalculator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tarunsmalviya.solarcalculator.R;
import com.tarunsmalviya.solarcalculator.model.PinnedLocation;
import com.tarunsmalviya.solarcalculator.util.OnPinnedLocationSelected;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.realm.RealmResults;

public class PinnedLocationAdapter extends ArrayAdapter<PinnedLocation> {

    private OnPinnedLocationSelected onPinnedLocationSelected;

    public PinnedLocationAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        onPinnedLocationSelected = (OnPinnedLocationSelected) context;
    }

    public PinnedLocationAdapter(Context context, int resource, RealmResults<PinnedLocation> locations) {
        super(context, resource, locations);

        onPinnedLocationSelected = (OnPinnedLocationSelected) context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(R.layout.item_location, null);
        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPinnedLocationSelected != null)
                    onPinnedLocationSelected.onPinnedLocationSelected(position, false);
            }
        });

        final PinnedLocation location = getItem(position);
        if (location != null) {
            View bg = v.findViewById(R.id.bg);
            bg.setVisibility(position % 2 == 0 ? View.VISIBLE : View.GONE);

            TextView dateTxt = v.findViewById(R.id.date_txt);
            TextView latitudeTxt = v.findViewById(R.id.latitude_txt);
            TextView longitudeTxt = v.findViewById(R.id.longitude_txt);
            SimpleDateFormat date = new SimpleDateFormat("dd MMM, yyyy");

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, location.getDay());
            calendar.set(Calendar.MONTH, location.getMonth());
            calendar.set(Calendar.YEAR, location.getYear());

            dateTxt.setText(date.format(calendar.getTime()));
            latitudeTxt.setText(String.format("%.06f", location.getLatitude()));
            longitudeTxt.setText(String.format("%.06f", location.getLongitude()));

            ImageButton deleteBtn = v.findViewById(R.id.delete_btn);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onPinnedLocationSelected != null)
                        onPinnedLocationSelected.onPinnedLocationSelected(position, true);
                    notifyDataSetChanged();
                }
            });
        }

        return v;
    }
}
