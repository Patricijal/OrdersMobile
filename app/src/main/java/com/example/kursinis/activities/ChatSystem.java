package com.example.kursinis.activities;

import static com.example.kursinis.utils.Constants.GET_MESSAGES_BY_ORDER;
import static com.example.kursinis.utils.Constants.GET_ORDERS_BY_USER;
import static com.example.kursinis.utils.Constants.SEND_MESSAGE_IN_CHAT;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kursinis.R;
import com.example.kursinis.model.FoodOrder;
import com.example.kursinis.model.Review;
import com.example.kursinis.utils.LocalDateTypeAdapter;
import com.example.kursinis.utils.RestOperations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatSystem extends AppCompatActivity {

    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_system);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Noriu uzkrauti orderius konkreciam klientui

        Intent intent = getIntent();
        orderId = intent.getIntExtra("orderId", 0);
//        String userInfo = intent.getStringExtra("userJsonObject");


        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(GET_MESSAGES_BY_ORDER + orderId);
                System.out.println(response);
                handler.post(() -> {
                    try {
                        if (!response.equals("Error")) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter());
                            Gson gsonRestaurants = gsonBuilder.setPrettyPrinting().create();
                            Type ordersListType = new TypeToken<List<Review>>() {
                            }.getType();
                            List<Review> ordersListFromJson = gsonRestaurants.fromJson(response, ordersListType);
                            ListView restaurantListElement = findViewById(R.id.messageList);
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

    public void createMessage(View view) {
        TextView messageField = findViewById(R.id.bodyField);
        String messageText = messageField.getText().toString().trim();

        if (messageText.isEmpty()) return;

        // Create JSON
        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        json.addProperty("text", messageText);

        // YOU MUST send commentOwner.id or driver.id
        // Example: ownerId = 5
        int ownerId = 2; // take from logged-in user!!!
        JsonObject commentOwner = new JsonObject();
        commentOwner.addProperty("id", ownerId);

        json.add("commentOwner", commentOwner);

        String body = gson.toJson(json);

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response =
                        RestOperations.sendPost(SEND_MESSAGE_IN_CHAT + orderId, body);

                handler.post(() -> {
                    messageField.setText("");
//                    reloadMessages();  // refresh messages list
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}