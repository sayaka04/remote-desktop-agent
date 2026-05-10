package remoteagent.api;

import com.google.gson.JsonObject;
import remoteagent.controller.WindowController;
import remoteagent.utils.Config;
import remoteagent.utils.Json;
import remoteagent.utils.LogUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApiController {

    WindowController windowController;
    public void setWindowController(WindowController windowController){
        this.windowController = windowController;
    }

    public JsonObject requestData() {
        try {
            String response = ApiClient.get("/commands/" + Config.get("api.device_id"));
            JsonObject json = Json.parseObject(response);
            windowController.writeToLog(LogUtil.LogType.INFO, "Request Data: " + response);

            // --- TODO: Add debug mode
//            System.out.println(response);
//            System.out.println(Json.toJson(json));

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

            windowController.writeToLog(LogUtil.LogType.INFO, "Response: " + response);


            return response;
        } catch (IOException e) {
            System.out.println("Failed to respond data: " + e.getMessage());

            return null;        }
    }




}
