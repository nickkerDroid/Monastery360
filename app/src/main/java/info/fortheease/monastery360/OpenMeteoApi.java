package info.fortheease.monastery360;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenMeteoApi {
    @GET("v1/forecast")
    Call<OpenMeteoResponse> getForecast(
            @Query("latitude") double lat,
            @Query("longitude") double lon,
            @Query("current") String current,
            @Query("daily") String daily,
            @Query("forecast_days") int days,
            @Query("timezone") String tz
    );
}
