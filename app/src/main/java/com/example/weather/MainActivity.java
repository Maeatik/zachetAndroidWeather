package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;

    private TextView day;
    private TextView daytemp;
    private TextView city;
    private TextView nighttemp;
    private TextView pressure;

    public  double latitude;
    public double longitude;
    private int dayNum;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        city = findViewById(R.id.textView);
        day = findViewById(R.id.dayView);
        daytemp = findViewById(R.id.textView2);
        nighttemp = findViewById(R.id.textView3);
        pressure = findViewById(R.id.textView4);



        loadLocation();

        findViewById(R.id.mainPane).setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {

            public void onSwipeRight() {
                dayNum--;
                if (dayNum < 0) dayNum = 29;

                String url = "https://pro.openweathermap.org/data/2.5/forecast/climate?lat=" +latitude+"&lon="+longitude+"&appid=ee318df497020f077901717e7b51cced&units=metric";
                new GetUrlData().execute(url);
            }

            public void onSwipeLeft() {
                dayNum++;
                if (dayNum > 29) dayNum = 0;

                String url = "https://pro.openweathermap.org/data/2.5/forecast/climate?lat=" +latitude+"&lon="+longitude+"&appid=ee318df497020f077901717e7b51cced&units=metric";
                new GetUrlData().execute(url);
            }

            public void onSwipeBottom() {
                String url = "https://pro.openweathermap.org/data/2.5/forecast/climate?lat=" +latitude+"&lon="+longitude+"&appid=ee318df497020f077901717e7b51cced&units=metric";
                new GetUrlData().execute(url);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void loadLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, location -> {
        });

        latitude = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
        longitude = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();

        String url = "https://pro.openweathermap.org/data/2.5/forecast/climate?lat=" +latitude+"&lon="+longitude+"&appid=ee318df497020f077901717e7b51cced&units=metric";
        dayNum = 0;
        new GetUrlData().execute(url);
    }


    private class GetUrlData extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            daytemp.setText("...");
            city.setText("...");
            pressure.setText("...");
            nighttemp.setText("...");
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                System.out.println(buffer);
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject obj = new JSONObject(result);

                city.setText(obj.getJSONObject("city").getString("name"));
                day.setText(String.valueOf(dayNum +1));
                daytemp.setText("Дневная температура " + obj.getJSONArray("list").getJSONObject(dayNum).getJSONObject("temp").getDouble("day") + "C");

                nighttemp.setText("Ночная температура " + obj.getJSONArray("list").getJSONObject(dayNum).getJSONObject("temp").getDouble("night") + "C");
                pressure.setText("Давление атм: " +  obj.getJSONArray("list").getJSONObject(dayNum).getDouble("pressure"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}