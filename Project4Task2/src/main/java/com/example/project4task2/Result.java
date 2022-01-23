//        Name: Sanjana Rinke
//        Andrew ID: srinke
//        Email: srinke@andrew.cmu.edu
//        Project 4-Task 2
//
// This class has getters and setters to stores request and response info about 3rd party api call

package com.example.project4task2;

public class Result {
    private int responseCode;
    private String responseText;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int code) {
        responseCode = code;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String msg) {
        responseText = msg;
    }

    public String toString() {
        return responseCode + ":" + responseText;
    }
}
