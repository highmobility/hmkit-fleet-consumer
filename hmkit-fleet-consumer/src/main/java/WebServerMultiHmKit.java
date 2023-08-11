// TODO

///*
// * The MIT License
// *
// * Copyright (c) 2023- High-Mobility GmbH (https://high-mobility.com)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// */
//
//import com.highmobility.autoapi.Command;
//import com.highmobility.autoapi.CommandResolver;
//import com.highmobility.autoapi.Diagnostics;
//import com.highmobility.autoapi.FailureMessage;
//import com.highmobility.hmkitfleet.HMKitFleet;
//import com.highmobility.hmkitfleet.model.Brand;
//import com.highmobility.hmkitfleet.model.ClearanceStatus;
//import com.highmobility.hmkitfleet.model.ControlMeasure;
//import com.highmobility.hmkitfleet.model.EligibilityStatus;
//import com.highmobility.hmkitfleet.model.Odometer;
//import com.highmobility.hmkitfleet.model.RequestClearanceResponse;
//import com.highmobility.hmkitfleet.model.VehicleAccess;
//import com.highmobility.hmkitfleet.network.Response;
//import com.highmobility.hmkitfleet.network.TelematicsCommandResponse;
//import com.highmobility.hmkitfleet.network.TelematicsResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.ExecutionException;
//
//import static java.lang.String.format;
//
//class WebServerMultiHmKit {
//    final String vin1 = "C0NNECT0000000000"; // sandbox simulator 1
//    final String vin2 = "C0NNECT0000000001"; // sandbox simulator 2
//
//    final Logger logger = LoggerFactory.getLogger(this.getClass());
//    // Instead of reading from storage, you could also set the configuration by creating a new
//    // ServiceAccountApiConfiguration object.
//    ServiceAccountApiConfigurationStore configurationStore = new ServiceAccountApiConfigurationStore();
//    ServiceAccountApiConfigurationStore configurationStoreTwo = new ServiceAccountApiConfigurationStore("credentials2.yaml");
//
//    // Store the VehicleAccess object for later use
//    VehicleAccessStore vehicleAccessStore = new VehicleAccessStore();
//
//    WebServerMultiHmKit() {
//    }
//
//    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
//        new WebServerMultiHmKit().start();
//    }
//
//    void start() throws IOException, ExecutionException, InterruptedException {
//        logger.info("Start " + getDate());
//
//        HMKitFleet hmkitOne = new HMKitFleet(
//          configurationStore.read(),
//          HMKitFleet.Environment.SANDBOX
//        );
//
//        HMKitFleet hmkitTwo = new HMKitFleet(
//          configurationStoreTwo.read(),
//          HMKitFleet.Environment.SANDBOX
//        );
//
//        Brand brand = Brand.SANDBOX;
//
//        // # get whether the vehicle is eligible for clearance
//
//        getEligibility(hmkitOne, vin1, brand);
//        getEligibility(hmkitTwo, vin2, brand);
//
//        // # request clearance
//
//        requestClearance(hmkitOne, vin1, brand);
//        requestClearance(hmkitTwo, vin2, brand);
//
//        // # verify clearance
//
//        getClearanceStatus(hmkitOne, vin1);
//        getClearanceStatus(hmkitTwo, vin2);
//
//        // # get vehicle access object and diagnostics state.
//
//        // TODO: new request
////        VehicleAccess vehicleAccess = getVehicleAccess(hmkitOne, vin1);
////        getVehicleDiagnostics(hmkitOne, vehicleAccess);
////
////        VehicleAccess vehicleAccessTwo = getVehicleAccess(hmkitTwo, vin2);
////        getVehicleDiagnostics(hmkitTwo, vehicleAccessTwo);
//
//        // # delete clearance
//
//         // deleteClearance(hmkitOne, vin1);
//
//        logger.info("End: " + getDate());
//    }
//
//    private void getEligibility(HMKitFleet hmkit, String vin, Brand brand) throws ExecutionException, InterruptedException {
//        Response<EligibilityStatus> response = hmkit.getEligibility(vin, brand).get();
//
//        if (response.getResponse() != null) {
//            logger.info(format("getEligibility response: %s", response.getResponse()));
//        } else {
//            logger.info(format("getEligibility error: %s", response.getError().getTitle()));
//        }
//    }
//
//    private void requestClearance(HMKitFleet hmkit, String vin, Brand brand) throws ExecutionException, InterruptedException {
//        ControlMeasure measure = new Odometer(110000, Odometer.Length.KILOMETERS);
//        List<ControlMeasure> measures = new ArrayList<>();
//        measures.add(measure);
//
//        Response<RequestClearanceResponse> response =
//          hmkit.requestClearance(
//            vin,
//            brand,
//            measures
//          ).get();
//
//        if (response.getResponse() != null) {
//            logger.info(format("requestClearances response: %s", response.getResponse()));
//        } else {
//            logger.info(format("requestClearances error: %s", response.getError().getTitle()));
//        }
//    }
//
//    private void getClearanceStatuses(HMKitFleet hmkit) throws ExecutionException, InterruptedException {
//        Response<List<ClearanceStatus>> response = hmkit.getClearanceStatuses().get();
//
//        if (response.getResponse() != null) {
//            logger.info("getClearanceStatuses response");
//            for (ClearanceStatus status : response.getResponse()) {
//                logger.info(format("status: %s:%s",
//                  status.getVin(),
//                  status.getStatus()));
//            }
//        } else {
//            logger.info(format("getClearanceStatuses error: %s", response.getError().getTitle()));
//        }
//    }
//
//
//    private void getClearanceStatus(HMKitFleet hmkit, String vin) throws ExecutionException, InterruptedException {
//        Response<ClearanceStatus> response = hmkit.getClearanceStatus(vin).get();
//        ClearanceStatus status = response.getResponse();
//
//        if (status != null) {
//            logger.info("getClearanceStatus response");
//            logger.info(format("status: %s:%s",
//              status.getVin(),
//              status.getStatus()));
//        } else {
//            logger.info(format("getClearanceStatus error: %s", response.getError().getTitle()));
//        }
//    }
//
////
////    private VehicleAccess getVehicleAccess(HMKitFleet hmkit, String vin) throws
////      ExecutionException, InterruptedException, IOException {
////        // If you're having problems with stored VehicleAccess object, you can delete the vehicleAccess.json file from
////        // the project directory.
////        Optional<VehicleAccess> storedVehicleAccess = vehicleAccessStore.read(vin);
////        if (storedVehicleAccess.isPresent()) return storedVehicleAccess.get();
////
////        Response<VehicleAccess> accessResponse = hmkit.getVehicleAccess(vin).get();
////        if (accessResponse.getError() != null)
////            throw new RuntimeException(accessResponse.getError().getDetail());
////
////        VehicleAccess serverVehicleAccess = accessResponse.getResponse();
////        vehicleAccessStore.store(serverVehicleAccess);
////        return serverVehicleAccess;
////    }
//
//
//    // TODO: new rest request
////    private void getVehicleDiagnostics(HMKitFleet hmkit, VehicleAccess vehicleAccess) throws
////      ExecutionException, InterruptedException {
////        // make sure you have Get Vehicle Odometer permission in your console app
////        Command getDiagnostics = new Diagnostics.GetState(Diagnostics.PROPERTY_ODOMETER);
////
////        TelematicsResponse response = hmkit.sendCommand(
////          getDiagnostics,
////          vehicleAccess
////        ).get();
////
////        // First check if we have a telematics response
////        if (response.getResponse() == null) {
////            throw new RuntimeException(format("%s - %s", response.getErrors().get(0).getTitle(), response.getErrors().get(0).getDetail()));
////        }
////
////        // Then check if the Telematics response is an error
////        if (response.getResponse().getStatus() != TelematicsCommandResponse.Status.OK) {
////            throw new RuntimeException(format("Telematics command response: %s - %s", response.getResponse().getStatus(), response.getResponse().getMessage()));
////        }
////
////        // Now we can be sure that the Telematics response is a command
////        TelematicsCommandResponse telematicsResponse = response.getResponse();
////
////        logger.info(format("Got telematics response: %s - %s", telematicsResponse.getMessage(), telematicsResponse.getStatus()));
////
////        Command commandFromVehicle = CommandResolver.resolve(telematicsResponse.getResponseData());
////
////        if (commandFromVehicle instanceof Diagnostics.State) {
////            Diagnostics.State diagnostics = (Diagnostics.State) commandFromVehicle;
////            if (diagnostics.getOdometer().getValue() != null) {
////                logger.info(format(
////                  " > diagnostics.odometer: %s",
////                  diagnostics.getOdometer().getValue().getValue()));
////            } else {
////                logger.info(format(" > diagnostics.bytes: %s", diagnostics));
////            }
////        } else if (commandFromVehicle instanceof FailureMessage.State) {
////            FailureMessage.State failureMessage = (FailureMessage.State) commandFromVehicle;
////
////            logger.info(format(
////              " > FailureMessage: %s, %s",
////              failureMessage.getFailureReason().getValue(),
////              failureMessage.getFailureDescription().getValue()));
////        }
////    }
//
//    private void deleteClearance(HMKitFleet hmkitFleet, String vin) throws ExecutionException, InterruptedException {
//        Response<RequestClearanceResponse> response = hmkitFleet.deleteClearance(vin).get();
//
//        if (response.getError() != null) {
//            logger.info(format("deleteClearance error: %s", response.getError().getDetail()));
//        } else {
//            logger.info("deleteClearance success");
//        }
//    }
//
//    String getDate() {
//        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
//        return LocalTime.now().format(formatter);
//    }
//}