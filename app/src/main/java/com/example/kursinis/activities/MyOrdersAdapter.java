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
import com.example.kursinis.model.FoodOrder;

import java.util.List;

public class MyOrdersAdapter extends ArrayAdapter<FoodOrder> {

    public MyOrdersAdapter(@NonNull Context context, @NonNull List<FoodOrder> orders) {
        super(context, 0, orders);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_order, parent, false);
        }

        FoodOrder order = getItem(position);

        TextView restaurantLabel = view.findViewById(R.id.orderRestaurant);
        TextView orderTitle = view.findViewById(R.id.orderTitle);
        TextView orderPrice = view.findViewById(R.id.orderPrice);

        if (order != null) {
//            String restaurantName = order.getRestaurantName();
//            // Fallback to order name if restaurant name is not present
//            restaurantLabel.setText(restaurantName != null && !restaurantName.isEmpty()
//                    ? restaurantName
//                    : order.getName());

            orderTitle.setText("Order #" + order.getId());

            if (order.getPrice() != null) {
                orderPrice.setText("€" + String.format("%.2f", order.getPrice()));
            } else {
                orderPrice.setText("Price: N/A");
            }
        }

        return view;
    }
}

