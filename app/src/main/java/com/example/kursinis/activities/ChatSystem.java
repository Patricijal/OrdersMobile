package com.example.kursinis.activities;

import static com.example.kursinis.utils.Constants.COMPLETE_ORDER_URL;
import static com.example.kursinis.utils.Constants.GET_MESSAGES_BY_ORDER;
import static com.example.kursinis.utils.Constants.LEAVE_REVIEW_URL;
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
import com.example.kursinis.model.OrderStatus;
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
    String userType;
    private int driverId;
    private int buyerId;
    private int chatId;

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
        String orderStatusString = intent.getStringExtra("orderStatus");
        userType = intent.getStringExtra("userType");
        buyerId = intent.getIntExtra("buyerId", 0);
        driverId = intent.getIntExtra("driverId", 0);

        // Convert to enum
        OrderStatus orderStatus = OrderStatus.valueOf(orderStatusString);
        // Disable input if order is completed
        TextView bodyField = findViewById(R.id.bodyField);
        View sendButton = findViewById(R.id.sendButton);
        View completeButton = findViewById(R.id.completeButton);
        View reviewButton = findViewById(R.id.reviewButton);
        if (orderStatus == OrderStatus.COMPLETED) {
            bodyField.setEnabled(false);
            sendButton.setEnabled(false);
            completeButton.setEnabled(false);
            reviewButton.setEnabled(true);
        } else {
            reviewButton.setEnabled(false);
        }
        if (!"Driver".equals(userType)) {
            completeButton.setVisibility(View.GONE);
        }

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
                            if (!messagesListFromJson.isEmpty()) {
                                chatId = messagesListFromJson.get(0).getChatId();
                            }
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
        TextView messageBody = findViewById(R.id.bodyField);
        String text = messageBody.getText().toString().trim();

        if (text.isEmpty()) {
            android.widget.Toast.makeText(
                    this,
                    "Message cannot be empty!",
                    android.widget.Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("orderId", orderId);
        jsonObject.addProperty("messageText", text);

        String message = gson.toJson(jsonObject);

        executor.execute(() -> {
            try {
                String response = RestOperations.sendPost(SEND_MESSAGE, message);
                System.out.println(response);

                handler.post(() -> {
                    try {
                        if (!response.equals("Error")) {
                            messageBody.setText("");               // clear input
                            loadMessages();                        // reload chat
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

    public void completeOrder(View view) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Complete Order")
                .setMessage("Are you sure you want to mark this order as completed?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    Executor executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());

                    executor.execute(() -> {

                        try {
                            String response = RestOperations.sendPut(
                                    COMPLETE_ORDER_URL + orderId,
                                    ""
                            );

                            handler.post(() -> {
                                if (!"Error".equals(response)) {

                                    // Disable UI locally
                                    TextView bodyField = findViewById(R.id.bodyField);
                                    View sendButton = findViewById(R.id.sendButton);
                                    View completeButton = findViewById(R.id.completeButton);

                                    bodyField.setEnabled(false);
                                    sendButton.setEnabled(false);
                                    completeButton.setEnabled(false);

                                    android.widget.Toast.makeText(
                                            this,
                                            "Order marked as completed.",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void leaveReview(View view) {
        // Inflate custom layout for dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_leave_review, null);

        android.widget.RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        TextView reviewTextField = dialogView.findViewById(R.id.reviewTextField);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Leave a Review")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {

                    int rating = (int) ratingBar.getRating();
                    String reviewText = reviewTextField.getText().toString().trim();

                    if (rating == 0) {
                        android.widget.Toast.makeText(this, "Rating cannot be 0", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (reviewText.isEmpty()) {
                        reviewText = "No comment";
                    }

                    // Determine target user
                    int feedbackUserId = userType.equals("Driver") ? buyerId : driverId;

                    Executor executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());

                    Gson gson = new Gson();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("rating", rating);
                    jsonObject.addProperty("text", reviewText);
                    jsonObject.addProperty("orderId", orderId);
                    jsonObject.addProperty("commentOwnerId", userId);
                    jsonObject.addProperty("feedbackUserId", feedbackUserId);
                    if (userType.equals("Driver")) {
                        jsonObject.addProperty("driver", true);
                        jsonObject.addProperty("chatId", chatId); // you'll need to have chatId in this activity
                    } else {
                        jsonObject.addProperty("driver", false);
                        jsonObject.addProperty("orderId", orderId);
                    }

                    String jsonBody = gson.toJson(jsonObject);

                    executor.execute(() -> {
                        try {
                            String response = RestOperations.sendPost(
                                    LEAVE_REVIEW_URL,
                                    jsonBody
                            );

                            handler.post(() -> {
                                if (!response.equals("Error")) {

                                    // Disable review button so user cannot post again
                                    View leaveReviewBtn = findViewById(R.id.reviewButton);
                                    leaveReviewBtn.setEnabled(false);

                                    android.widget.Toast.makeText(
                                            this,
                                            "Review submitted!",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}