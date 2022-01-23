<%--
        Name: Sanjana Rinke
        Andrew ID: srinke
        Email: srinke@andrew.cmu.edu
        Project 4-Task 2

 This JSP shows all logs in tabular format and all analytics

--%>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard</title>
</head>
<body>
<h1 align="center"><%= "Dictionary App Usage Statistics" %>
</h1>
<br/>
<%--Display top 7 analytics--%>
<h3>Most searched word:: <%=request.getAttribute("popWord")%>
</>
<h3>Most Requested Functionality:: Find <%=request.getAttribute("popRequest")%>
</>
<h3>Device sending most requests::  <%=request.getAttribute("popularDevice")%>
</>
<h3>3rd Party API Avg response time::  <%=request.getAttribute("ExtApiResTime")%> ms
</>
<h3>Web server Avg response time::  <%=request.getAttribute("WebAvgResTime")%> ms
</>
<h3>Number of Error Requests on Web server::  <%=request.getAttribute("ErrReqWebServer")%>
</>
<h3>Number of Correct Requests on 3rd Party API::  <%=request.getAttribute("ReqExtApi")%>
</>
<%--Display all lgs in tabular format--%>
<h2 align="center">All logs</h2>
<%--https://stackoverflow.com/questions/19766963/passing-arraylist-from-servlet-to-jsp--%>
<table border="1">
    <tr>
        <td>Functionality</td>
        <td>Word</td>
        <td>Web Service Latency(ms)</td>
        <td>Web Service Response Code</td>
        <td>3rd Party API Response code</td>
        <td>3rd Party API Latency(ms)</td>
        <td>Device</td>
    </tr>
    <%
        // retrieve your list from the request, with casting
        ArrayList<JSONObject> list = (ArrayList<JSONObject>) request.getAttribute("allLogs");
        // print the information about every log of the list
        for (JSONObject logs : list) {%>
    <tr>
        <td><%=logs.get("functionality")%>
        </td>
        <td><%
            if (logs.get("word").toString().equalsIgnoreCase("null")) {%>
            NA
            <%
            } else%><%=logs.get("word")%>
        </td>
        <td><%
            if (Long.parseLong(logs.get("WebServerlatency").toString()) == Long.MIN_VALUE) {%>
            NA
            <%
            } else%><%=logs.get("WebServerlatency")%>
        </td>
        <td><%=logs.get("responseStatus")%>
        </td>
        <td><%=logs.get("extApiResCode")%>
        </td>
        <td><%
            if (Long.parseLong(logs.get("extAPILatency").toString()) == Long.MIN_VALUE) {%>
            NA
            <%
            } else%><%=logs.get("extAPILatency")%>
        </td>
        <td><%=logs.get("deviceType")%>
        </td>
    </tr>
    <%}%>
</table>
</body>
</html>