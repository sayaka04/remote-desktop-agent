package remoteagent.api;

import remoteagent.utils.Config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static String baseUrl() {
        return Config.get("api.base_url");
    }

    private static String token() {
        return Config.get("api.token");
    }

    // Generic POST JSON
    public static String post(String endpoint, String jsonBody){
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + endpoint))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Generic POST Multipart/Form-data
    public static String post(String endpoint, MultipartBodyBuilder builder) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + endpoint))
                    .header("Content-Type", "multipart/form-data; boundary=" + builder.getBoundary())
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(builder.build()))
                    .build();

            HttpResponse<String> response =
                    CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Generic GET
    public static String get(String endpoint){
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + endpoint))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token())
                    .GET()
                    .build();

            HttpResponse<String> response =
                    CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}