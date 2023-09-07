import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class testing {


    @BeforeClass
     public static void beforeClass() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        server.createContext("/api", new ApiHandler());
        server.start();
    }

    @Test
    public void applicationXmlTest() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder().build();

        HttpRequest httpRequest = HttpRequest.newBuilder(
                        URI.create("http://localhost:8080/api"))
                .header("accept","application/xml")
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,HttpResponse.BodyHandlers.ofString());

        JSONObject response = new JSONObject(httpResponse.body());
        System.out.println(response);
        assert httpResponse.statusCode() == 406;
        assert response.get("status").equals(406);
        assert response.get("message").equals("Wrong header application/xml");
    }

    @Test
    public void notExistingUserTest() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder().build();

        HttpRequest httpRequest = HttpRequest.newBuilder(
                        URI.create("http://localhost:8080/api?username=NOTEXISTINGUSER////////////////"))
                .header("accept","application/json")
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,HttpResponse.BodyHandlers.ofString());

        JSONObject response = new JSONObject(httpResponse.body());
        System.out.println(response);
        assert httpResponse.statusCode() == 404;
        assert response.get("status").equals(404);
        assert response.get("message").equals("Not Found");
    }

    @Test
    public void existingUserTest() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder().build();

        HttpRequest httpRequest = HttpRequest.newBuilder(
                        URI.create("http://localhost:8080/api?username=mandrejczuk"))
                .header("accept","application/json")
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        System.out.println(httpResponse.body());
        JSONArray response = new JSONArray(httpResponse.body());
       assert httpResponse.statusCode() == 200;
       assert response.getJSONObject(0).getString("ownerLogin").equals("mandrejczuk");

    }

    @Test
    public void forkTest() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder().build();

        HttpRequest httpGithubRequest = HttpRequest.newBuilder(
                        URI.create("https://api.github.com/users/MarSik/repos"))
                .header("accept","application/json")
                .build();

        HttpResponse<String> httpGithubResponse = httpClient.send(httpGithubRequest,HttpResponse.BodyHandlers.ofString());

        JSONArray githubResponse = new JSONArray(httpGithubResponse.body());

        int forkGithubFalseCounter = 0;

        for (int i = 0; i < githubResponse.length(); i++) {

            if(githubResponse.getJSONObject(i).getBoolean("fork"))
            {
            }
            else
            {
                forkGithubFalseCounter++;
            }
        }

        HttpRequest httpApiRequest = HttpRequest.newBuilder(
                        URI.create("http://localhost:8080/api?username=MarSik"))
                .header("accept","application/json")
                .build();

        HttpResponse<String> httpApiResponse = httpClient.send(httpApiRequest,HttpResponse.BodyHandlers.ofString());

        JSONArray apiResponse = new JSONArray(httpApiResponse);


        assert forkGithubFalseCounter == apiResponse.length();

    }
}
