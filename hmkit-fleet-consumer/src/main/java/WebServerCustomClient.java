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
import com.highmobility.hmkitfleet.HMKitConfiguration;
import com.highmobility.hmkitfleet.HMKitFleet;
import com.highmobility.hmkitfleet.ServiceAccountApiConfiguration;
import com.highmobility.hmkitfleet.model.Brand;
import com.highmobility.hmkitfleet.model.ClearanceStatus;
import com.highmobility.hmkitfleet.model.ControlMeasure;
import com.highmobility.hmkitfleet.model.EligibilityStatus;
import com.highmobility.hmkitfleet.model.Odometer;
import com.highmobility.hmkitfleet.model.RequestClearanceResponse;
import com.highmobility.hmkitfleet.model.VehicleAccess;
import com.highmobility.hmkitfleet.network.Response;
import com.highmobility.hmkitfleet.network.TelematicsCommandResponse;
import com.highmobility.hmkitfleet.network.TelematicsResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;

import static java.lang.String.format;

class WebServerCustomClient {
    final String vin1 = "C0NNECT0000000000";

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    ServiceAccountApiConfigurationStore configurationStore = new ServiceAccountApiConfigurationStore();
    ServiceAccountApiConfiguration configuration = configurationStore.read();

    VehicleAccessStore vehicleAccessStore = new VehicleAccessStore();

    WebServerCustomClient() throws IOException {
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        new WebServerCustomClient().start();
    }

    void start() throws ExecutionException, InterruptedException {
        logger.info("Start " + getDate());

        HMKitConfiguration hmkitConfiguration = new HMKitConfiguration.Builder()
          .client(new OkHttpClient())
          .build();

        HMKitFleet hmkit = new HMKitFleet(
          configuration,
          HMKitFleet.Environment.SANDBOX,
          hmkitConfiguration
        );

        getEligibility(hmkit, vin1, Brand.SANDBOX);

        logger.info("End: " + getDate());
    }

    private void getEligibility(HMKitFleet hmkit, String vin, Brand brand) throws ExecutionException, InterruptedException {
        Response<EligibilityStatus> response = hmkit.getEligibility(vin, brand).get();

        if (response.getResponse() != null) {
            logger.info(format("getEligibility response: %s", response.getResponse()));
        } else {
            logger.info(format("getEligibility error: %s", response.getError().getTitle()));
        }
    }

    String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        return LocalTime.now().format(formatter);
    }
}