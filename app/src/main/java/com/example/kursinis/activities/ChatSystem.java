package com.example.kursinis.activities;

import static com.example.kursinis.utils.Constants.GET_MESSAGES_BY_ORDER;
import static com.example.kursinis.utils.Constants.SEND_MESSAGE;

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
    private int userId;

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
        userId = intent.getIntExtra("userId", 0);

        loadMessages();
    }

    private void loadMessages() {
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
                            Gson gsonMessages = gsonBuilder.setPrettyPrinting().create();
                            Type messagesListType = new TypeToken<List<Review>>() {
                            }.getType();
                            List<Review> messagesListFromJson = gsonMessages.fromJson(response, messagesListType);
                            ListView messagesListElement = findViewById(R.id.messageList);
                            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messagesListFromJson);
                            messagesListElement.setAdapter(adapter);
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

    public void sendMessage(View view) {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        TextView messageBody = findViewById(R.id.bodyField);

        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("orderId", orderId);
        jsonObject.addProperty("messageText", messageBody.getText().toString());

        String message = gson.toJson(jsonObject);

        executor.execute(() -> {
            try {
                String response = RestOperations.sendPost(SEND_MESSAGE, message);
                System.out.println(response);
                handler.post(() -> {
                    try {
                        if (!response.equals("Error")) {
                            loadMessages();
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