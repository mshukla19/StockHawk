package com.sam_chordas.android.stockhawk.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by manas on 7/31/16.
 */
public interface YahooChartApiService {
    @GET("{name}/chartdata;type=quote;range={range}/json")
    Call<ResponseBody> gethistoricalData(@Path("name") String name, @Path("range") String range);
}
