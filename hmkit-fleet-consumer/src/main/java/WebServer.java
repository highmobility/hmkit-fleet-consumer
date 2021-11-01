/*
 * The MIT License
 *
 * Copyright (c) 2014- High-Mobility GmbH (https://high-mobility.com)
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
import com.highmobility.value.Bytes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.highmobility.hmkitfleet.model.Brand;
import com.highmobility.hmkitfleet.model.ClearanceStatus;
import com.highmobility.hmkitfleet.model.ControlMeasure;
import com.highmobility.hmkitfleet.model.Odometer;
import com.highmobility.hmkitfleet.model.VehicleAccess;
import com.highmobility.hmkitfleet.network.Response;

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

    void start() throws ExecutionException, InterruptedException, IOException {
        logger.info("Start " + getDate());

        hmkitFleet.setConfiguration(configurationStore.read());

//        requestClearance(testVin);
//        requestClearance(testVin2);

//        getClearanceStatuses();

        VehicleAccess vehicleAccess = getVehicleAccess(testVin);
        getVehicleDiagnostics(vehicleAccess);

//        revokeClearance(testVin2);

        logger.info("End: " + getDate());
    }

    private void revokeClearance(String vin) throws ExecutionException, InterruptedException {
        VehicleAccess vehicleAccess = vehicleAccessStore.read(vin);

        if (vehicleAccess == null) {
            logger.warn(format("Vehicle access does not exist for %s", vin));
        } else {
            Response<Boolean> response = hmkitFleet.revokeClearance(vehicleAccess).get();

            if (response.getError() != null) {
                logger.info(format("revokeClearance error: %s", response.getError().getDetail()));
            } else {
                logger.info(format("revokeClearance success"));
            }
        }
    }

    private void requestClearance(String vin) throws ExecutionException, InterruptedException {
        ControlMeasure measure = new Odometer(110000, Odometer.Length.KILOMETERS);
        List<ControlMeasure> measures = List.of(measure);

        Response<ClearanceStatus> response =
                hmkitFleet.requestClearance(
                        vin,
                        Brand.MERCEDES_BENZ,
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

        Response<VehicleAccess> accessResponse = hmkitFleet.getVehicleAccess(vin).get();
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
        } else if (commandFromVehicle instanceof FailureMessage.State) {
            FailureMessage.State failureMessage = (FailureMessage.State) commandFromVehicle;
            logger.info(format(
                    "Got FailureMessage response: %s, %s",
                    failureMessage.getFailureReason().getValue(),
                    failureMessage.getFailureDescription().getValue()));
        }
    }

    String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        return LocalTime.now().format(formatter);
    }
}