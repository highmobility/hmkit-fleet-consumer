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

import com.highmobility.credentials.CredentialsStore;
import com.highmobility.hmkitfleet.HMKitConfiguration;
import com.highmobility.hmkitfleet.HMKitCredentials;
import com.highmobility.hmkitfleet.HMKitFleet;
import com.highmobility.hmkitfleet.model.Brand;
import com.highmobility.hmkitfleet.model.ClearanceStatus;
import com.highmobility.hmkitfleet.model.ControlMeasure;
import com.highmobility.hmkitfleet.model.EligibilityStatus;
import com.highmobility.hmkitfleet.model.Odometer;
import com.highmobility.hmkitfleet.model.RequestClearanceResponse;
import com.highmobility.hmkitfleet.network.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;

class WebServer {
  final String vin1 = "C0NNECT0000000000";
  final String vin2 = "C0NNECT0000000001";

  final Logger logger = LoggerFactory.getLogger(this.getClass());

  WebServer() throws IOException {
  }

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
    new WebServer().start();
  }

  Optional<HMKitCredentials> readCredentials() {
    // either create the credentials
//    return Optional.of(new HMKitOAuthCredentials(
//      "client_id",
//      "client_secret"
//    ));

    // or read them from a file/db
    HMKitCredentials credentials = CredentialsStore.readOAuthCredentials().get();
    return Optional.of(credentials);
  }

  void start() throws IOException, ExecutionException, InterruptedException {
    logger.info("Start " + getDate());

    Optional<HMKitCredentials> credentials = readCredentials();
    if (!credentials.isPresent()) {
      logger.info("Credentials not found");
      System.exit(1);
    }

    HMKitConfiguration configuration = new HMKitConfiguration.Builder()
      .credentials(credentials.get())
      .environment(HMKitFleet.Environment.SANDBOX)
      .build();

    HMKitFleet hmkit = new HMKitFleet(configuration);

    Brand brand = Brand.SANDBOX;

    // # get whether the vehicle is eligible for clearance

    getEligibility(hmkit, vin1, brand);

    // # request clearance

//         requestClearance(hmkit, vin1, brand);
    // requestClearance(hmkit, vin2, brand);

    // # verify clearance

    // getClearanceStatuses(hmkit);
//        getClearanceStatus(hmkit, vin1);

    getVehicleStatus(hmkit, vin1);

    // # delete clearance

    // deleteClearance(hmkit, vin2);

    logger.info("End: " + getDate());
    System.exit(0);
  }

  private void getEligibility(HMKitFleet hmkit, String vin, Brand brand) throws ExecutionException, InterruptedException {
    Response<EligibilityStatus> response = hmkit.getEligibility(vin, brand).get();

    if (response.getResponse() != null) {
      logger.info(format("getEligibility response: %s", response.getResponse()));
    } else {
      logger.info(format("getEligibility error: %s", response.getError().getTitle()));
    }
  }

  private void requestClearance(HMKitFleet hmkit, String vin, Brand brand) throws ExecutionException, InterruptedException {
    ControlMeasure measure = new Odometer(110000, Odometer.Length.KILOMETERS);
    List<ControlMeasure> measures = new ArrayList<>();
    measures.add(measure);

    Response<RequestClearanceResponse> response =
      hmkit.requestClearance(
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

  private void getClearanceStatuses(HMKitFleet hmkit) throws ExecutionException, InterruptedException {
    Response<List<ClearanceStatus>> response = hmkit.getClearanceStatuses().get();

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


  private void getClearanceStatus(HMKitFleet hmkit, String vin) throws ExecutionException, InterruptedException {
    Response<ClearanceStatus> response = hmkit.getClearanceStatus(vin).get();
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

  private void getVehicleStatus(HMKitFleet hmkit, String vin) throws ExecutionException, InterruptedException {
    Response<String> response = hmkit.getVehicleState(vin).get();

    if (response.getResponse() != null) {
      String jsonString = response.getResponse();

      logger.info(format("getVehicleState response: %s", jsonString));
    } else {
      logger.info(format("getVehicleState error: %s", response.getError().getTitle()));
    }
  }

  private void deleteClearance(HMKitFleet hmkitFleet, String vin) throws ExecutionException, InterruptedException {
    Response<RequestClearanceResponse> response = hmkitFleet.deleteClearance(vin).get();

    if (response.getError() != null) {
      logger.info(format("deleteClearance error: %s", response.getError().getDetail()));
    } else {
      logger.info("deleteClearance success");
    }
  }

  String getDate() {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
    return LocalTime.now().format(formatter);
  }
}