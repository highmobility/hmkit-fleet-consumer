import com.highmobility.autoapi.Command;
import com.highmobility.autoapi.CommandResolver;
import com.highmobility.autoapi.Diagnostics;
import com.highmobility.hmkitfleet.HMKitFleet;
import com.highmobility.hmkitfleet.ServiceAccountApiConfiguration;
import com.highmobility.value.Bytes;

import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.highmobility.hmkitfleet.model.Brand;
import com.highmobility.hmkitfleet.model.ClearanceStatus;
import com.highmobility.hmkitfleet.model.ControlMeasure;
import com.highmobility.hmkitfleet.model.Odometer;
import com.highmobility.hmkitfleet.model.VehicleAccess;
import com.highmobility.hmkitfleet.network.Response;

import static java.lang.String.format;

class DevCenterSnippetTests {
    // if this class fails to build, the relevant docs page needs to be updated

    // https://docs.high-mobility.com/guides/getting-started/fleet/
    void snippet() {
        ServiceAccountApiConfiguration configuration = new ServiceAccountApiConfiguration(
                "", // Add Service Account API Key here
                "", // Add Service Account Private Key here
                "[CLIENT CERTIFICATE]",
                "[CLIENT PRIVATE KEY]",
                "", // Add OAuth2 Client ID here
                ""  // Add OAuth2 Client Secret here
        );
        HMKitFleet.INSTANCE.setConfiguration(configuration);
    }

    HMKitFleet hmkitFleet;
    Logger logger;
    String vin = "";

    void one() throws ExecutionException, InterruptedException {
        Response<ClearanceStatus> response =
                hmkitFleet.requestClearance(vin, Brand.MERCEDES_BENZ).get();

        if (response.getResponse() != null) {
            logger.info(format("requestClearances response: %s", response.getResponse()));
        } else {
            logger.info(format("requestClearances error: %s", response.getError().getTitle()));
        }
    }

    void one_a() throws ExecutionException, InterruptedException {
        ControlMeasure measure = new Odometer(110000, Odometer.Length.KILOMETERS);
        List<ControlMeasure> measures = List.of(measure);

        Response<ClearanceStatus> response =
                hmkitFleet.requestClearance(vin, Brand.MERCEDES_BENZ, measures).get();
    }

    void getClearanceStatuses() throws ExecutionException, InterruptedException {
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

    ServiceAccountApiConfigurationStore configurationStore = new ServiceAccountApiConfigurationStore();
    VehicleAccessStore vehicleAccessStore = new VehicleAccessStore();

    VehicleAccess three() throws ExecutionException, InterruptedException {
// use the stored VehicleAccess if it exists
        VehicleAccess storedVehicleAccess = vehicleAccessStore.read(vin);
        if (storedVehicleAccess != null) return storedVehicleAccess;

// download VehicleAccess if it does not exist
        Response<VehicleAccess> accessResponse = hmkitFleet.getVehicleAccess(vin).get();
        if (accessResponse.getError() != null)
            throw new RuntimeException(accessResponse.getError().getDetail());

// store the downloaded vehicle access
        VehicleAccess serverVehicleAccess = accessResponse.getResponse();
        vehicleAccessStore.store(serverVehicleAccess);

        return serverVehicleAccess;
    }

    VehicleAccess vehicleAccess;

    void sendTelematicsCommand() throws ExecutionException, InterruptedException {
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

    void revoke() throws ExecutionException, InterruptedException {
        Response<Boolean> response = hmkitFleet.revokeClearance(vehicleAccess).get();

        if (response.getError() != null) {
            logger.info(format("revokeClearance error: %s",
                    response.getError().getDetail()));
        } else {
            logger.info(format("revokeClearance success"));
        }

    }
}
