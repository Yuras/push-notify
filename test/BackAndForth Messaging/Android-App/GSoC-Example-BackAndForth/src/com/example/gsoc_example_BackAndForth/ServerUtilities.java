package com.example.gsoc_example_BackAndForth;

import static com.example.gsoc_example_BackAndForth.CommonUtilities.SERVER_URL;
import static com.example.gsoc_example_BackAndForth.CommonUtilities.SENDER_ID;
import static com.example.gsoc_example_BackAndForth.CommonUtilities.displayMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.gsoc_example_BackAndForth.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

//Class for communicating with Yesod and GCM servers.
public final class ServerUtilities {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();
    
    // Tag used on log messages
    static final String TAG = "GSoC-Example-ServerUtilities";
    
    //Register this device in the server.
    static boolean register(final Context context,String regId,String user,String password) {
    	
    	String serverUrl = SERVER_URL + "/fromdevices/register";
    	Log.i(TAG, "registering device (regId = " + regId + ")");
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("user", user);
        params.put("password", password);
        params.put("system", "ANDROID");
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {
            	displayMessage(context, context.getString(
                        R.string.server_registering, i, MAX_ATTEMPTS));
                post(serverUrl, params);
                String message = context.getString(R.string.server_registered);
                displayMessage(context, message);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Failed to register on attempt " + i, e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                	// Activity finished before we complete - exit.
                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return false;
                }
                // increase backoff exponentially.
                backoff *= 2;
            }
        }
        String message = context.getString(R.string.server_register_error,
                MAX_ATTEMPTS);
        displayMessage(context, message);
        return false;
    }
    

    // Send a message to the server.
    static void sendMsgToServer(final Context context,String regId, String user, String password , String msg , AtomicInteger msgId) {
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    	Boolean useCCS = sharedPref.getBoolean("pref_useCCS", false);
    	Log.i(TAG, "sending msg to server");
        String serverUrl = SERVER_URL + "/fromdevices/messages";
        if(useCCS){
        	String id = Integer.toString(msgId.incrementAndGet());
        	Bundle params = new Bundle();
        	params.putString("NewMessage",msg);
            params.putString("regId", regId);
            params.putString("user",user);
            params.putString("password",password);
            params.putString("system", "ANDROID");
        	GoogleCloudMessaging gcm= GoogleCloudMessaging.getInstance(context);
        	try {
        		gcm.send(SENDER_ID + "@gcm.googleapis.com", id, 0, params);
        	} catch (IOException ex) {
        		displayMessage(context,"The message couldn't be sent.");
            }
        }
        else{
        	Map<String, String> params = new HashMap<String, String>();
            params.put("NewMessage",msg);
            params.put("regId", regId);
            params.put("user",user);
            params.put("password",password);
            params.put("system", "ANDROID");
            long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
	        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
	            try {
	            	post(serverUrl, params);
	            	displayMessage(context,"Message successfully sent.");
	                break;
	            } catch (IOException e) {
	            	if(i == MAX_ATTEMPTS){
	            		displayMessage(context,"The message couldn't be sent.");
	            		break;
	            	}
	            	try {
	                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
	                    Thread.sleep(backoff);
	                } catch (InterruptedException e1) {
	                    Log.d(TAG, "Problem sleeping");
	                }
	            	// increase backoff exponentially.
	                if (backoff<200000) backoff*= 2;
	            }
	        }
        }
    }
    
    // Issue a POST request to the server.
    private static void post(String endpoint, Map<String, String> params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        
        JSONObject holder = new JSONObject();
        
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            try {
            	holder.put(param.getKey(),param.getValue());
            } catch(JSONException e){
            	throw new IllegalArgumentException(e);	
            }     
        }
        
        String body = holder.toString();
        Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json");
            
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            // handle the response.
            for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            try
            {
            	int status=200;
            	status = conn.getResponseCode();
            if (status != 200) {
              throw new IOException("Post failed with error code " + status);
            }
            break;
            }catch(java.io.EOFException e){}
            }
            
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
      }
}
