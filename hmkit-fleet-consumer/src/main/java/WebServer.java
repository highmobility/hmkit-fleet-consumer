import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import network.ClearVehicleResponse;

class WebServer {
    HMKitFleet hmkit = HMKitFleet.getInstance("apiKey");
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new WebServer().start();
    }

    void start() throws ExecutionException, InterruptedException {
        Executors.newCachedThreadPool().submit(() -> {
            System.out.println("Start " + getDate());
            CompletableFuture requestClearance = hmkit.requestClearance("vin1");

            ClearVehicleResponse response = (ClearVehicleResponse) requestClearance.get();

            System.out.println("end " + getDate() + ", result: " + response.getStatus());
            return null;
        });
    }

    String getDate() {
        return formatter.format(Calendar.getInstance().getTime());
    }
}
