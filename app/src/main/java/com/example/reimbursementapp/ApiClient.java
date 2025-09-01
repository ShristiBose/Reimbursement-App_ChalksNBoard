package com.example.reimbursementapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ApiClient {

    private static final String BASE_URL = "https://b1fc30a8b8c2.ngrok-free.app";
    private static Retrofit retrofit = null;


    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
                String dateStr = json.getAsString();
                String[] formats = {
                        "dd MMM yyyy, HH:mm",    // e.g. 31 Aug 2025, 20:45
                        "yyyy-MM-dd'T'HH:mm:ss", // e.g. 2025-08-31T20:45:00
                        "dd-MM-yyyy HH:mm",      // e.g. 31-08-2025 20:45
                        "MMM dd, yyyy h:mm a"    // e.g. Aug 31, 2025 8:45 PM
                };
                for (String format : formats) {
                    try {
                        return new SimpleDateFormat(format, Locale.ENGLISH).parse(dateStr);
                    } catch (Exception ignored) {}
                }
                return null;
            })
            .create();

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    public static ApiService getAuthenticatedApiService(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (token != null) {
            clientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(newRequest);
                }
            });
        }

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService.class);
    }
}
