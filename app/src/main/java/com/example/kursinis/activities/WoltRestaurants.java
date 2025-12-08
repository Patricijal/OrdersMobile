package com.example.kursinis.activities;

import static com.example.kursinis.utils.Constants.ASSIGN_DRIVER_TO_ORDER_URL;
import static com.example.kursinis.utils.Constants.GET_ALL_RESTAURANTS_URL;
import static com.example.kursinis.utils.Constants.GET_PENDING_ORDERS_URL;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kursinis.R;
import com.example.kursinis.model.BasicUser;
import com.example.kursinis.model.FoodOrder;
import com.example.kursinis.utils.LocalDateTimeDeserializer;
import com.example.kursinis.utils.LocalDateTimeSerializer;
import com.example.kursinis.utils.LocalDateTypeAdapter;
import com.example.kursinis.utils.RestOperations;
import com.example.kursinis.model.Driver;
import com.example.kursinis.model.Restaurant;
import com.example.kursinis.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WoltRestaurants extends AppCompatActivity {

    User currentUser;
    List<FoodOrder> pendingOrders;
    ListView ordersListView;
    String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wolt_restaurants);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Priejimas prie duomenu is praeitos Activity

        Intent intent = getIntent();
        String userInfo = intent.getStringExtra("userJsonObject");

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(userInfo, JsonObject.class);

        userType = jsonObject.get("userType").getAsString();


        if (userType.equals("Driver")) {
            currentUser = gson.fromJson(userInfo, Driver.class);

            Executor executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                try {
                    String response = RestOperations.sendGet(GET_PENDING_ORDERS_URL);
                    System.out.println(response);
                    handler.post(() -> {
                        if (!response.equals("Error")) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter());
                            Gson gsonOrders = gsonBuilder.create();
                            Type listType = new TypeToken<List<FoodOrder>>() {}.getType();

                            pendingOrders = gsonOrders.fromJson(response, listType);
                            ordersListView = findViewById(R.id.restaurantList);
                            DriverOrderAdapter adapter = new DriverOrderAdapter(this, pendingOrders);
                            ordersListView.setAdapter(adapter);

                            ordersListView.setOnItemClickListener((parent, view, position, id) -> {
                                FoodOrder selectedOrder = pendingOrders.get(position);
                                showAssignDriverDialog(selectedOrder);
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } else if (userType.equals("BasicUser")) {
            currentUser = gson.fromJson(userInfo, BasicUser.class);
            Executor executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                try {
                    String response = RestOperations.sendGet(GET_ALL_RESTAURANTS_URL);
                    System.out.println(response);
                    handler.post(() -> {
                        try {
                            if (!response.equals("Error")) {
                                GsonBuilder gsonBuilder = new GsonBuilder();
                                gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
                                Gson gsonRestaurants = gsonBuilder.setPrettyPrinting().create();
                                Type restaurantListType = new TypeToken<List<Restaurant>>() {
                                }.getType();
                                List<Restaurant> restaurantListFromJson = gsonRestaurants.fromJson(response, restaurantListType);
                                ListView restaurantListElement = findViewById(R.id.restaurantList);
                                RestaurantAdapter adapter = new RestaurantAdapter(this, restaurantListFromJson);
                                restaurantListElement.setAdapter(adapter);

                                restaurantListElement.setOnItemClickListener((parent, view, position, id) -> {
                                    Restaurant selectedRestaurant = restaurantListFromJson.get(position);
                                    Intent intentMenu = new Intent(WoltRestaurants.this, MenuActivity.class);
                                    intentMenu.putExtra("restaurantId", selectedRestaurant.getId());
                                    intentMenu.putExtra("userId", currentUser.getId());
                                    intentMenu.putExtra("name", selectedRestaurant.getName());
                                    intentMenu.putExtra("workHours", selectedRestaurant.getWorkHours());
                                    intentMenu.putExtra("discount", selectedRestaurant.getDiscount());
                                    startActivity(intentMenu);
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

    public void viewPurchaseHistory(View view) {
        Intent intent = new Intent(WoltRestaurants.this, MyOrders.class);
        intent.putExtra("id", currentUser.getId());
        if (userType.equals("Driver")) {
            intent.putExtra("userType", "Driver");
        } else if (userType.equals("BasicUser")) {
            intent.putExtra("userType", "BasicUser");
        }
        startActivity(intent);
    }

    public void viewMyAccount(View view) {
        if (currentUser == null) {
            Toast.makeText(this, "User not loaded yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(WoltRestaurants.this, MyInfoActivity.class);
        intent.putExtra("id", currentUser.getId());
        startActivity(intent);
    }

    private void showAssignDriverDialog(FoodOrder order) {
        new AlertDialog.Builder(this)
                .setTitle("Take Order")
                .setMessage("Do you want to take \"" + order.getName() + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> assignDriverToOrder(order))
                .setNegativeButton("No", null)
                .show();
    }

    private void assignDriverToOrder(FoodOrder order) {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String url = ASSIGN_DRIVER_TO_ORDER_URL + currentUser.getId() + "?orderId=" + order.getId();
                String response = RestOperations.sendPut(url, "");
                handler.post(() -> {
                    if (!response.equals("Error")) {
                        Toast.makeText(this, "Order assigned successfully!", Toast.LENGTH_SHORT).show();
                        pendingOrders.remove(order);
                        ((DriverOrderAdapter) ordersListView.getAdapter()).notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to assign order.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}