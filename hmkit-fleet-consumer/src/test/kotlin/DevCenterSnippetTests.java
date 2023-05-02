import com.highmobility.autoapi.Command;
import com.highmobility.autoapi.CommandResolver;
import com.highmobility.autoapi.Diagnostics;
import com.highmobility.hmkitfleet.HMKitFleet;
import com.highmobility.hmkitfleet.ServiceAccountApiConfiguration;
import com.highmobility.hmkitfleet.model.RequestClearanceResponse;
import com.highmobility.hmkitfleet.network.TelematicsCommandResponse;
import com.highmobility.hmkitfleet.network.TelematicsResponse;
import com.highmobility.value.Bytes;

import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.highmobility.hmkitfleet.model.Brand;
import com.highmobility.hmkitfleet.model.ClearanceStatus;
import com.highmobility.hmkitfleet.model.ControlMeasure;
import com.highmobility.hmkitfleet.model.Odometer;
import com.highmobility.hmkitfleet.model.VehicleAccess;
import com.highmobility.hmkitfleet.network.Response;

import static java.lang.String.format;

// test this link manually
// https://github.com/highmobility/hmkit-fleet-consumer/blob/main/hmkit-fleet-consumer/src/main/java/VehicleAccessStore.java#L27

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
        Response<RequestClearanceResponse> response =
          hmkitFleet.requestClearance(vin, Brand.MERCEDES_BENZ).get();

        if (response.getResponse() != null) {
            RequestClearanceResponse requestClearanceResponse = response.getResponse();

            if (requestClearanceResponse.getStatus() == ClearanceStatus.Status.ERROR) {
                logger.error(format("Request clearance error: %s", requestClearanceResponse.getDescription()));
            } else {
                logger.info(format("requestClearances response: %s", requestClearanceResponse));
            }
        } else {
            logger.info(format("requestClearances error: %s", response.getError().getTitle()));
        }
    }

    void one_a() throws ExecutionException, InterruptedException {
        ControlMeasure measure = new Odometer(110000, Odometer.Length.KILOMETERS);
        List<ControlMeasure> measures = List.of(measure);

        Response<RequestClearanceResponse> response =
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

    VehicleAccess three() throws ExecutionException, InterruptedException, IOException {
// use the stored VehicleAccess if it exists
        Optional<VehicleAccess> storedVehicleAccess = vehicleAccessStore.read(vin);
        if (storedVehicleAccess.isPresent()) return storedVehicleAccess.get();

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

        TelematicsResponse response = hmkitFleet.sendCommand(
          getVehicleSpeed,
          vehicleAccess
        ).get();

        if (response.getResponse() == null || response.getResponse().getStatus() != TelematicsCommandResponse.Status.OK)
            throw new RuntimeException("There was an error");

        Command commandFromVehicle = CommandResolver.resolve(response.getResponse().getResponseData());

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
