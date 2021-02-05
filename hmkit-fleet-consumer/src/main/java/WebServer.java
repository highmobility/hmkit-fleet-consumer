import com.highmobility.autoapi.Command;
import com.highmobility.autoapi.CommandResolver;
import com.highmobility.autoapi.Diagnostics;
import com.highmobility.value.Bytes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import model.Brand;
import model.ClearanceStatus;
import model.ControlMeasure;
import model.Odometer;
import model.VehicleAccess;
import network.Response;

import static java.lang.String.format;

class WebServer {
    final String testVin = "C0NNECT0000000006";
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    ServiceAccountApiConfigurationStore configurationStore = new ServiceAccountApiConfigurationStore();
    VehicleAccessStore vehicleAccessStore = new VehicleAccessStore();
    HMKitFleet hmkitFleet = HMKitFleet.INSTANCE;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        new WebServer().start();
    }

    void start() throws ExecutionException, InterruptedException, IOException {
        logger.info("Start " + getDate());
        hmkitFleet.setEnvironment(HMKitFleet.Environment.DEV);
        hmkitFleet.setConfiguration(configurationStore.read());

//        requestClearances();
//        getClearanceStatuses();

        VehicleAccess vehicleAccess = getVehicleAccess(testVin);
        getVehicleDiagnostics(vehicleAccess);

//        revokeClearance(testVin);

        logger.info("End: " + getDate());
    }

    private void revokeClearance(String vin) throws ExecutionException, InterruptedException {
        VehicleAccess vehicleAccess = vehicleAccessStore.read(vin);
        Response<Boolean> response = hmkitFleet.revokeClearance(vehicleAccess).get();

        if (response.getError() != null) {
            logger.info(format("revokeClearance error: %s", response.getError().getDetail()));
        } else {
            logger.info(format("revokeClearance success"));
        }
    }

    private void requestClearances() throws ExecutionException, InterruptedException {
        ControlMeasure measure = new Odometer(110000, Odometer.Length.KILOMETERS);

        Response<ClearanceStatus> response =
                hmkitFleet.requestClearance(
                        testVin,
                        Brand.DAIMLER_FLEET,
                        List.of(measure)
                ).get();

        if (response.getResponse() != null) {
            logger.info(format("requestClearances response: %s", response.getResponse()));
        } else {
            logger.info(format("requestClearances error: %s", response.getError().getTitle()));
        }
    }

    private void getClearanceStatuses() throws ExecutionException, InterruptedException {
        Response<List<ClearanceStatus>> response = hmkitFleet.getClearanceStatuses().get();

        if (response.getResponse() != null) {
            logger.info(format("getClearanceStatuses response"));
            for (ClearanceStatus status : response.getResponse()) {
                logger.info(format("status: %s:%s",
                        status.getVin(),
                        status.getStatus()));
            }
        } else {
            logger.info(format("getClearanceStatuses error: %s", response.getError().getTitle()));
        }
    }

    private VehicleAccess getVehicleAccess(String vin) throws ExecutionException, InterruptedException {
        VehicleAccess storedVehicleAccess = vehicleAccessStore.read(vin);
        if (storedVehicleAccess != null) return storedVehicleAccess;

        Response<VehicleAccess> accessResponse = hmkitFleet.getVehicleAccess(testVin, Brand.DAIMLER_FLEET).get();
        if (accessResponse.getError() != null)
            throw new RuntimeException(accessResponse.getError().getDetail());

        VehicleAccess serverVehicleAccess = accessResponse.getResponse();
        vehicleAccessStore.store(serverVehicleAccess);
        return serverVehicleAccess;
    }

    private void getVehicleDiagnostics(VehicleAccess vehicleAccess) throws ExecutionException, InterruptedException {
        Command getVehicleSpeed = new Diagnostics.GetState(Diagnostics.PROPERTY_SPEED);

        Response<Bytes> response = hmkitFleet.sendCommand(
                getVehicleSpeed,
                vehicleAccess
        ).get();

        if (response.getError() != null)
            throw new RuntimeException(response.getError().getTitle());

        Command commandFromVehicle = CommandResolver.resolve(response.getResponse());

        if (commandFromVehicle instanceof Diagnostics.State) {
            Diagnostics.State diagnostics = (Diagnostics.State) commandFromVehicle;
            logger.info(format(
                    "Got diagnostics response: %s",
                    diagnostics.getSpeed().getValue().getValue()));
        }
    }

    String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        return LocalTime.now().format(formatter);
    }
}