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

public class DriverOrderAdapter extends ArrayAdapter<FoodOrder> {

    private Context context;
    private List<FoodOrder> orders;

    public DriverOrderAdapter(@NonNull Context context, @NonNull List<FoodOrder> orders) {
        super(context, 0, orders);
        this.context = context;
        this.orders = orders;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.order_list_item, parent, false);
        }

        FoodOrder order = orders.get(position);

        TextView nameText = convertView.findViewById(R.id.orderName);
        TextView priceText = convertView.findViewById(R.id.orderPrice);
        TextView statusText = convertView.findViewById(R.id.orderStatus);
        TextView dateText = convertView.findViewById(R.id.orderDate);

        nameText.setText(order.getName());
        priceText.setText(String.format("%.2f EUR", order.getPrice()));
        statusText.setText("Status: " + order.getOrderStatus());
        dateText.setText("Date: " + order.getDateCreated());

        return convertView;
    }
}