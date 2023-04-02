package com.fyp.bambino;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ModeSpinnerAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;

    public ModeSpinnerAdapter(Context context, List<String> items) {
        super(context, R.layout.spinner_dropdown_item_layout, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public String getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_selected_item_layout, parent, false);
        // Image and TextViews
        TextView state = row.findViewById(R.id.text);
        // Get flag image from drawables folder
//        Resources res = context.getResources();


        //Set state abbreviation and state flag
        state.setText(items.get(position));
        return row;
    }
}