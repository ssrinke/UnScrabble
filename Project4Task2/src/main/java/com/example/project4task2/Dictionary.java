//        Name: Sanjana Rinke
//        Andrew ID: srinke
//        Email: srinke@andrew.cmu.edu
//        Project 4-Task 2
//
// This model has functions to fetch data requested by user from a 3rd party dictionary API and draws some analytics on data from MongoDB
// check max logic once

package com.example.project4task2;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Dictionary {

    public MongoClient mongoClient = null;
    //    stores 3rd party API response code
    public int extApiResCode = 0;
    //    stores 3rd party API response request timestamp
    public Long extApiReqTs = Long.MIN_VALUE;
    //    stores 3rd party API response timestamp
    public Long extApiResTs = Long.MIN_VALUE;
    //    stores web server request timestamp
    public Long reqTs = Long.MIN_VALUE;
    //    stores web server response timestamp
    public Long resTs = Long.MIN_VALUE;

    //    This function process all requests received on the Web server and calls relevant functions depending on request URL
    public void processRequest(String requestWord, String requestURL, HttpServletRequest request, HttpServletResponse response) {
        switch (reqURLToFunctionality(requestURL)) {
            case "Meanings":
                getMeanings(requestWord, response);
                break;
            case "Origin":
                getOrigin(requestWord, response);
                break;
            case "Synonyms":
                findSynonym(requestWord, response);
                break;
            case "Examples":
                findSentenceUsage(requestWord, response);
                break;
            case "Antonyms":
                findAntonym(requestWord, response);
                break;
            case "Analytics":
                getAnalytics(request, response);
                break;
            default:
                giveError(response);
                break;
        }
    }

    //    function executed when URL is mal-formed or no data in request parameters
    public void giveError(HttpServletResponse response) {
        writeResponseData(null, response);
    }

    //    Gives call to 3rd party API to find sentence usage of a word
    public void findSentenceUsage(String word, HttpServletResponse response) {
//        gets data from 3rd party API
        Result r = getData(word);
//        executes only if the response is valid
        if (r.getResponseCode() == 200) {
            JSONArray jsonArray = new JSONArray(r.getResponseText());
            if (checkKeyPresent(jsonArray.getJSONObject(0), "meanings")) {
                JSONArray meanings = new JSONArray(jsonArray.getJSONObject(0).get("meanings").toString());
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < meanings.length(); i++) {
                    if (checkKeyPresent(meanings.getJSONObject(i), "definitions")) {
                        JSONArray definition = new JSONArray(meanings.getJSONObject(i).get("definitions").toString());
                        for (int j = 0; j < definition.length(); j++) {
                            if (checkKeyPresent(definition.getJSONObject(j), "example"))
//                                extracts all the available examples for a given word and separates it using '$' to create a response for mobile app
                                s.append(definition.getJSONObject(j).get("example") + "$");
                        }
                    }
                }
                JSONArray jsonResponse = new JSONArray(s.toString().split("\\$"));
                //function to write response for mobile app
                writeResponseData(jsonResponse.toString(), response);
            }
        } else
            //executes if 3rd party API response is not valid
            writeResponseData(null, response);
    }

    //    Gives the Origin of the word requested by user
    public void getOrigin(String word, HttpServletResponse response) {
        //        gets data from 3rd party API
        Result r = getData(word);
        //        executes only if the response is valid
        if (r.getResponseCode() == 200) {
            JSONArray jsonArray = new JSONArray(r.getResponseText());
            if (checkKeyPresent(jsonArray.getJSONObject(0), "origin")) {
                //extracts origin for a given word to create a response for mobile app
                writeResponseData(new JSONArray().put(jsonArray.getJSONObject(0).get("origin").toString()).toString(), response);
            }
        } else
            //        Executes if the 3rd PArty API response is invalid
            writeResponseData(null, response);
    }

    //    Gives call to 3rd party API to find meaning of a word
    public void getMeanings(String word, HttpServletResponse response) {
        //        gets data from 3rd party API
        Result r = getData(word);
        if (r.getResponseCode() == 200) {
            JSONArray jsonArray = new JSONArray(r.getResponseText());
            if (checkKeyPresent(jsonArray.getJSONObject(0), "meanings")) {
                JSONArray meanings = new JSONArray(jsonArray.getJSONObject(0).get("meanings").toString());
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < meanings.length(); i++) {
                    if (checkKeyPresent(meanings.getJSONObject(i), "definitions")) {
                        JSONArray definition = new JSONArray(meanings.getJSONObject(i).get("definitions").toString());
                        for (int j = 0; j < definition.length(); j++) {
//                            extracts all the available examples for a given word and separates it using '$' to create a response for mobile app
                            s.append(definition.getJSONObject(j).get("definition") + "$");
                        }
                    }
                }
                JSONArray jsonResponse = new JSONArray(s.toString().split("\\$"));
                //function to write response for mobile app
                writeResponseData(jsonResponse.toString(), response);
            }
        }
//        Executes if the 3rd PArty API response is invalid
        else
            writeResponseData(null, response);

    }

    //writes the response and status code to be sent to mobile app
    public void writeResponseData(String data, HttpServletResponse response) {
        try {
            PrintWriter out = response.getWriter();
//            when the request is valid and correct response is generated
            if (data != null) {
                response.setContentType("text/plain; charset=utf-8");
                response.setStatus(200);
                out.write(data);
            } else {
//                when request is invalid
                extApiResCode = 0;
                response.setContentType("text/plain; charset=utf-8");
                response.setStatus(400);
                out.write("");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    gives a call to 3rd party API to fetch all information about a requested word
//    Sample JSON Response from 3rd Party API for word=Hello-> [{"word":"hello","phonetic":"həˈləʊ","phonetics":[{"text":"həˈləʊ","audio":"//ssl.gstatic.com/dictionary/static/sounds/20200429/hello--_gb_1.mp3"},{"text":"hɛˈləʊ"}],"origin":"early 19th century: variant of earlier hollo ; related to holla.","meanings":[{"partOfSpeech":"exclamation","definitions":[{"definition":"used as a greeting or to begin a phone conversation.","example":"hello there, Katie!","synonyms":[],"antonyms":[]}]},{"partOfSpeech":"noun","definitions":[{"definition":"an utterance of ‘hello’; a greeting.","example":"she was getting polite nods and hellos from people","synonyms":[],"antonyms":[]}]},{"partOfSpeech":"verb","definitions":[{"definition":"say or shout ‘hello’.","example":"I pressed the phone button and helloed","synonyms":[],"antonyms":[]}]}]}]
    public Result getData(String word) {
        HttpURLConnection conn;
        int status = 0;
        Result result = new Result();
        try {
            extApiReqTs = System.currentTimeMillis();
            // GET wants us to pass the name on the URL line
            URL url = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + word);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // we are sending plain text
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            // tell the server what format we want back
            conn.setRequestProperty("Accept", "text/plain; charset=utf-8");
            // wait for response
            status = conn.getResponseCode();
            // set http response code
            result.setResponseCode(status);
            // set http response message - this is just a status message
            // and not the body returned by GET
//          set 3rd party API response timestamp
            extApiResTs = System.currentTimeMillis();
//            set 3rd party API response cde
            extApiResCode = result.getResponseCode();
            result.setResponseText(conn.getResponseMessage());
            String responseBody = getResponseBody(conn);
            result.setResponseText(responseBody);
            conn.disconnect();

        }
        // handle exceptions
        catch (MalformedURLException e) {
            System.out.println("URL Exception thrown" + e);
        } catch (IOException e) {
            System.out.println("IO Exception thrown" + e);
        } catch (Exception e) {
            System.out.println("Exception thrown" + e);
        }
        return result;
    }

    // find synonyms for given word
    public void findSynonym(String word, HttpServletResponse response) {
        //        gets data from 3rd party API
        Result r = getData(word);
        //        executes only if the response is valid
        if (r.getResponseCode() == 200) {
            JSONArray jsonArray = new JSONArray(r.getResponseText());
            if (checkKeyPresent(jsonArray.getJSONObject(0), "meanings")) {
                JSONArray meanings = new JSONArray(jsonArray.getJSONObject(0).get("meanings").toString());
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < meanings.length(); i++) {
                    if (checkKeyPresent(meanings.getJSONObject(i), "definitions")) {
                        JSONArray definition = new JSONArray(meanings.getJSONObject(i).get("definitions").toString());
                        for (int j = 0; j < definition.length(); j++) {
                            if (checkKeyPresent(definition.getJSONObject(j), "synonyms")) {
                                JSONArray syn = new JSONArray(definition.getJSONObject(j).get("synonyms").toString());
                                for (int k = 0; k < syn.length(); k++) {
                                    s.append(syn.get(k) + "$");
                                }
                            }
                        }
                    }
                }
                JSONArray jsonResponse = new JSONArray(s.toString().split("\\$"));
                //function to write response for mobile app
                writeResponseData(jsonResponse.toString(), response);
            }

        }//        Executes if the 3rd PArty API response is invalid
        else
            writeResponseData(null, response);
    }

    //    Gives call to 3rd party API to find antonyms of a word
    public void findAntonym(String word, HttpServletResponse response) {
        //        gets data from 3rd party API
        Result r = getData(word);
        //        executes only if the response is valid
        if (r.getResponseCode() == 200) {
            JSONArray jsonArray = new JSONArray(r.getResponseText());
            if (checkKeyPresent(jsonArray.getJSONObject(0), "meanings")) {
                JSONArray meanings = new JSONArray(jsonArray.getJSONObject(0).get("meanings").toString());
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < meanings.length(); i++) {
                    if (checkKeyPresent(meanings.getJSONObject(i), "definitions")) {
                        JSONArray definition = new JSONArray(meanings.getJSONObject(i).get("definitions").toString());
                        for (int j = 0; j < definition.length(); j++) {
                            if (checkKeyPresent(definition.getJSONObject(j), "antonyms")) {
                                JSONArray ant = new JSONArray(definition.getJSONObject(j).get("antonyms").toString());
                                for (int k = 0; k < ant.length(); k++) {
                                    s.append(ant.get(k) + "$");
                                }
                            }
                        }
                    }
                }
                JSONArray jsonResponse = new JSONArray(s.toString().split("\\$"));
                //function to write response for mobile app
                writeResponseData(jsonResponse.toString(), response);
            }
        }
        //        Executes if the 3rd PArty API response is invalid
        else
            writeResponseData(null, response);
    }

    //    check if a key is present in json object or not
    private boolean checkKeyPresent(JSONObject jsonObject, String key) {
        if (jsonObject.has(key))
            return true;
        return false;
    }

    //    create a response for a request
    public String getResponseBody(HttpURLConnection conn) {
        String responseText = "";
        try {
            String output = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            while ((output = br.readLine()) != null) {
                responseText += output;
            }
            conn.disconnect();
        } catch (IOException e) {
            System.out.println("Exception caught " + e);
        }
        return responseText;
    }

    //    map the URL to the functionality
    public String reqURLToFunctionality(String Url) {
        String functionality = "";
        switch (Url) {
            case "/getMeanings":
                functionality = "Meanings";
                break;
            case "/getOrigin":
                functionality = "Origin";
                break;
            case "/getSynonym":
                functionality = "Synonyms";
                break;
            case "/getExamples":
                functionality = "Examples";
                break;
            case "/getAntonym":
                functionality = "Antonyms";
                break;
            case "/getAnalytics":
                functionality = "Analytics";
                break;
            default:
                functionality = "none";
                break;
        }
        return functionality;
    }

    //    Gives 7 analytical parameters for data stores in mongoDB
    public void getAnalytics(HttpServletRequest request, HttpServletResponse response) {
//        get the mongoDB database
        MongoDatabase database = mongoClient.getDatabase("project4");
//        get the collection
        MongoCollection<Document> collection = database.getCollection("metrics");
//        find all documents of the collection
        FindIterable<Document> doc = collection.find();
//        call various functions to analyze 7 parameters
        request.setAttribute("popWord", findPopularWord(doc));
        request.setAttribute("popRequest", findPopularReq(doc));
        request.setAttribute("popularDevice", findPopularDevice(doc));
        request.setAttribute("WebAvgResTime", findWebServAvgResTime(doc));
        request.setAttribute("ExtApiResTime", findExtApiAvgResTime(doc));
        request.setAttribute("ErrReqWebServer", findErrReqWeb(doc));
        request.setAttribute("ReqExtApi", findReqExtApi(doc));
        ArrayList<JSONObject> logs = new ArrayList<>();
        MongoCursor<Document> cursor = doc.iterator();
        while (cursor.hasNext()) {
//           this arraylist stores all logs to be displayed in JSP
            logs.add(new JSONObject(cursor.next().toJson()));
        }
        request.setAttribute("allLogs", logs);
        try {
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    get the count of all correct requests send to 3rd party API
    private int findReqExtApi(FindIterable<Document> doc) {
        MongoCursor<Document> cursor = doc.iterator();
        int count = 0;
        try {
//            iterate over all mongoDB docs
            while (cursor.hasNext()) {
                JSONObject jsonObject = new JSONObject(cursor.next().toJson());
                if (checkKeyPresent(jsonObject, "extApiResCode")) {
//                    if the response status code of 3rd party API is 200, increment count
                    if (Integer.parseInt(jsonObject.get("extApiResCode").toString()) == 200) {
                        count++;
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return count;
    }

    //find all erroneous requests to web server
    private int findErrReqWeb(FindIterable<Document> doc) {
        MongoCursor<Document> cursor = doc.iterator();
        int count = 0;
        try {
//            iterate over all docs in mongoDB
            while (cursor.hasNext()) {
                JSONObject jsonObject = new JSONObject(cursor.next().toJson());
                if (checkKeyPresent(jsonObject, "responseStatus")) {
                    //if the response status code of 3rd party API is not 200, increment count
                    if (Integer.parseInt(jsonObject.get("responseStatus").toString()) != 200) {
                        count++;
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return count;
    }

    //    finds avg response time for web server
    private long findWebServAvgResTime(FindIterable<Document> doc) {
        int sum = 0;
        int count = 0;
        //https://examples.javacodegeeks.com/software-development/mongodb/java-mongodb-iterating-through-a-collection/
        MongoCursor<Document> cursor = doc.iterator();
        try {
//            iterate over mongoDB docs
            while (cursor.hasNext()) {
                JSONObject jsonObject = new JSONObject(cursor.next().toJson());
                if (checkKeyPresent(jsonObject, "WebServerlatency")) {
                    if (jsonObject.get("WebServerlatency").toString() != null) {
//                        for each record find the difference between request and response time
                        sum += Long.parseLong(jsonObject.get("WebServerlatency").toString());
                        count++;
                    }
                }
            }
        } finally {
            cursor.close();
        }
//        if count=0 then return 0, else returns the avg response time of web server
        return count == 0 ? 0 : (sum / count);
    }

    //    finds avg response time for 3rd party server
    private long findExtApiAvgResTime(FindIterable<Document> doc) {
        int sum = 0;
        int count = 0;
        //https://examples.javacodegeeks.com/software-development/mongodb/java-mongodb-iterating-through-a-collection/
        MongoCursor<Document> cursor = doc.iterator();
        try {
            while (cursor.hasNext()) {
                JSONObject jsonObject = new JSONObject(cursor.next().toJson());
                if (checkKeyPresent(jsonObject, "extAPILatency")) {
                    if (Long.parseLong(jsonObject.get("extAPILatency").toString()) != Long.MIN_VALUE) {
                        sum += Long.parseLong(jsonObject.get("extAPILatency").toString());
                        count++;
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return count == 0 ? 0 : (sum / count);
    }

    //    finds the most popular device sending the request
    public String findPopularDevice(FindIterable<Document> doc) {
        String popDevice = "NA";
//        hashmap maintains the devices as key and frequency of request from device as value
        HashMap<String, Integer> popularDevice = new HashMap<>();
        //https://examples.javacodegeeks.com/software-development/mongodb/java-mongodb-iterating-through-a-collection/
//        iterate over mongoDB
        MongoCursor<Document> cursor = doc.iterator();
        try {
            while (cursor.hasNext()) {
                JSONObject jsonObject = new JSONObject(cursor.next().toJson());
                if (jsonObject.has("deviceType")) {
                    String device = jsonObject.get("deviceType").toString();
                    if (popularDevice.containsKey(device)) {
                        popularDevice.put(device, popularDevice.get(device) + 1);
                    } else
                        popularDevice.put(device, 1);
                }
            }
//            gets the device with maximum frequency
            popDevice = getMaximum(popularDevice);
        } finally {
            cursor.close();
        }
        return popDevice;
    }

    //    finds the most asked functionality
    public String findPopularReq(FindIterable<Document> doc) {
        String popReq = "NA";
//      hashmap maintains the functionality as key and frequency of request as value
        HashMap<String, Integer> popularFunctionality = new HashMap<>();
        //https://examples.javacodegeeks.com/software-development/mongodb/java-mongodb-iterating-through-a-collection/
        MongoCursor<Document> cursor = doc.iterator();
        try {
            while (cursor.hasNext()) {
                JSONObject jsonObject = new JSONObject(cursor.next().toJson());
                //instead of check getAnalytics, check device
//                https://github.com/CMU-Heinz-95702/Lab2-InterestingPicture/blob/master/InterestingPictureServlet.java
                String ua = jsonObject.getString("deviceType");
//                this checks if the request came from mobile, which means count fo getAnalytics function will not be kept
                if (jsonObject.has("functionality") && ua != null && ((ua.indexOf("Android") != -1) || (ua.indexOf("iPhone") != -1))) {
                    String functionality = jsonObject.get("functionality").toString();
                    if (popularFunctionality.containsKey(functionality)) {
                        popularFunctionality.put(functionality, popularFunctionality.get(functionality) + 1);
                    } else
                        popularFunctionality.put(functionality, 1);
                }
            }
//            gets the maximum functionality
            popReq = getMaximum(popularFunctionality);
        } finally {
            cursor.close();
        }
        return popReq;
    }

    //    for a hashmap containing key and value, this function returns the key having maximum value
    public String getMaximum(HashMap<String, Integer> map) {
        String key = "NA";
        int max = Integer.MIN_VALUE;
        if (map != null) {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() > max && entry.getKey() != "null") {
                    max = entry.getValue();
                    key = entry.getKey();
                }
            }
        }

        return key;
    }

    //  finds most searched word by user
    public String findPopularWord(FindIterable<Document> doc) {
        String popWord = "NA";
//      hashmap maintains the functionality as key and frequency of request as value
        HashMap<String, Integer> popularWord = new HashMap<>();
        //https://examples.javacodegeeks.com/software-development/mongodb/java-mongodb-iterating-through-a-collection/
        MongoCursor<Document> cursor = doc.iterator();
        try {
            while (cursor.hasNext()) {
                JSONObject jsonObject = new JSONObject(cursor.next().toJson());
//                checks if json has keys
                if (jsonObject.has("word")) {
                    String word = jsonObject.get("word").toString();
                    if (popularWord.containsKey(word) && word != "null") {
                        popularWord.put(word, popularWord.get(word) + 1);
                    } else
                        popularWord.put(word, 1);
                }
            }
            popWord = getMaximum(popularWord);
        } finally {
            cursor.close();
        }
        return popWord;
    }

    //    close mongoDB connection
    public void closeMongoConnection() {
        mongoClient.close();
    }

    //    all information about request and reponse from the mobile app
    public void logRequestInfo(HttpServletRequest request, Long reqTs, HttpServletResponse response, Long resTs) {
//        https://github.com/CMU-Heinz-95702/Lab2-InterestingPicture/blob/master/InterestingPictureServlet.java
//       checks if the logging is only for mobile app
        String ua = request.getHeader("User-Agent");
        if (ua != null && ((ua.indexOf("Android") != -1) || (ua.indexOf("iPhone") != -1))) {
            MongoDatabase database = mongoClient.getDatabase("project4");
            Document document = new Document();
            document.append("functionality", reqURLToFunctionality(request.getServletPath()));
            document.append("deviceType", ua);
            document.append("word", request.getParameter("word"));
            document.append("WebServerlatency", resTs - reqTs);
            document.append("extAPILatency", extApiResTs - extApiReqTs);
            document.append("word", request.getParameter("word"));
            document.append("responseStatus", response.getStatus());
            document.append("extApiResCode", extApiResCode);
            database.getCollection("metrics").insertOne(document);
        }

    }

    //    connects to mongoDB using connection string
    public void connectToMongoDB() {
        try {
            ConnectionString connectionString = new ConnectionString("mongodb+srv://dbuser:1234@cluster0.7p5vm.mongodb.net/project4?retryWrites=true&w=majority");
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            mongoClient = MongoClients.create(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
