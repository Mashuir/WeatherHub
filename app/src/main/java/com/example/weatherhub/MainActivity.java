package com.example.weatherhub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherhub.Adapter.WeatherRvAdapter;
import com.example.weatherhub.Model.WeatherRvModel;
import com.example.weatherhub.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    List<WeatherRvModel> list;
    WeatherRvAdapter adapter;

    LocationManager manager;
    int PERMISSION_CODE = 1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        list = new ArrayList<>();
        adapter = new WeatherRvAdapter(MainActivity.this, list);
        binding.idRVWeather.setAdapter(adapter);

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
        }


        Location location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(), location.getLatitude());
        getWeatherInfo(cityName);

        binding.idIVSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String city = binding.idEditCity.getText().toString();

                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter city name", Toast.LENGTH_SHORT).show();
                } else {
                    binding.idTVCityName.setText(cityName);
                    getWeatherInfo(city);
                }

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Please provide the permission", Toast.LENGTH_SHORT).show();
                finish();
            }

        }

    }

    private String getCityName(double longitude, double latitude) {

        String cityName = "Not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());

        try {

            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);

            for (Address adr : addresses) {

                if (adr != null) {
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                    } else {
                        Toast.makeText(MainActivity.this, "User City Not Found", Toast.LENGTH_SHORT).show();
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return cityName;

    }

    private void getWeatherInfo(String cityName) {

        String url = "https://api.weatherapi.com/v1/forecast.json?key=46076643d27f431984684409210711&q="+cityName+"&days=1&aqi=yes&alerts=yes";

        binding.idTVCityName.setText(cityName);

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                binding.idPBLoading.setVisibility(View.GONE);
                binding.idRLHome.setVisibility(View.VISIBLE);

                try {

                    double temperature = response.getJSONObject("current").getDouble("temp_c");
                    int isDay = response.getJSONObject("current").getInt("is_day");

                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");

                    Picasso.get().load("https:".concat(conditionIcon)).into(binding.idIVIcon);
                    binding.idTVTemperature.setText(temperature + "°c");
                    binding.idTVCondition.setText(condition);

                    if (isDay == 1){
                        binding.idRLHome.setBackground(getResources().getDrawable(R.drawable.daybackground));
                       // Picasso.get().load("https://www.freepik.com/free-photo/view-foggy-mountain-landscape_1120953.htm#page=1&query=morning&position=2&from_view=keyword").into(binding.idIVBlack);
                    }else {
                        binding.idRLHome.setBackground(getResources().getDrawable(R.drawable.nightbackground));
                        //Picasso.get().load("https://unsplash.com/photos/L95xDkSSuWw").into(binding.idIVBlack);
                    }

                    //Forecast reader
                    //JSONObject forecastObject = response.getJSONObject();
                    //JSONObject forecast0 = response.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = response.getJSONObject("forecast").getJSONArray("forecastday")
                            .getJSONObject(0).getJSONArray("hour");

                    for (int i=0; i<hourArray.length(); i++){

                        JSONObject hourObject = hourArray.getJSONObject(i);

                        String time = hourObject.getString("time");
                        double temper = hourObject.getDouble("temp_c");
                        String img = hourObject.getJSONObject("condition").getString("icon");
                        String wind = hourObject.getString("wind_kph");

                        list.add(new WeatherRvModel(time,temper,img,wind));
                    }

                    adapter.notifyDataSetChanged();



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, error -> Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show());

        requestQueue.add(jsonObjectRequest);
    }
}