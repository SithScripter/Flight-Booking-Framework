package com.demo.flightbooking.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.testng.annotations.DataProvider;

import com.demo.flightbooking.model.Passenger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class JsonDataProvider {

    // Correct the path to reflect the 'testdata' folder within resources (NO HYPHEN)
    private static final String JSON_FILE = "testdata/passengers.json"; // <--- Corrected path

    @DataProvider(name = "passengerData")
    public static Object[][] getPassengerData() throws Exception {
        Gson gson = new Gson();
        Type type = new TypeToken<Passenger[]>() {}.getType();

        // Use getResourceAsStream for classpath resources
        try (InputStream is = JsonDataProvider.class.getClassLoader().getResourceAsStream(JSON_FILE);
             InputStreamReader reader = new InputStreamReader(is)) {
            
            if (is == null) {
                throw new RuntimeException("JSON file not found on classpath: " + JSON_FILE);
            }

            Passenger[] passengers = gson.fromJson(reader, type);

            Object[][] data = new Object[passengers.length][1];
            for (int i = 0; i < passengers.length; i++) {
                data[i][0] = passengers[i];
            }
            return data;
        }
    }
}