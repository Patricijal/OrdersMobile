package com.example.kursinis.activities;

import static com.example.kursinis.utils.Constants.GET_ALL_RESTAURANTS_URL;
import static com.example.kursinis.utils.Constants.GET_ORDERS_BY_DRIVER;
import static com.example.kursinis.utils.Constants.GET_ORDERS_BY_USER;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kursinis.R;
import com.example.kursinis.model.Driver;
import com.example.kursinis.model.FoodOrder;
import com.example.kursinis.model.Restaurant;
import com.example.kursinis.model.User;
import com.example.kursinis.utils.LocalDateTimeDeserializer;
import com.example.kursinis.utils.LocalDateTimeSerializer;
import com.example.kursinis.utils.LocalDateTypeAdapter;
import com.example.kursinis.utils.RestOperations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MyOrders extends AppCompatActivity {

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Noriu uzkrauti orderius konkreciam klientui

        Intent intent = getIntent();
        userId = intent.getIntExtra("id", 0);
        String userType = intent.getStringExtra("userType");

        String url;
        if ("Driver".equals(userType)) {
            url = GET_ORDERS_BY_DRIVER + userId;
        } else {
            url = GET_ORDERS_BY_USER + userId;
        }

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(url);
                System.out.println(response);
                handler.post(() -> {
                    try {
                        if (!response.equals("Error")) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter());
                            Gson gsonRestaurants = gsonBuilder.setPrettyPrinting().create();
                            Type ordersListType = new TypeToken<List<FoodOrder>>() {
                            }.getType();

                            List<FoodOrder> ordersListFromJson = gsonRestaurants.fromJson(response, ordersListType);

                            ListView ordersListElement = findViewById(R.id.myOrderList);
                            MyOrdersAdapter adapter = new MyOrdersAdapter(this, ordersListFromJson);
                            ordersListElement.setAdapter(adapter);

                            ordersListElement.setOnItemClickListener((parent, view, position, id) -> {
                                System.out.println(ordersListFromJson.get(position));
                                Intent intentChat = new Intent(MyOrders.this, ChatSystem.class);
                                intentChat.putExtra("orderId", ordersListFromJson.get(position).getId());
                                intentChat.putExtra("userId", userId);
                                intentChat.putExtra("orderStatus", ordersListFromJson.get(position).getOrderStatus().name());
                                if (userType.equals("Driver")) {
                                    intentChat.putExtra("userType", "Driver");
                                } else if (userType.equals("BasicUser")) {
                                    intentChat.putExtra("userType", "BasicUser");
                                }
                                intentChat.putExtra("buyerId", ordersListFromJson.get(position).getBuyerId());
                                intentChat.putExtra("driverId", ordersListFromJson.get(position).getDriverId());
                                startActivity(intentChat);
                            });


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}