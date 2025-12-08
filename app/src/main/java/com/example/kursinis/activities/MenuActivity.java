package com.example.kursinis.activities;

import static com.example.kursinis.utils.Constants.CREATE_ORDER;
import static com.example.kursinis.utils.Constants.GET_RESTAURANT_MENU;
import static com.example.kursinis.utils.Constants.GET_USER_BY_ID_URL;
import static com.example.kursinis.utils.Constants.LEAVE_REVIEW_URL;
import static com.example.kursinis.utils.Constants.UPDATE_CLIENT_BONUS_URL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.kursinis.R;
import com.example.kursinis.model.Cuisine;
import com.example.kursinis.utils.LocalDateTypeAdapter;
import com.example.kursinis.utils.RestOperations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MenuActivity extends AppCompatActivity implements MenuAdapter.OnQuantityChangeListener {
    private int userId;
    private int restaurantId;
    private int bonusPoints;
    private int restaurantDiscount;
    private MenuAdapter menuAdapter;
    private TextView orderTotalTextView;
    private TextView orderItemsCountTextView;
    private double discountedTotal;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
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

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", 0);
        restaurantId = intent.getIntExtra("restaurantId", 0);
        String restaurantName = intent.getStringExtra("name");
        String workHours = intent.getStringExtra("workHours");
        restaurantDiscount = intent.getIntExtra("discount", 0);

        ((TextView) findViewById(R.id.restaurantNameLabel)).setText(restaurantName);
        ((TextView) findViewById(R.id.restaurantWorkHoursLabel)).setText("Work hours: " + workHours);
        ((TextView) findViewById(R.id.restaurantDiscountLabel)).setText("Discount: " + restaurantDiscount + "%");

        orderTotalTextView = findViewById(R.id.orderTotal);
        orderItemsCountTextView = findViewById(R.id.orderItemsCount);
        loadUserBonusPoints(userId);
        loadMenu();
    }

    private void loadMenu() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(GET_RESTAURANT_MENU + restaurantId);
                System.out.println(response);
                handler.post(() -> {
                    try {
                        if (!response.equals("Error")) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter());
                            Gson gsonMenu = gsonBuilder.setPrettyPrinting().create();
                            Type menuListType = new TypeToken<List<Cuisine>>() {
                            }.getType();
                            List<Cuisine> menuListFromJson = gsonMenu.fromJson(response, menuListType);

                            ListView menuListElement = findViewById(R.id.menuItems);
                            menuAdapter = new MenuAdapter(this, menuListFromJson);
                            menuListElement.setAdapter(menuAdapter);

                            // Update order summary
                            updateOrderSummary();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading menu", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                handler.post(() -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateOrderSummary() {
        if (menuAdapter == null) return;

        Map<Integer, Integer> quantities = menuAdapter.getQuantities();
        List<Cuisine> menuItems = menuAdapter.getMenuItems();

        double total = 0.0;
        int itemCount = 0;

        // Calculate total and item count
        for (Cuisine cuisine : menuItems) {
            int quantity = quantities.getOrDefault(cuisine.getId(), 0);
            if (quantity > 0) {
                total += cuisine.getPrice() * quantity;
                itemCount += quantity;
            }
        }

        discountedTotal = total * (1 - restaurantDiscount / 100.0) * (1 - bonusPoints / 100.0);
        discountedTotal = Math.round(discountedTotal * 100.0) / 100.0;

        // Update the real price TextView (before discounts)
        TextView realPriceTextView = findViewById(R.id.realPrice);
        realPriceTextView.setText(String.format("Price: €%.2f", total));

        // Show both original and discounted prices in orderTotal
        String text = String.format("€%.2f\n€%.2f", total, discountedTotal);
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(
                new StrikethroughSpan(),
                0,
                String.format("€%.2f", total).length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        orderTotalTextView.setText(spannable);

        // Update items count
        orderItemsCountTextView.setText(String.format("Items: %d", itemCount));
    }

    public void placeOrder(View view) {
        if (menuAdapter == null) {
            Toast.makeText(this, "Menu not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<Integer, Integer> quantities = menuAdapter.getQuantities();
        List<Cuisine> menuItems = menuAdapter.getMenuItems();

        boolean hasItems = false;
        for (int qty : quantities.values()) {
            if (qty > 0) {
                hasItems = true;
                break;
            }
        }

        if (!hasItems) {
            Toast.makeText(this, "Please add items to your order", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        JsonObject orderJson = new JsonObject();
        orderJson.addProperty("userId", userId);
        orderJson.addProperty("restaurantId", restaurantId);
        orderJson.addProperty("bonusPoints", bonusPoints);

        JsonArray itemsArray = new JsonArray();
        for (Cuisine cuisine : menuItems) {
            int quantity = quantities.getOrDefault(cuisine.getId(), 0);
            if (quantity > 0) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("cuisineId", cuisine.getId());
                itemJson.addProperty("quantity", quantity);
                itemsArray.add(itemJson);
            }
        }
        orderJson.add("items", itemsArray);

        String orderData = gson.toJson(orderJson);

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendPost(CREATE_ORDER, orderData);
                System.out.println("Order response: " + response);
                handler.post(() -> {
                    if (!response.equals("Error") && !response.isEmpty()) {
                        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();

                        bonusPoints++;

                        JsonObject bonusJson = new JsonObject();
                        bonusJson.addProperty("id", userId);
                        bonusJson.addProperty("bonusPoints", bonusPoints);

                        Executor bonusExecutor = Executors.newSingleThreadExecutor();
                        bonusExecutor.execute(() -> {
                            try {
                                RestOperations.sendPut(UPDATE_CLIENT_BONUS_URL, bonusJson.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        // Clear the cart
                        menuAdapter.getQuantities().clear();
                        menuAdapter.notifyDataSetChanged();
                        updateOrderSummary();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                handler.post(() -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onQuantityChanged() {
        updateOrderSummary();
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

                    Executor executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());

                    Gson gson = new Gson();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("rating", rating);
                    jsonObject.addProperty("text", reviewText);
                    jsonObject.addProperty("commentOwnerId", userId);
                    jsonObject.addProperty("feedbackUserId", restaurantId);
                    jsonObject.addProperty("restaurantId", restaurantId);

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
                                    View leaveReviewBtn = findViewById(R.id.restaurantReviewButton);
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

    @SuppressLint("SetTextI18n")
    private void loadUserBonusPoints(int userId) {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(GET_USER_BY_ID_URL + userId);
                if (!response.equals("Error")) {
                    Gson gson = new Gson();
                    JsonObject userJson = gson.fromJson(response, JsonObject.class);
                    bonusPoints = userJson.get("bonusPoints").getAsInt();

                    handler.post(() -> {
                        ((TextView) findViewById(R.id.bonusPointsLabel)).setText("Bonus points: " + bonusPoints);
                        Toast.makeText(this, "You have " + bonusPoints + " bonus points!", Toast.LENGTH_SHORT).show();
                        updateOrderSummary();
                    });
                }
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(this, "Failed to load bonus points", Toast.LENGTH_SHORT).show());
            }
        });
    }
}