package info.fortheease.monastery360;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherBottomSheet extends BottomSheetDialogFragment {
    private TextView tvCurrent, tvForecast;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_weather, container, false);
        tvCurrent = view.findViewById(R.id.tvCurrent);
        tvForecast = view.findViewById(R.id.tvForecast);

        fetchWeather();
        return view;
    }

    private void fetchWeather() {
        OpenMeteoApi api = ApiClient.getService();
        api.getForecast(
                27.33194, 88.60194,
                "temperature_2m,weather_code,wind_speed_10m",
                "temperature_2m_max,temperature_2m_min",
                3,
                "Asia/Kolkata"
        ).enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoResponse> call, Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OpenMeteoResponse data = response.body();
                    if (data.current != null) {
                        tvCurrent.setText("Now: " + data.current.temperature2m.intValue() + "°C, " +
                                "Wind " + data.current.windSpeed10m.intValue() + " km/h");
                    }
                    if (data.daily != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < data.daily.time.size(); i++) {
                            sb.append(data.daily.time.get(i))
                                    .append(": Max ")
                                    .append(data.daily.tMax.get(i).intValue())
                                    .append("° / Min ")
                                    .append(data.daily.tMin.get(i).intValue())
                                    .append("°\n");
                        }
                        tvForecast.setText(sb.toString());
                    }
                } else {
                    tvCurrent.setText("Error loading weather.");
                }
            }

            @Override
            public void onFailure(Call<OpenMeteoResponse> call, Throwable t) {
                tvCurrent.setText("Failed: " + t.getMessage());
            }
        });
    }
}
