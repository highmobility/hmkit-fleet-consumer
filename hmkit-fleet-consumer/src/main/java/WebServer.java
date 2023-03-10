/*
 * The MIT License
 *
 * Copyright (c) 2023- High-Mobility GmbH (https://high-mobility.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import com.highmobility.autoapi.Command;
import com.highmobility.autoapi.CommandResolver;
import com.highmobility.autoapi.Diagnostics;
import com.highmobility.autoapi.FailureMessage;
import com.highmobility.hmkitfleet.HMKitFleet;
import com.highmobility.hmkitfleet.model.EligibilityStatus;
import com.highmobility.hmkitfleet.model.RequestClearanceResponse;
import com.highmobility.hmkitfleet.model.VehicleAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.highmobility.hmkitfleet.model.Brand;
import com.highmobility.hmkitfleet.model.ClearanceStatus;
import com.highmobility.hmkitfleet.model.ControlMeasure;
import com.highmobility.hmkitfleet.model.Odometer;
import com.highmobility.hmkitfleet.network.Response;
import com.highmobility.hmkitfleet.network.TelematicsCommandResponse;
import com.highmobility.hmkitfleet.network.TelematicsResponse;
import com.highmobility.value.Bytes;

import static java.lang.String.format;

class WebServer {
    final String testVin = "C0NNECT0000000009";
    final String testVin2 = "C0NNECT0000000010";

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    ServiceAccountApiConfigurationStore configurationStore = new ServiceAccountApiConfigurationStore();
    VehicleAccessStore vehicleAccessStore = new VehicleAccessStore();
    HMKitFleet hmkitFleet = HMKitFleet.INSTANCE;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        new WebServer().start();
    }

    void start() throws IOException, ExecutionException, InterruptedException {
        logger.info("Start " + getDate());

        hmkitFleet.setEnvironment(HMKitFleet.Environment.SANDBOX);
        hmkitFleet.setConfiguration(configurationStore.read());

        Brand brand = Brand.SANDBOX;
        getEligibility("1HM9CY66SL1gNPGORE", brand);

//        requestClearance("1HM9CY66SL1gNPGORE", brand);
//        requestClearance(testVin2);
//        getClearanceStatuses();
//        getClearanceStatus(testVin);
//        VehicleAccess vehicleAccess = getVehicleAccess(testVin);
//        getVehicleDiagnostics(vehicleAccess);
//        revokeClearance(testVin2);
//        deleteClearance(testVin2);

        logger.info("End: " + getDate());
    }

    private void getEligibility(String vin, Brand brand) throws ExecutionException, InterruptedException {
        Response<EligibilityStatus> response = hmkitFleet.getEligibility(vin, brand).get();

        if (response.getResponse() != null) {
            logger.info(format("getEligibility response: %s", response.getResponse()));
        } else {
            logger.info(format("getEligibility error: %s", response.getError().getTitle()));
        }
    }

    private void requestClearance(String vin, Brand brand) throws ExecutionException, InterruptedException {
        ControlMeasure measure = new Odometer(110000, Odometer.Length.KILOMETERS);
        List<ControlMeasure> measures = new ArrayList<>();
        measures.add(measure);

        Response<RequestClearanceResponse> response =
          hmkitFleet.requestClearance(
            vin,
            brand,
            measures
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
            logger.info("getClearanceStatuses response");
            for (ClearanceStatus status : response.getResponse()) {
                logger.info(format("status: %s:%s",
                  status.getVin(),
                  status.getStatus()));
            }
        } else {
            logger.info(format("getClearanceStatuses error: %s", response.getError().getTitle()));
        }
    }


    private void getClearanceStatus(String vin) throws ExecutionException, InterruptedException {
        Response<ClearanceStatus> response = hmkitFleet.getClearanceStatus(vin).get();
        ClearanceStatus status = response.getResponse();

        if (status != null) {
            logger.info("getClearanceStatus response");
            logger.info(format("status: %s:%s",
              status.getVin(),
              status.getStatus()));
        } else {
            logger.info(format("getClearanceStatus error: %s", response.getError().getTitle()));
        }
    }


    private VehicleAccess getVehicleAccess(String vin) throws ExecutionException, InterruptedException, IOException {
        Optional<VehicleAccess> storedVehicleAccess = vehicleAccessStore.read(vin);
        if (storedVehicleAccess.isPresent()) return storedVehicleAccess.get();

        Response<VehicleAccess> accessResponse = hmkitFleet.getVehicleAccess(vin).get();
        if (accessResponse.getError() != null)
            throw new RuntimeException(accessResponse.getError().getDetail());

        VehicleAccess serverVehicleAccess = accessResponse.getResponse();
        vehicleAccessStore.store(serverVehicleAccess);
        return serverVehicleAccess;
    }

    private void getVehicleDiagnostics(VehicleAccess vehicleAccess) throws ExecutionException, InterruptedException {
        Command getVehicleSpeed = new Diagnostics.GetState(Diagnostics.PROPERTY_SPEED);

        TelematicsResponse response = hmkitFleet.sendCommand(
          new Bytes("0D11AF0003"),
          vehicleAccess
        ).get();

        // First check if we have a telematics response
        if (response.getResponse() == null) {
            throw new RuntimeException(format("%s - %s", response.getErrors().get(0).getTitle(), response.getErrors().get(0).getDetail()));
        }

        // Then check if the Telematics response is an error
        if (response.getResponse().getStatus() != TelematicsCommandResponse.Status.OK) {
            throw new RuntimeException(format("Telematics command response: %s - %s", response.getResponse().getStatus(), response.getResponse().getMessage()));
        }

        // Now we can be sure that the Telematics response is a command
        TelematicsCommandResponse telematicsResponse = response.getResponse();

        logger.info(format("Got telematics response: %s - %s", telematicsResponse.getMessage(), telematicsResponse.getStatus()));

        Command commandFromVehicle = CommandResolver.resolve(telematicsResponse.getResponseData());

        if (commandFromVehicle instanceof Diagnostics.State) {
            Diagnostics.State diagnostics = (Diagnostics.State) commandFromVehicle;
            if (diagnostics.getSpeed().getValue() != null) {
                logger.info(format(
                  " > diagnostics.speed: %s",
                  diagnostics.getSpeed().getValue().getValue()));
            } else {
                logger.info(format(" > diagnostics.bytes: %s", diagnostics));
            }
        } else if (commandFromVehicle instanceof FailureMessage.State) {
            FailureMessage.State failureMessage = (FailureMessage.State) commandFromVehicle;

            logger.info(format(
              " > FailureMessage: %s, %s",
              failureMessage.getFailureReason().getValue(),
              failureMessage.getFailureDescription().getValue()));
        }
    }

    private void deleteClearance(String vin) throws ExecutionException, InterruptedException {
        Response<RequestClearanceResponse> response = hmkitFleet.deleteClearance(vin).get();

        if (response.getError() != null) {
            logger.info(format("deleteClearance error: %s", response.getError().getDetail()));
        } else {
            logger.info("deleteClearance success %s");
        }
    }

    String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        return LocalTime.now().format(formatter);
    }
}