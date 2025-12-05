package com.example.kursinis.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.kursinis.R;
import com.example.kursinis.model.Restaurant;

import java.util.List;

public class RestaurantAdapter extends ArrayAdapter<Restaurant> {

    public RestaurantAdapter(@NonNull Context context, @NonNull List<Restaurant> restaurants) {
        super(context, 0, restaurants);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_restaurant, parent, false);
        }

        Restaurant restaurant = getItem(position);

        if (restaurant != null) {
            TextView nameTextView = convertView.findViewById(R.id.restaurantName);
            TextView addressTextView = convertView.findViewById(R.id.restaurantAddress);
            TextView phoneTextView = convertView.findViewById(R.id.restaurantPhone);

            // Display restaurant name (name + surname or just name)
            String restaurantName = restaurant.getName();
//            if (restaurant.getSurname() != null && !restaurant.getSurname().isEmpty()) {
//                restaurantName += " " + restaurant.getSurname();
//            }
            nameTextView.setText(restaurantName);

            // Display address
            if (restaurant.getAddress() != null && !restaurant.getAddress().isEmpty()) {
                addressTextView.setText("📍 " + restaurant.getAddress());
            } else {
                addressTextView.setText("📍 Address not available");
            }

            // Display phone number
            if (restaurant.getPhoneNumber() != null && !restaurant.getPhoneNumber().isEmpty()) {
                phoneTextView.setText("📞 " + restaurant.getPhoneNumber());
            } else {
                phoneTextView.setText("📞 Phone not available");
            }
        }

        return convertView;
    }
}

