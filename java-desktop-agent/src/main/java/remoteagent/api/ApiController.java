package remoteagent.api;

import com.google.gson.JsonObject;
import remoteagent.utils.Config;
import remoteagent.utils.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApiController {

    public JsonObject requestData() {
        try {
            String response = ApiClient.get("/commands/" + Config.get("api.device_id"));
            JsonObject json = Json.parseObject(response);

            // --- TODO: Add debug mode
            System.out.println(response);
            System.out.println(Json.toJson(json));

            return Json.getObject(json, "data");

        } catch (Exception e) {

            // --- TODO: Add debug mode
            System.out.println("Failed to request data: " + e.getMessage());

            return null;
        }
    }

    public String respond() {
        try{
            MultipartBodyBuilder data = new MultipartBodyBuilder()
                    .addField("host_payload[success]", "1")
                    .addField("host_payload[message]", "Task Finished")
                    .addFile("screenshot", "screen.png",
                            Files.readAllBytes(Path.of("single-screen.png")),
                            "image/png");

            String response = ApiClient.post(
                    "/commands/" + Config.get("api.device_id") + "/response", data
            );

            return response;
        } catch (IOException e) {
            System.out.println("Failed to respond data: " + e.getMessage());

            return null;        }
    }



}
