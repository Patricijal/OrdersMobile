package com.example.kursinis.activities;

import static com.example.kursinis.utils.Constants.CREATE_BASIC_USER_URL;
import static com.example.kursinis.utils.Constants.CREATE_DRIVER_URL;
import static com.example.kursinis.utils.Constants.GET_USER_BY_ID_URL;
import static com.example.kursinis.utils.Constants.UPDATE_USER_URL;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kursinis.R;
import com.example.kursinis.model.BasicUser;
import com.example.kursinis.model.Driver;
import com.example.kursinis.model.User;
import com.example.kursinis.model.VehicleType;
import com.example.kursinis.utils.LocalDateTimeDeserializer;
import com.example.kursinis.utils.LocalDateTimeSerializer;
import com.example.kursinis.utils.LocalDateTypeAdapter;
import com.example.kursinis.utils.RestOperations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MyInfoActivity extends AppCompatActivity {
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        userId = intent.getIntExtra("id", -1);

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = RestOperations.sendGet(GET_USER_BY_ID_URL + userId);
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())  // for sending
                            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer()) // for reading
                            .setPrettyPrinting()
                            .create();

                    // Parse as User first
                    User user = gson.fromJson(response, User.class);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            populateFields(user, gson, response);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void populateFields(User user, Gson gson, String rawJson) {
        ((TextView) findViewById(R.id.updLoginField)).setText(user.getLogin());
        ((TextView) findViewById(R.id.updPasswordField)).setText(user.getPassword());
        ((TextView) findViewById(R.id.updNameField)).setText(user.getName());
        ((TextView) findViewById(R.id.updSurnameField)).setText(user.getSurname());
        ((TextView) findViewById(R.id.updPhoneNumField)).setText(user.getPhoneNumber());
        BasicUser basicUser = gson.fromJson(rawJson, BasicUser.class);
        ((TextView) findViewById(R.id.updAddressField)).setText(basicUser.getAddress());

        // Check if  Driver
        if (rawJson.contains("vehicleType")) { // crude check based on JSON
            Driver driver = gson.fromJson(rawJson, Driver.class);
            ((TextView) findViewById(R.id.updLicenseField)).setText(driver.getLicense());
            ((TextView) findViewById(R.id.updBirthDateField)).setText(driver.getBDate().toString());
            ((TextView) findViewById(R.id.updVehicleTypeField)).setText(driver.getVehicleType().name());

            findViewById(R.id.updLicenseField).setVisibility(View.VISIBLE);
            findViewById(R.id.updBirthDateField).setVisibility(View.VISIBLE);
            findViewById(R.id.updVehicleTypeField).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.updLicenseField).setVisibility(View.GONE);
            findViewById(R.id.updBirthDateField).setVisibility(View.GONE);
            findViewById(R.id.updVehicleTypeField).setVisibility(View.GONE);
        }
    }

    public void updateUser(View view) {
        TextView username = findViewById(R.id.updLoginField);
        TextView password = findViewById(R.id.updPasswordField);
        TextView name = findViewById(R.id.updNameField);
        TextView surname = findViewById(R.id.updSurnameField);
        TextView phoneNum = findViewById(R.id.updPhoneNumField);
        TextView address = findViewById(R.id.updAddressField);
        TextView license = findViewById(R.id.updLicenseField);
        TextView bDate = findViewById(R.id.updBirthDateField);
        TextView vehicleType = findViewById(R.id.updVehicleTypeField);

        boolean isDriver = license.getVisibility() == View.VISIBLE;

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                .setPrettyPrinting()
                .create();

        String userInfo = "{}";
        if (isDriver) {
            LocalDate birthDate = LocalDate.parse(bDate.getText().toString());
            VehicleType vehicleTypeEnum = VehicleType.valueOf(vehicleType.getText().toString().toUpperCase());
            Driver driver = new Driver(
                    userId,
                    username.getText().toString(),
                    password.getText().toString(),
                    name.getText().toString(),
                    surname.getText().toString(),
                    phoneNum.getText().toString(),
                    address.getText().toString(),
                    license.getText().toString(),
                    birthDate,
                    vehicleTypeEnum
            );
//            driver.setId(userId);
            userInfo = gson.toJson(driver, Driver.class);
            System.out.println(userInfo);
        } else {
            BasicUser basicUser = new BasicUser(
                    userId,
                    username.getText().toString(),
                    password.getText().toString(),
                    name.getText().toString(),
                    surname.getText().toString(),
                    phoneNum.getText().toString(),
                    address.getText().toString()
            );
//            basicUser.setId(userId);
            userInfo = gson.toJson(basicUser, BasicUser.class);
            System.out.println(userInfo);
        }

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        String finalUserInfo = userInfo;
        executor.execute(() -> {
            try {
                String response = RestOperations.sendPut(UPDATE_USER_URL + userId, finalUserInfo);
                handler.post(() -> {
                    if (!response.equals("Error") && !response.isEmpty()) {
                        Intent intent = new Intent(MyInfoActivity.this, MainActivity.class);
//                        intent.putExtra("userJsonObject", response);
                        startActivity(intent);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(MyInfoActivity.this, "Update failed!", Toast.LENGTH_SHORT).show());
            }

        });
    }
}