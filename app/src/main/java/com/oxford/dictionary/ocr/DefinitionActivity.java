package com.oxford.dictionary.ocr;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;



public class DefinitionActivity extends AppCompatActivity  {
    private Toolbar toolbar;
    private ListView lv;
    ArrayList<HashMap<String, String>> definitionList;
    private ProgressDialog pDialog;

    private static final String DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/com.ltapps.textscanner/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definition);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setElevation(toolbar, 10);
        ViewCompat.setElevation((ListView) findViewById(R.id.list), 10);
        definitionList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);
        new DefinitionActivity.CallbackTask().execute(dictionaryEntries());
    }


    private String dictionaryEntries() {
        final String language = "en";
        String term = getIntent().getStringExtra("Term");
        final String word = term;
        final String word_id = word.toLowerCase(); //word id is case sensitive and lowercase is required
        return "https://od-api.oxforddictionaries.com:443/api/v1/entries/" + language + "/" + word_id;
    }

    //in android calling network requests on the main thread forbidden by default
    //create class to do async job
    private class CallbackTask extends AsyncTask<String, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(DefinitionActivity.this);
            pDialog.setMessage("Please wait");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {

            //TODO: replace with your own app id and app key
            final String app_id = "96dbeab9";
            final String app_key = "08f327a5eddbe15635328018c2b503d0";
            try {
                URL url = new URL(params[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("app_id", app_id);
                urlConnection.setRequestProperty("app_key", app_key);

                // read the output from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                int status = urlConnection.getResponseCode();

                if (status != HttpURLConnection.HTTP_OK) {
                    Log.w("Connection", "Error ");
                    Toast.makeText(DefinitionActivity.this, "Error, check the word entered", Toast.LENGTH_LONG).show();
                    return null;
                } else {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(stringBuilder.toString());
                        JSONArray resultArray = jsonObject.getJSONArray("results");
                        JSONObject resultObject = resultArray.getJSONObject(0);
                        JSONArray lexicalArray = resultObject.getJSONArray("lexicalEntries");
                        for (int i = 0; i < lexicalArray.length(); i++) {
                            JSONObject lexicalObject = lexicalArray.getJSONObject(i);

                            //Get the category
                            String lexicalCategory = lexicalObject.getString("lexicalCategory");

                            //Get the definition
                            JSONArray entriesArray = lexicalObject.getJSONArray("entries");
                            JSONObject entryObject = entriesArray.getJSONObject(0);
                            JSONArray sensesArray = entryObject.getJSONArray("senses");
                            JSONObject sensesObject = sensesArray.getJSONObject(0);
                            JSONArray definitionArray = sensesObject.getJSONArray("definitions");
                            String definition = definitionArray.getString(0);

                            // tmp hash map for single contact
                            HashMap<String, String> definitionMap = new HashMap<>();

                            // adding each child node to HashMap key => value
                            definitionMap.put("lexicalCategory", lexicalCategory);
                            definitionMap.put("definitions", definition);

                            // adding contact to contact list
                            definitionList.add(definitionMap);
//                    tv.setText(definition);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
//                return e.toString();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    DefinitionActivity.this, definitionList,
                    R.layout.list_item, new String[]{"lexicalCategory", "definitions"}, new int[]{R.id.lexical_category, R.id.definitions});

            lv.setAdapter(adapter);

        }

    }
}
