import com.highmobility.autoapi.Command;
import com.highmobility.autoapi.CommandResolver;
import com.highmobility.autoapi.Diagnostics;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.value.Bytes;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import model.AccessToken;
import model.Brand;
import model.ControlMeasure;
import model.Odometer;
import model.VehicleAccess;
import network.Response;
import model.ClearanceStatus;

import static java.lang.String.format;

class WebServer {
    final String testVin = "C0NNECT0000000005";

    ServiceAccountApiConfigurationStore configurationStore = new ServiceAccountApiConfigurationStore();
    VehicleAccessStore vehicleAccessStore = new VehicleAccessStore();
    HMKitFleet hmkitFleet = HMKitFleet.INSTANCE;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        new WebServer().start();
    }

    void start() throws ExecutionException, InterruptedException, IOException {
        System.out.println("Start " + getDate());
        hmkitFleet.setEnvironment(HMKitFleet.Environment.DEV);
        hmkitFleet.setConfiguration(configurationStore.read());

//        getClearanceStatuses();
        requestClearances();

//        VehicleAccess vehicleAccess = getVehicleAccess();
//        getVehicleDiagnostics(vehicleAccess);
    }

    private VehicleAccess getVehicleAccess() throws ExecutionException, InterruptedException {
        VehicleAccess storedVehicleAccess = vehicleAccessStore.read();
        if (storedVehicleAccess != null) return storedVehicleAccess;

        Response<VehicleAccess> accessResponse = hmkitFleet.getVehicleAccess(testVin, Brand.DAIMLER_FLEET).get();
        if (accessResponse.getError() != null)
            throw new RuntimeException(accessResponse.getError().getDetail());

        VehicleAccess serverVehicleAccess = accessResponse.getResponse();
        vehicleAccessStore.store(serverVehicleAccess);
        return serverVehicleAccess;

//        return getTestVehicleAccess();
    }

    private void getVehicleDiagnostics(VehicleAccess vehicleAccess) throws ExecutionException, InterruptedException {
        Response<Bytes> diagnosticsResponse = hmkitFleet.sendCommand(
                new Diagnostics.GetState(Diagnostics.PROPERTY_SPEED),
                vehicleAccess
        ).get();

        if (diagnosticsResponse.getError() != null)
            throw new RuntimeException(diagnosticsResponse.getError().getTitle());

        Command commandFromVehicle = CommandResolver.resolve(diagnosticsResponse.getResponse());

        if (commandFromVehicle instanceof Diagnostics.State) {
            Diagnostics.State diagnostics = (Diagnostics.State) commandFromVehicle;
            System.out.println(format(
                    "Got diagnostics response: %s", diagnostics.getOdometer().getValue().getValue()));
        }
    }

    }

    private void requestClearances() {
        ControlMeasure measure = new Odometer(110000, Odometer.Length.KILOMETERS);

        CompletableFuture<Response<ClearanceStatus>> requestClearance =
                hmkitFleet.requestClearance(
                        testVin,
                        Brand.DAIMLER_FLEET,
                        List.of(measure)
                );

        requestClearance.thenApply(status -> {
            System.out.println(format("requestClearances response: %s", status.getResponse()));
            System.out.println(format("requestClearances error: %s", status.getError().getTitle()));
            System.out.println("End: " + getDate());
            return null;
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return ex;
        });

        Executors.newCachedThreadPool().submit(() -> requestClearance.get());
    }

    private void getClearanceStatuses() {
        CompletableFuture<Response<List<ClearanceStatus>>> getClearance =
                hmkitFleet.getClearanceStatuses();

        getClearance.thenApply(statuses -> {
            if (statuses.getResponse() != null) {
                if (statuses.getResponse().size() > 0) {
                    System.out.println(format("getClearanceStatuses response"));
                    for (ClearanceStatus status : statuses.getResponse()) {
                        System.out.println(format(format("status: %s:%s",
                                status.getVin(),
                                status.getStatus())));
                    }
                }
            } else {
                System.out.println(format("clear vehicle status error: %s", statuses.getError().getTitle()));
            }

            System.out.println("End: " + getDate());
            return null;
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return ex;
        });

        Executors.newCachedThreadPool().submit(() -> getClearance.get());
    }

    String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        return LocalTime.now().format(formatter);
    }
}