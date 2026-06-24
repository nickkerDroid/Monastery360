package info.fortheease.monastery360;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OpenMeteoResponse {
    public Current current;
    public Daily daily;

    public static class Current {
        public String time;
        @SerializedName("temperature_2m") public Double temperature2m;
        @SerializedName("weather_code") public Integer weatherCode;
        @SerializedName("wind_speed_10m") public Double windSpeed10m;
    }

    public static class Daily {
        public List<String> time;
        @SerializedName("temperature_2m_max") public List<Double> tMax;
        @SerializedName("temperature_2m_min") public List<Double> tMin;
    }
}
