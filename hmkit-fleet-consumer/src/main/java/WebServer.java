import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

//        requestClearances(authTokenRequest);
        getClearanceStatuses(authTokenRequest);
    }

    private void requestClearances(CompletableFuture<Response<AuthToken>> authTokenRequest) {
        CompletableFuture<Response<ClearanceStatus>> requestClearance =
                authTokenRequest.thenCompose(token ->
                        hmkitFleet.requestClearance(token.getResponse(), "C0NNECT0000000005")
                );

        requestClearance.thenApply(status -> {
            System.out.println(String.format("clear vehicle status response: %s", status.getResponse()));
            System.out.println(String.format("clear vehicle status error: %s", status.getError().getTitle()));
            System.out.println("End: " + getDate());
            return null;
        });

        Executors.newCachedThreadPool().submit(() -> requestClearance.get());
    }

    private void getClearanceStatuses(CompletableFuture<Response<AuthToken>> authTokenRequest) {
        CompletableFuture<Response<List<ClearanceStatus>>> getClearance =
                authTokenRequest.thenCompose(token ->
                        hmkitFleet.getClearanceStatuses(token.getResponse())
                );

        getClearance.thenApply(statuses -> {
            if (statuses.getResponse() != null) {
                if (statuses.getResponse().size() > 0) {
                    System.out.println(String.format("clear vehicle status response"));
                    for (ClearanceStatus status : statuses.getResponse()) {
                        System.out.println(String.format(String.format("status: %s:%s",
                                status.getVin(),
                                status.getStatus())));
                    }
                }
            } else {
                System.out.println(String.format("clear vehicle status error: %s", statuses.getError().getTitle()));
            }

            System.out.println("End: " + getDate());
            return null;
        });

        Executors.newCachedThreadPool().submit(() -> getClearance.get());
    }

    String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        return LocalTime.now().format(formatter);
    }
}
