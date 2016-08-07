package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.YahooChartApiService;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by manas on 7/31/16.
 */
public class GraphActivity extends AppCompatActivity {

    private static final String RANGE = "60d";

    private TextView errorMessage;
    private ProgressBar progressCircle;
    private ValueLineChart valueLineChart;

    private boolean isLoaded = false;
    private String stockSymbol;
    private String companyName;
    private ArrayList<String> labels;
    private ArrayList<Float> values;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        errorMessage = (TextView) findViewById(R.id.error_text);
        progressCircle = (ProgressBar) findViewById(R.id.progress_bar);
        valueLineChart = (ValueLineChart) findViewById(R.id.linechart);

        stockSymbol = getIntent().getStringExtra("stockSymbol");
        if (savedInstanceState == null) {
            getHistoricalData();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isLoaded) {
            outState.putString("companyName", companyName);
            outState.putStringArrayList("labels", labels);

            float[] valuesArray = new float[values.size()];
            for (int i = 0; i < valuesArray.length; i++) {
                valuesArray[i] = values.get(i);
            }
            outState.putFloatArray("values", valuesArray);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("companyName")) {
            isLoaded = true;

            companyName = savedInstanceState.getString("companyName");
            labels = savedInstanceState.getStringArrayList("labels");
            values = new ArrayList<>();

            float[] valuesArray = savedInstanceState.getFloatArray("values");
            for (float f : valuesArray) {
                values.add(f);
            }
            onNetworkRequestComplete();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return false;
        }
    }


    private void getHistoricalData() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://chartapi.finance.yahoo.com/instrument/1.0/")
                .client(new okhttp3.OkHttpClient())
                .build();
        YahooChartApiService service = retrofit.create(YahooChartApiService.class);
        service.gethistoricalData(stockSymbol, RANGE)
                .enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {

                                parseJsonResponse(response);

                                onNetworkRequestComplete();
                            } catch (IOException | JSONException e) {
                                onNetworkRequestFailed();
                            }
                        } else {
                            onNetworkRequestFailed();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        onNetworkRequestFailed();
                    }
                });
    }

    private void parseJsonResponse(Response<ResponseBody> response) throws IOException, JSONException {
        String result = response.body().string();
        if (result.startsWith("finance_charts_json_callback( ")) {
            result = result.substring(29, result.length() - 2);
        }
        JSONObject object = new JSONObject(result);
        companyName = object.getJSONObject("meta").getString("Company-Name");
        labels = new ArrayList<>();
        values = new ArrayList<>();
        JSONArray series = object.getJSONArray("series");
        for (int i = 0; i < series.length(); i++) {
            JSONObject seriesItem = series.getJSONObject(i);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String date = format.format(seriesItem.getLong("Timestamp") * 1000);
            labels.add(date);
            values.add(Float.parseFloat(seriesItem.getString("close")));
        }
    }

    private void onNetworkRequestComplete() {

        setTitle(companyName);

        progressCircle.setVisibility(View.GONE);
        errorMessage.setVisibility(View.GONE);

        ValueLineSeries series = new ValueLineSeries();
        series.setColor(0xFF63CBB0);
        for (int i = 0; i < labels.size(); i++) {
            series.addPoint(new ValueLinePoint(labels.get(i), values.get(i)));
        }
        if (!isLoaded) {
            valueLineChart.startAnimation();
        }
        valueLineChart.addSeries(series);
        valueLineChart.setVisibility(View.VISIBLE);

        isLoaded = true;
    }

    private void onNetworkRequestFailed() {
        valueLineChart.setVisibility(View.GONE);
        progressCircle.setVisibility(View.GONE);
        errorMessage.setVisibility(View.VISIBLE);
        setTitle(getResources().getString(R.string.error));
    }
}
