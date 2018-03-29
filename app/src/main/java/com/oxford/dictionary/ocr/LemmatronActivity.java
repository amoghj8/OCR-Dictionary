package com.oxford.dictionary.ocr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by amogh on 20/2/18.
 */

public class LemmatronActivity extends AppCompatActivity {

    public static final String fake = null;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lemmatron);
        new CallbackTask().execute(inflections());
    }

    private String inflections() {
        final String language = "en";
        String term = getIntent().getStringExtra("Term");
        final String word_id = term.toLowerCase(); //word id is case sensitive and lowercase is required
        return "https://od-api.oxforddictionaries.com:443/api/v1/inflections/" + language + "/" + word_id;
    }

    //in android calling network requests on the main thread forbidden by default
    //create class to do async job
    private class CallbackTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(LemmatronActivity.this);
            pDialog.setMessage("Find root word...");
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected String doInBackground(String... params) {

            //TODO: replace with your own app id and app key
            final String app_id = "96dbeab9";
            final String app_key = "08f327a5eddbe15635328018c2b503d0";
            try {
                URL url = new URL(params[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.setRequestProperty("app_id",app_id);
                urlConnection.setRequestProperty("app_key",app_key);

                // read the output from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                JSONObject jsonObject = null;
                jsonObject = new JSONObject(stringBuilder.toString());
                JSONArray resultArray = jsonObject.getJSONArray("results");
                JSONObject resultObject = resultArray.getJSONObject(0);
                JSONArray lexicalArray = resultObject.getJSONArray("lexicalEntries");
                JSONObject lexicalObject = lexicalArray.getJSONObject(0);
                JSONArray inflectionArray = lexicalObject.getJSONArray("inflectionOf");
                JSONObject inflectionObject = inflectionArray.getJSONObject(0);
                String text_lemmatron = inflectionObject.getString("text");
                Log.v("text -lemmatron",text_lemmatron);
                return text_lemmatron;

            }
            catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            Intent intent = new Intent(LemmatronActivity.this, DefinitionActivity.class);
            intent.putExtra("Term",result);
            startActivity(intent);
        }
    }

}
