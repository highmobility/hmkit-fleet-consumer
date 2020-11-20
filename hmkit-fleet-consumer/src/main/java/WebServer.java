import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import model.AuthToken;
import network.Response;
import network.response.ClearanceStatus;

class WebServer {
    ServiceAccountApiConfiguration configuration;
    HMKitFleet hmkitFleet = HMKitFleet.INSTANCE;

    public static void main(String[] args) throws IOException {
        new WebServer().start();
    }

    WebServer() throws IOException {
        String path = WebServer.class.getClassLoader().getResource("credentials.yaml").getFile();
        File credentialsFile = new File(path);
        ObjectMapper om = new ObjectMapper(new YAMLFactory()).registerModule(new KotlinModule());
        /*
        use credentials.yaml to decode a ServiceAccountApiConfiguration object
        required keys:
        privateKey: ""
        apiKey: ""
         */
        configuration = om.readValue(credentialsFile, ServiceAccountApiConfiguration.class);
    }

    void start() {
        System.out.println("Start " + getDate());
        hmkitFleet.setEnvironment(HMKitFleet.Environment.DEV);

        CompletableFuture<Response<AuthToken>> authTokenRequest =
                hmkitFleet.getAuthToken(configuration);

        CompletableFuture<Response<ClearanceStatus>> requestClearance =
                authTokenRequest.thenCompose(token ->
                        hmkitFleet.requestClearance(token.getResponse(), "WBADT43452G296404")
                );

        requestClearance.thenApply(status -> {
            System.out.println(String.format("clear vehicle status response: %s", status.getResponse()));
            System.out.println(String.format("clear vehicle status error: %s", status.getError().getTitle()));
            System.out.println("End: " + getDate());
            return null;
        });

        Executors.newCachedThreadPool().submit(() -> requestClearance.get());

        // TODO: request a vehicle clearance
        //        CompletableFuture<ClearVehicle> requestClearance = hmkit.requestClearance("vin1");

    }

    String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        return LocalTime.now().format(formatter);
    }
}
