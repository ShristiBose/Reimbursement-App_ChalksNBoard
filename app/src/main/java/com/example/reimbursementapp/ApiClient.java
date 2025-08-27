// In ApiClient.java

package com.example.reimbursementapp;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://5200f0d34222.ngrok-free.app";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    // ✅ CHANGED: Use a singleton pattern to prevent re-creating objects
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }

    // ✅ ADDED: A method to create an instance with an auth token when needed
    public static ApiService getAuthenticatedApiService(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);

        if (token == null) {
            // Or handle this case as an error, e.g., redirect to login
            return getApiService();
        }

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.addInterceptor(chain -> {
            okhttp3.Request original = chain.request();
            okhttp3.Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(request);
        });

        // Create a new Retrofit instance ONLY for authenticated calls
        Retrofit authRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build();

        return authRetrofit.create(ApiService.class);
    }

    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
