package info.fortheease.monastery360;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import info.fortheease.monastery360.monastaries.DubdiFragment;
import info.fortheease.monastery360.monastaries.EncheyFragment;
import info.fortheease.monastery360.monastaries.LachungFragment;
import info.fortheease.monastery360.monastaries.PhodongFragment;
import info.fortheease.monastery360.monastaries.RumtekFragment;
import io.kommunicate.KmConversationBuilder;
import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KmCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private BottomSheetBehavior<FrameLayout> weatherBottomSheetBehavior;
    private FrameLayout weatherBottomSheet;
    private TextView tvCurrentWeather, tvWeatherForecast, tvCurrentTemperatureLarge, tvWeatherDescription;
    private FloatingActionButton chatBtn, homeBtn,offMapBtn;
    private Spinner monasterySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initialize Kommunicate with your app id from dashboard
        Kommunicate.init(this, BuildConfig.KOMMUNICATE_APP_ID);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        chatBtn = findViewById(R.id.chatBtn);
        weatherBottomSheet = findViewById(R.id.weather_bottom_sheet);
        homeBtn = findViewById(R.id.fab_center);
        monasterySpinner = findViewById(R.id.monastery_spinner);
        offMapBtn = findViewById(R.id.offMapBtn);
        weatherBottomSheetBehavior = BottomSheetBehavior.from(weatherBottomSheet);

        // Initialize TextViews from the included layout
        tvCurrentTemperatureLarge = weatherBottomSheet.findViewById(R.id.tvCurrentTemperatureLarge);
        tvWeatherDescription = weatherBottomSheet.findViewById(R.id.tvWeatherDescription);
        tvCurrentWeather = weatherBottomSheet.findViewById(R.id.tvCurrent); // This is for wind speed now
        tvWeatherForecast = weatherBottomSheet.findViewById(R.id.tvForecast);

        // Set initial state for the bottom sheet to collapsed (peeking)
        weatherBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.fab_place_center );
            String selectedMonastery = monasterySpinner.getSelectedItem().toString();
            Fragment fragment = getMonasteryFragment(selectedMonastery);
            loadFragment(fragment, false);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            boolean addToBackStack = true; // Default to true
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_travel_hotels) {
                fragment = new TravelTripsFragment();
            } else if (itemId == R.id.navigation_monastery_info) {
                fragment = new PermitsFragment();
            } else if (itemId == R.id.navigation_calendar_events) {
                fragment = new EventsFragment();
            } else if (itemId == R.id.navigation_cabs){
                fragment = new CabsFragment();
            } else if (itemId == R.id.fab_place_center) {
                fragment = new HomeFragment();
                addToBackStack = false; // Home should not be on backstack
            }

            if (fragment != null) {
                loadFragment(fragment, addToBackStack);
                return true;
            }
            return false;
        });

        monasterySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMonastery = parent.getItemAtPosition(position).toString();
                Fragment fragment = getMonasteryFragment(selectedMonastery);
                if (fragment != null) {
                    loadFragment(fragment, true); // Add to backstack when spinner changes monastery info
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Interface callback
            }
        });

        weatherBottomSheet.setOnClickListener(v -> {
            if (weatherBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                weatherBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                weatherBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        chatBtn.setOnClickListener(view -> {
            new KmConversationBuilder(this)
                    .setConversationTitle("Support")
                    .setSingleConversation(false)
                    .launchConversation(new KmCallback() {
                        @Override public void onSuccess(Object msg) { /* saved conversation id */ }
                        @Override public void onFailure(Object err) { /* handle error */ }
                    });
        });

        homeBtn.setOnClickListener(view -> {
            // Just set the selected item, the listener will handle fragment loading
            bottomNavigationView.setSelectedItemId(R.id.fab_place_center);
            String selectedMonastery = monasterySpinner.getSelectedItem().toString();
            Fragment fragment = getMonasteryFragment(selectedMonastery);
            loadFragment(fragment, false);
        });

        offMapBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, TestActivity.class));
        });

        requestLocationPermissions();

        fetchWeather();
    }

    public void setMonasterySpinnerEnabled(boolean enabled) {
        if (monasterySpinner != null) {
            monasterySpinner.setEnabled(enabled);
            monasterySpinner.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }

    private Fragment getMonasteryFragment(String monasteryName) {
        switch (monasteryName) {
            case "Rumtek":
                return new RumtekFragment();
            case "Lachung":
                return new LachungFragment();
            case "Enchey":
                return new EncheyFragment();
            case "Phodong":
                return new PhodongFragment();
            case "Dubdi":
                return new DubdiFragment();
            default:
                return new HomeFragment(); // Fallback or a generic MonasteryInfoFragment
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    private String getWeatherDescription(int weatherCode) {
        // Basic WMO Weather interpretation codes (extend as needed)
        // See: https://open-meteo.com/en/docs#weathervariables
        switch (weatherCode) {
            case 0: return "Clear sky";
            case 1: return "Mainly clear";
            case 2: return "Partly cloudy";
            case 3: return "Overcast";
            case 45: return "Fog";
            case 48: return "Depositing rime fog";
            case 51: return "Light drizzle";
            case 53: return "Moderate drizzle";
            case 55: return "Dense drizzle";
            case 56: return "Light freezing drizzle";
            case 57: return "Dense freezing drizzle";
            case 61: return "Slight rain";
            case 63: return "Moderate rain";
            case 65: return "Heavy rain";
            // Add more cases for other codes (snow, thunderstorms, etc.)
            default: return "Weather code: " + weatherCode;
        }
    }

    private void fetchWeather() {
        OpenMeteoApi api = ApiClient.getService();
        api.getForecast(
                27.33194, 88.60194, // Rumtek Monastery coordinates
                "temperature_2m,weather_code,wind_speed_10m",
                "temperature_2m_max,temperature_2m_min",
                3, // Forecast for 3 days
                "Asia/Kolkata"
        ).enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoResponse> call, Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OpenMeteoResponse data = response.body();
                    if (data.current != null) {
                        if (tvCurrentTemperatureLarge != null) {
                            tvCurrentTemperatureLarge.setText(String.format("%d°C", data.current.temperature2m.intValue()));
                        }
                        if (tvWeatherDescription != null && data.current.weatherCode != null) {
                            tvWeatherDescription.setText(getWeatherDescription(data.current.weatherCode));
                        }
                        if (tvCurrentWeather != null) { // This is R.id.tvCurrent, now for wind
                            tvCurrentWeather.setText(String.format("Wind: %d km/h", data.current.windSpeed10m.intValue()));
                        }
                    }
                    if (data.daily != null && tvWeatherForecast != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < data.daily.time.size(); i++) {
                            sb.append(data.daily.time.get(i))
                                    .append(": Max ")
                                    .append(data.daily.tMax.get(i).intValue())
                                    .append("° / Min ")
                                    .append(data.daily.tMin.get(i).intValue())
                                    .append("°\n");
                        }
                        tvWeatherForecast.setText(sb.toString());
                    }
                } else {
                    String errorMsg = "Error loading weather.";
                    if (tvCurrentTemperatureLarge != null) tvCurrentTemperatureLarge.setText("--°C");
                    if (tvWeatherDescription != null) tvWeatherDescription.setText("N/A");
                    if (tvCurrentWeather != null) tvCurrentWeather.setText(errorMsg);
                    if (tvWeatherForecast != null) tvWeatherForecast.setText("");
                }
            }

            @Override
            public void onFailure(Call<OpenMeteoResponse> call, Throwable t) {
                String failMsg = "Failed to load weather";
                if (tvCurrentTemperatureLarge != null) tvCurrentTemperatureLarge.setText("--°C");
                if (tvWeatherDescription != null) tvWeatherDescription.setText("N/A");
                if (tvCurrentWeather != null) tvCurrentWeather.setText(failMsg);
                if (tvWeatherForecast != null) tvWeatherForecast.setText("");
            }
        });
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    101);
        }
    }
}
