//        Name: Sanjana Rinke
//        Andrew ID: srinke
//        Email: srinke@andrew.cmu.edu
//        Project 4-Task 1
//
// This class sends request to service deployed on heroku

package edu.cmu.project4task1;

import android.os.AsyncTask;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SearchWord {
    //    create instance of main activity
    DictionaryActivity dictionaryActivity = null;
    //    base URL for task 1
//    String baseURL = "https://powerful-wave-47295.herokuapp.com/";
//    base URL for task 2
    String baseURL = "https://fast-mesa-20310.herokuapp.com/";

//    to pass the selected operation to UI
    public String operation = "";

    //    search the word entered by user
    public void search(String searchTerm, String functionality, DictionaryActivity firstFragment) {
        this.dictionaryActivity = firstFragment;
        new AsyncWordSearch().execute(searchTerm, functionality);
    }

    //    implemented search functionality in async thread
    private class AsyncWordSearch extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... data) {
            return search(data[0], data[1]);
        }

        //        executed after search is complete
        protected void onPostExecute(String data) {
//            check if data is received or not.
//            send the data to view to display on UI
            if (data != null) {
                String[] processedData = null;
                if (data.length() == 0)
                    processedData = new String[]{"No Data"};
                else {
                    try {
//                        converting into String array to display in list view
                        JSONArray responseJson = new JSONArray(data);
                        processedData = new String[responseJson.length()];
                        for (int i = 0; i < responseJson.length(); i++) {
                            if (responseJson.get(i).toString().isEmpty())
                                processedData[i] = "No Data";
                            else
                                processedData[i] = responseJson.get(i).toString();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                dictionaryActivity.responseReady(processedData, operation);
            } else
//                send null if no data is received to report error
                dictionaryActivity.responseReady(null, "Error");
        }

        //        calls service deployed on heroku to get data
        public String search(String searchTerm, String functionality) {
            String response = "";
            String dictionaryURL = "";
            operation = functionality;
//            create a URL depending on request functionality
            switch (functionality) {
                case "Examples":
                    dictionaryURL = baseURL + "getExamples?word=" + searchTerm;
                    break;
                case "Meanings":
                    dictionaryURL = baseURL + "getMeanings?word=" + searchTerm;
                    break;
                case "Origin":
                    dictionaryURL = baseURL + "getOrigin?word=" + searchTerm;
                    break;
                case "Synonym":
                    dictionaryURL = baseURL + "getSynonym?word=" + searchTerm;
                    break;
                case "Antonym":
                    dictionaryURL = baseURL + "getAntonym?word=" + searchTerm;
                    break;
            }
            // At this point, we have the URL for the search.  Get data from service.
            try {
                URL url = new URL(dictionaryURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String str;
                while ((str = in.readLine()) != null) {
                    // str is one line of text readLine() strips newline characters
                    response += str;
                }
//                the response send is of the format data_received%functionality so that we can display functionality on UI
                return response;
            } catch (Exception e) {
                return null;  // so compiler does not complain
            }
        }
    }

}
