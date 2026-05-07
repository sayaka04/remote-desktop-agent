package remoteagent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApiController {

    public String getNewData(){
        String response = ApiClient.get("/commands/1");
        return response;
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
                    "/commands/1/response", data
            );

            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
