import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiHandler implements HttpHandler {


    @Override
    public void handle(HttpExchange exchange) throws IOException {


        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Accept");
            exchange.sendResponseHeaders(204, -1);
            return;
        } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            if (exchange.getRequestHeaders().get("Accept").contains("application/xml")) {
                headerApplicationXmlHandler(exchange);
            } else if (exchange.getRequestHeaders().get("Accept").contains("application/json")) {
                try {
                    headerApplicationJsonHandler(exchange);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            exchange.close();
        } else {
            exchange.sendResponseHeaders(405, 0);
            exchange.close();
        }
    }

    private void headerApplicationJsonHandler(HttpExchange exchange) throws IOException, InterruptedException {

        String path = exchange.getRequestURI().getQuery();

        if(path.startsWith("username="))
        {
            String username = path.replace("username=","");
            HttpResponse<String> githubResponse = githubReposRequestByUsername(username);

            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "application/json");
            responseHeaders.set("Access-Control-Allow-Origin", "*");

            if (githubResponse.statusCode() == 200) {
                byte[] responseBytes = apiResponse200(githubResponse.body(), username).toString().getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(responseBytes);
                responseBody.close();
            } else if (githubResponse.statusCode() == 404) {
                byte[] responseBytes = apiResponse404(githubResponse.body()).toString().getBytes();
                exchange.sendResponseHeaders(404, responseBytes.length);
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(responseBytes);
                responseBody.close();
            } else {
                exchange.sendResponseHeaders(501, 0);
                exchange.getResponseBody().close();
            }
        }
        else
        {
            exchange.sendResponseHeaders(501, 0);
            exchange.getResponseBody().close();
        }


    }

    private JSONObject apiResponse404(String body) {

        JSONObject jsonObject = new JSONObject();
        JSONObject githubResponse = new JSONObject(body);
        jsonObject.put("status", 404);
        jsonObject.put("message", githubResponse.getString("message"));
        return jsonObject;
    }

    private JSONArray apiResponse200(String body, String username) throws IOException, InterruptedException {

        JSONArray jsonArray = new JSONArray(body);

        JSONArray res = new JSONArray();

        for (int i = 0; i < jsonArray.length(); i++) {

            if (!jsonArray.getJSONObject(i).getBoolean("fork")) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", jsonArray.getJSONObject(i).getString("name"));
                jsonObject.put("ownerLogin", jsonArray.getJSONObject(i).getJSONObject("owner").getString("login"));
                jsonObject.put("branchesInfo", getBranchInfo(jsonArray.getJSONObject(i).getString("name"), username));
                res.put(jsonObject);
            }

        }

        return res;
    }

    private HttpResponse<String> githubReposRequestByUsername(String username) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder().build();

        HttpRequest httpRequest = HttpRequest.newBuilder(
                        URI.create("https://api.github.com/users/" + username + "/repos"))
                .header("accept", "application/json")
                .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

    }

    private JSONArray getBranchInfo(String repoName, String username) throws IOException, InterruptedException {

        HttpClient httpClient = HttpClient.newBuilder().build();

        HttpRequest httpRequest = HttpRequest.newBuilder(
                        URI.create("https://api.github.com/repos/" + username + "/" + repoName + "/branches"))
                .header("accept", "application/json")
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        JSONArray jsonArray = new JSONArray(httpResponse.body());

        JSONArray resultArray = new JSONArray();

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", jsonArray.getJSONObject(i).getString("name"));
            jsonObject.put("last_commit_sha", jsonArray.getJSONObject(i).getJSONObject("commit").getString("sha"));
            resultArray.put(jsonObject);
        }

        return resultArray;

    }

    private void headerApplicationXmlHandler(HttpExchange exchange) throws IOException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("status", 406);
        jsonObject.put("message", "Wrong header application/xml");

        byte[] responseBytes = jsonObject.toString().getBytes();
        exchange.sendResponseHeaders(406, responseBytes.length);
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(responseBytes);
        responseBody.close();
    }
}
