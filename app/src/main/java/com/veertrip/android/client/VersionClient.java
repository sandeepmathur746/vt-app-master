package com.veertrip.android.client;

import org.json.JSONObject;
import java.util.Map;

public class VersionClient extends ARestClient {

    public static final int ACTIVITY_CODE = 101;
    private IOnRequestCompleted activity;

    public VersionClient (IOnRequestCompleted activity) {
        this.activity = activity;
    }

    @Override
    protected String getAbsoluteURL() {
        return "https://www.veertrip.com/app/version.json";
    }

    @Override
    protected void onRequestComplete(String object) {
        try {
            JSONObject response = new JSONObject(object);
            this.activity.onRequestComplete(ACTIVITY_CODE, response);
        } catch (Exception ignored) {}
    }

    @Override
    protected Map<String, String> getHeaders() {
        return null;
    }

    @Override
    protected String getBody() {
        return null;
    }

    @Override
    protected String getMethod() {
        return ARestClient.GET;
    }
}
