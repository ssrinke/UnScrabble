//        Name: Sanjana Rinke
//        Andrew ID: srinke
//        Email: srinke@andrew.cmu.edu
//        Project 4-Task 2
//
// This servlet accepts 7 URL patterns and calls functions from model dictionary based on URL

package com.example.project4task2;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

//7 URLs mapped
@WebServlet(name = "dictionaryServlet", urlPatterns = {"/getMeanings", "/getOrigin", "/getSynonym", "/getExamples", "/getAntonym", "/Error", "/getAnalytics"})
public class DictionaryServlet extends HttpServlet {
    Dictionary dictionary;

    //    initialize model
    public void init() {
        dictionary = new Dictionary();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        connect to mongoDB
        dictionary.connectToMongoDB();
//        stores web server request timestamp
        dictionary.reqTs = System.currentTimeMillis();
//        gets the request URL
        String requestURL = request.getServletPath();
//        gets the request parameter
        String requestWord = request.getParameter("word");
//       https://www.geeksforgeeks.org/check-if-a-string-contains-only-alphabets-in-java-using-regex/
//        if the request is valid process it
        if ((requestWord != null && requestWord.matches("^[a-zA-Z]*$")) || requestURL.equalsIgnoreCase("/getAnalytics"))
            dictionary.processRequest(requestWord, requestURL, request, response);
        else
//          if the request is invalid, report error
            dictionary.giveError(response);
//        stores response timestamp
        dictionary.resTs = System.currentTimeMillis();
//        logs information to mongDB
        dictionary.logRequestInfo(request, dictionary.reqTs, response, dictionary.resTs);
//        close mongoDB connection
        dictionary.closeMongoConnection();
    }
}