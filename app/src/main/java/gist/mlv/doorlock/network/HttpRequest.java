package gist.mlv.doorlock.network;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/*
 AsyncTask<X, Y, Z>
 X – The type of the input variables value you want to set to the background process. This can be an array of objects.
 Y – The type of the objects you are going to enter in the onProgressUpdate method.
 Z – The type of the result from the operations you have done in the background process.
 */
public class HttpRequest{

    private String TAG = "HTTPLogin";

    public HttpRequest(){
    }

    private String getHtmlLogin(String s){
        String token = "admin:admin";
        byte[] data = token.getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        int responseCode = 0;
        String resBody = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(s);//action url for example changing wifi etc. or open door
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Authorization", "Basic " + base64);

            responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            resBody = sb.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        Log.d(TAG, "login responseCode " + String.valueOf(responseCode));
        return resBody;
    }

    public String checkDevice(String url_s){ // checking whether a device is online, if it is online, return device ID
        URL url;
        HttpURLConnection urlConnection = null;
        int responseCode = 0;
        String resBody = "";
        int timeout = 500;
        try {
            url = new URL(url_s);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(timeout);
            InputStream in = urlConnection.getInputStream();
            responseCode = urlConnection.getResponseCode();
            InputStreamReader isw = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isw);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            resBody = sb.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        Log.d(TAG, "checkDevice responseCode " + String.valueOf(responseCode));
        return resBody;
    }

    public boolean credentialRequest(String[] urls){ // checking a match between IP and device ID
        URL url;
        HttpURLConnection urlConnection = null;
        int responseCode = 0;
        String resBody = "";
        boolean isSuccess = false;
        int timeout = 100;
        try {
            url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(timeout);
            InputStream in = urlConnection.getInputStream();
            responseCode = urlConnection.getResponseCode();
            InputStreamReader isw = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isw);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            resBody = sb.toString();
            // send request change password
            if(resBody.contains("True")){
                String token = "admin:admin";
                byte[] data = token.getBytes(StandardCharsets.UTF_8);
                String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                url = new URL(urls[1]);//action url for example changing wifi etc. or open door
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(timeout);
                urlConnection.setRequestProperty("Authorization", "Basic " + base64);

                if(urlConnection.getResponseCode() == 200){
                    urlConnection.disconnect();
                    isSuccess = true;
                }
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        Log.d(TAG, "checkDevice responseCode " + String.valueOf(responseCode));
        return isSuccess;
    }
}