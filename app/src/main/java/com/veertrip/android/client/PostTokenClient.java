package com.veertrip.android.client;

import androidx.collection.ArrayMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class PostTokenClient extends ARestClient {

    private String userID;
    private String token;
    private String topic;

    public PostTokenClient(String userID, String token, String topic) {
        super();
        this.userID = userID;
        this.token = token;
        this.topic = topic;
    }

    @Override
    protected String getAbsoluteURL() {
        return "https://vt-account.veertrip.com/api/v1/account/notifications/subscribe";
    }

    @Override
    protected void onRequestComplete(String object) {

    }

    @Override
    protected Map<String, String> getHeaders() {
        Map<String, String> headers = new ArrayMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Override
    protected String getBody() {
        try {
            JSONObject body = new JSONObject();
            body.put("token", token);
            body.put("userId", userID);
            body.put("topic", topic);
            return body.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected String getMethod() {
        return ARestClient.POST;
    }
}
