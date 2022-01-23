//        Name: Sanjana Rinke
//        Andrew ID: srinke
//        Email: srinke@andrew.cmu.edu
//        Project 4-Task 1
//
// This Activity loads the UI components and enables user to send request to service deployed on heroku


package edu.cmu.project4task1;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

public class DictionaryActivity extends AppCompatActivity {

    DictionaryActivity dictionaryActivity = this;
    String searchTerm;

    TextView heading=null;
    ListView listView=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        initialize ui components
        Button submitButton = findViewById(R.id.submit);
        Spinner dropdown = findViewById(R.id.functionDropdown);
         heading = findViewById(R.id.resultHeader);
         listView = findViewById(R.id.resultListView);
//        initialize dropdown values
        String[] functions = new String[]{"Meanings", "Origin", "Synonym", "Antonym", "Examples"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, functions);
//        set the dropdown with required functionalities
        dropdown.setAdapter(adapter);

//        bind listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                get the search term
                searchTerm = ((EditText) findViewById(R.id.searchTerm)).getText().toString();
//                check if search term is valid or not
//                https://www.geeksforgeeks.org/check-if-a-string-contains-only-alphabets-in-java-using-regex/
                if (searchTerm.isEmpty() || !(searchTerm.matches("^[a-zA-Z]*$"))) {
//                https://stackoverflow.com/questions/31175601/how-can-i-change-default-toast-message-color-and-background-color-in-android
//                  display error toast i invalid input
                    heading.setText("");
                    listView.setAdapter(null);
                    Toast toast = Toast.makeText(dictionaryActivity, "Invalid input", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.getView().setBackgroundColor(Color.RED);
                    toast.show();
                } else {
//                    if search term correct, give call to request deployed on heroku
                    SearchWord searchWord = new SearchWord();
                    String functionality = dropdown.getSelectedItem().toString();
                    searchWord.search(searchTerm, functionality, dictionaryActivity);
                }
            }
        });
    }

    //    executes after the response is received from web service
    public void responseReady(String[] data, String functionality) {
//        initialize UI components
//        populate views of data if present
        JSONArray a1=new JSONArray();
        if (data != null) {
            heading.setText(functionality + " for " + searchTerm);
            ArrayAdapter adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, data);
            listView.setAdapter(adapter);
        }
//        display error toast if data is not present
        else {
            Toast toast = Toast.makeText(dictionaryActivity, "No Response from Web Service!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.getView().setBackgroundColor(Color.RED);
            toast.show();
            heading.setText("");
            listView.setAdapter(null);
        }
    }
}