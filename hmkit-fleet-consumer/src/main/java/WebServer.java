import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import network.ClearVehicleResponse;

class WebServer {
    ServiceAccountApiConfiguration configuration;
    HMKitFleet hmkit = HMKitFleet.INSTANCE;

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
        hmkit.setConfiguration(configuration);
        System.out.println("Start " + getDate());
        CompletableFuture<ClearVehicleResponse> requestClearance = hmkit.requestClearance("vin1");

        requestClearance.thenAcceptAsync(response -> {
            System.out.println("end " + getDate() + ", result: " + response.getStatus());
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });

        Executors.newCachedThreadPool().submit(() -> requestClearance.get());
    }

    String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        return LocalTime.now().format(formatter);
    }
}
