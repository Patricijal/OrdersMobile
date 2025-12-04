package com.example.kursinis.activities;

import static com.example.kursinis.utils.Constants.CREATE_BASIC_USER_URL;
import static com.example.kursinis.utils.Constants.CREATE_DRIVER_URL;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kursinis.R;
import com.example.kursinis.model.Driver;
import com.example.kursinis.model.VehicleType;
import com.example.kursinis.utils.LocalDateAdapter;
import com.example.kursinis.utils.LocalDateTimeSerializer;
import com.example.kursinis.utils.LocalDateTypeAdapter;
import com.example.kursinis.utils.RestOperations;
import com.example.kursinis.model.BasicUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void createNewUser(View view) {
        TextView username = findViewById(R.id.regLoginField);
        TextView password = findViewById(R.id.regPasswordField);
        TextView name = findViewById(R.id.regNameField);
        TextView surname = findViewById(R.id.regSurnameField);
        TextView phoneNum = findViewById(R.id.regPhoneNumField);
        TextView address = findViewById(R.id.regAddressField);
        TextView license = findViewById(R.id.regLicenseField);
        TextView bDate = findViewById(R.id.regBirthDateField);
        TextView vehicleType = findViewById(R.id.regVehicleTypeField);

        CheckBox isDriverCheckbox = findViewById(R.id.regIsDriver);
        boolean isDriver = isDriverCheckbox.isChecked();

        GsonBuilder build = new GsonBuilder();
        build.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        build.registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter());
        Gson gson = build.setPrettyPrinting().create();

        String userInfo = "{}";
        if (isDriver) {
            LocalDate birthDate = LocalDate.parse(bDate.getText().toString());
            VehicleType vehicleTypeEnum = VehicleType.valueOf(vehicleType.getText().toString().toUpperCase());
            Driver driver = new Driver(
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
            userInfo = gson.toJson(driver, Driver.class);
            System.out.println(userInfo);
        } else {
            BasicUser basicUser = new BasicUser(
                    username.getText().toString(),
                    password.getText().toString(),
                    name.getText().toString(),
                    surname.getText().toString(),
                    phoneNum.getText().toString(),
                    address.getText().toString()
            );
            userInfo = gson.toJson(basicUser, BasicUser.class);
            System.out.println(userInfo);
        }

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        String finalUserInfo = userInfo;
        executor.execute(() -> {
            String response = "";
            try {
                if (isDriver) {
                    response = RestOperations.sendPost(CREATE_DRIVER_URL, finalUserInfo);
                } else {
                    response = RestOperations.sendPost(CREATE_BASIC_USER_URL, finalUserInfo);
                }
                String finalResponse = response;
                handler.post(() -> {
                    if (!finalResponse.equals("Error") && !finalResponse.isEmpty()) {
                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            } catch (IOException e) {
                //Toast reikes
            }

        });
    }
}