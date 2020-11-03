import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import network.ClearVehicleResponse;
import network.WebService;

class WebServer {
    HMKitFleet hmkit = HMKitFleet.getInstance("apiKey");

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new WebServer().start();
    }

    void start() throws ExecutionException, InterruptedException {
        Executors.newCachedThreadPool().submit(() -> {
            System.out.println("Start " + System.currentTimeMillis());
            CompletableFuture requestClearance = hmkit.requestClearance("vin1");

            ClearVehicleResponse response = (ClearVehicleResponse) requestClearance.get();

            System.out.println("end " + System.currentTimeMillis());
            return null;
        });
    }
}
