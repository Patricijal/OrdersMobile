package com.example.kursinis.activities;

import static com.example.kursinis.utils.Constants.GET_CUISINES_BY_RESTAURANT;
import static com.example.kursinis.utils.Constants.GET_MESSAGES_BY_ORDER;

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
import com.example.kursinis.model.Cuisine;
import com.example.kursinis.model.Review;
import com.example.kursinis.utils.LocalDateTypeAdapter;
import com.example.kursinis.utils.RestOperations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MenuActivity extends AppCompatActivity {

    private int restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Noriu uzkrauti orderius konkreciam klientui

        Intent intent = getIntent();
        restaurantId = intent.getIntExtra("restaurantId", 0);
//        String userInfo = intent.getStringExtra("userJsonObject");


        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(GET_CUISINES_BY_RESTAURANT);
                System.out.println(response);
                handler.post(() -> {
                    try {
                        if (!response.equals("Error")) {
                            //Cia yra dalis, kaip is json, kuriame yra [{},{}, {},...] paversti i List is Restoranu

                            GsonBuilder gsonBuilder = new GsonBuilder();
//                                gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
//                            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTypeAdapter());
                            gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter());
                            Gson gsonRestaurants = gsonBuilder.setPrettyPrinting().create();

                            Type ordersListType = new TypeToken<List<Cuisine>>() {
                            }.getType();
                            List<Cuisine> ordersListFromJson = gsonRestaurants.fromJson(response, ordersListType);
                            ListView restaurantListElement = findViewById(R.id.cuisineList);
                            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ordersListFromJson);
                            restaurantListElement.setAdapter(adapter);
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