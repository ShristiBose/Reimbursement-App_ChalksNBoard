package com.example.reimbursementapp;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://631c455f1749.ngrok-free.app";
    private static Retrofit retrofit = null;

    public static ApiService getApiService(String token) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        if (token != null && !token.isEmpty()) {
            clientBuilder.addInterceptor(chain -> {
                okhttp3.Request original = chain.request();
                okhttp3.Request request = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(request);
            });
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build();

        return retrofit.create(ApiService.class);
    }
}
