package com.veertrip.android.client;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public abstract class ARestClient extends AsyncTask<Void, Void, String> {

    protected static final String POST = "POST";
    protected static final String GET = "GET";
    protected static final String DELETE = "DELETE";

    private final String LOG_TAG = this.getClass().getSimpleName();
    protected abstract String getAbsoluteURL();
    protected abstract void onRequestComplete(String object);
    protected abstract Map<String, String> getHeaders();
    protected abstract String getBody();
    protected abstract String getMethod();

    public ARestClient() {

    }

    @Override
    protected void onPostExecute(String response) {
        onRequestComplete(response);
    }

    public String run() {
        String urlString = getAbsoluteURL();
        Log.d(LOG_TAG, urlString);
        HttpURLConnection connection = null;
        URL url;
        String object = null;
        InputStream inStream = null;
        OutputStreamWriter writer = null;
        try {
            url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            if (getHeaders() != null) {
                for (Map.Entry<String, String> entry : getHeaders().entrySet())
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            if (getMethod().equals(POST)) {
                connection.setRequestMethod(POST);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(getBody());
                Log.d(LOG_TAG, getBody());
                writer.flush();

            } else if (getMethod().equals(DELETE)) {
                //connection.setDoOutput(true);
                connection.setRequestMethod("DELETE");
                connection.connect();

            } else if (getMethod().equals(GET)) {
                connection.setRequestMethod(GET);
                connection.setDoOutput(false);
                connection.setDoInput(true);
                connection.connect();
            }

            Log.d(LOG_TAG, String.valueOf(connection.getResponseCode()));

            inStream = connection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp;
            StringBuilder response = new StringBuilder();
            while ((temp = bReader.readLine()) != null) {
                response.append(temp);
            }
            Log.d(LOG_TAG, response.toString());
            object = response.toString();

            if (getMethod().equals(POST) && writer != null) {
                writer.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return object;
    }

    @Override
    protected String doInBackground(Void... params) {
        return run();
    }

}
