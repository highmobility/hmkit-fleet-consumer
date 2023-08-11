import com.highmobility.hmkitfleet.HMKitFleet;
import com.highmobility.hmkitfleet.model.Brand;
import com.highmobility.hmkitfleet.model.ClearanceStatus;
import com.highmobility.hmkitfleet.model.ControlMeasure;
import com.highmobility.hmkitfleet.model.Odometer;
import com.highmobility.hmkitfleet.model.RequestClearanceResponse;
import com.highmobility.hmkitfleet.network.Response;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;

// test this link manually
// https://github.com/highmobility/hmkit-fleet-consumer/blob/main/hmkit-fleet-consumer/src/main/java/VehicleAccessStore.java#L27

class DevCenterSnippetTests {
    // if this class fails to build, the relevant docs page needs to be updated

    // https://docs.high-mobility.com/guides/getting-started/fleet/
    void snippet() {
        String privateKey = "{\"inserted_at\":\"2023-08-09T04:24:24\",\"private_key\":\"-----BEGIN PRIVATE KEY-----\\BaSE64\\n-----END PRIVATE KEY-----\",\"instance_uri\":\"https://sandbox.api.high-mobility.com\",\"token_uri\":\"https://sandbox.api.high-mobility.com/v1/auth_tokens\",\"id\":\"ea30adf3-2d8b-4276-9e48-d1ab65cfa685\"}";
        HMKitFleet hmkit = new HMKitFleet(privateKey);
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

        List<ControlMeasure> measures = new ArrayList<>();
        measures.add(measure);

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

    // TODO: add new vehicle status snippet
}
