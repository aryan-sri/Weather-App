package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRl;
    private ProgressBar loadingPB;
    private TextView cityNameTV , conditionTV , temperatureTV;
    private TextInputEditText cityEdt;
    private RecyclerView weatherRV;
    private ImageView backIV , iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModelArrayList;
    private WeatherRVAdapter weatherAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE =1;
    private String cityName;
    private Double latitude, longitude;

    private static final boolean AUTO_HIDE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        homeRl = findViewById(R.id.idRlHome);
        loadingPB = findViewById(R.id.idPBloading);
        cityNameTV = findViewById(R.id.idTVCityname);
        temperatureTV = findViewById(R.id.Temperature);
        conditionTV = findViewById(R.id.Condition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIicon);
        searchIV = findViewById(R.id.idVIsearch);
        weatherRVModelArrayList = new ArrayList<>();
        weatherAdapter = new WeatherRVAdapter(this,weatherRVModelArrayList);
        weatherRV.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION ,Manifest.permission.ACCESS_COARSE_LOCATION },PERMISSION_CODE );

        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude() , location.getLatitude());

        getWeatherInfo(cityName);
        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdt.getText().toString();
                if(city.isEmpty())
                {
                    Toast.makeText(MainActivity.this, "Please Enter City Name", Toast.LENGTH_SHORT).show();
                }
                else
                {

                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }

        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE)
        {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Please Provide the Permissions", Toast.LENGTH_SHORT).show();
                finish();
            }


        }
    }

    private String getCityName(double longitude , double latitude)
    {
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses= gcd.getFromLocation(latitude , longitude , 1);


            cityName = addresses.get(0).getAddressLine(0);
            return cityName;
//            for(Address adr : addresses)
//            {
//                if(adr!= null)
//                {
//                    String city = adr.getLocality();
//                    if(city!= null && !city.equals(""))
//                    {
//                        cityName = city;
//                    }
//                    else
//                    {
//                        Log.d("TAG", "CITY NOT FOUND");
//                        Toast.makeText(this, "User City Not Found", Toast.LENGTH_SHORT).show();
//                        cityName = "Delhi";
//                    }
//                }
//            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return cityName;
    }



    private void getWeatherInfo(String cityName)
    {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=45fe0b227be24d5d8fc130012222611&q="+cityName+"&days=1&aqi=yes&alerts=yes";

        cityNameTV.setText(cityName);
        RequestQueue reqQ = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRl.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if(isDay ==1)
                    {
                        Picasso.get().load("https://th.bing.com/th/id/R.1da354ede0ae108ac23cf0d66c7e1c94?rik=s4GgHuSoF9wFMQ&riu=http%3a%2f%2fimages.unsplash.com%2fphoto-1465156799763-2c087c332922%3fcrop%3dentropy%26cs%3dtinysrgb%26fit%3dmax%26fm%3djpg%26ixid%3dMnwxMjA3fDB8MXxzZWFyY2h8MXx8bW9ybmluZyUyMGNsb3Vkc3x8MHx8fHwxNjE4OTg4MzM4%26ixlib%3drb-1.2.1%26q%3d80%26w%3d1080&ehk=jkPpjaqUNEsnNX%2b9xa3vUJdor7v6suJtB15A8MBs3uU%3d&risl=&pid=ImgRaw&r=0").into(backIV);
                    }
                    else
                    {
                        Picasso.get().load("https://recoverit.wondershare.com/uploads/best-android-wallpaper-01.jpg").into(backIV);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastO.getJSONArray("hour");

                    for(int i=0;i<hourArray.length();i++)
                    {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img  = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");

                        weatherRVModelArrayList.add(new WeatherRVModal(time,temper,img,wind));
                    }

                    weatherAdapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please Enter Valid City Name...", Toast.LENGTH_SHORT).show();
            }
        });
        reqQ.add(jsonObjectRequest);
    }
}