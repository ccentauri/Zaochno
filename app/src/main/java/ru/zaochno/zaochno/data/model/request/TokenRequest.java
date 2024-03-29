package ru.zaochno.zaochno.data.model.request;

import com.google.gson.annotations.SerializedName;

public class TokenRequest implements BaseTokenRequest {
    @SerializedName("token")
    protected String token;

    public TokenRequest() {
    }

    public TokenRequest(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }
}
