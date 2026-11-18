package com.pbl.parkingsystem.network;


import com.pbl.parkingsystem.util.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

            Request originalRequest = chain.request();
            String path = originalRequest.url().encodedPath();

            // Login and signup are public endpoints
            if (path.equals("/api/users/login")
                    || path.equals("/api/users/register")) {
                return chain.proceed(originalRequest);
            }

            String token = tokenManager.getToken();

            Request.Builder requestBuilder = originalRequest.newBuilder();

            if (token != null && !token.trim().isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            return chain.proceed(requestBuilder.build());
        }
    }


