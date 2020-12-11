import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.highmobility.autoapi.Command;
import com.highmobility.autoapi.CommandResolver;
import com.highmobility.autoapi.Diagnostics;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.crypto.DeviceCertificate;
import com.highmobility.value.Bytes;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import jdk.jshell.Diag;
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

    ServiceAccountApiConfiguration configuration;
    HMKitFleet hmkitFleet = HMKitFleet.INSTANCE;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        new WebServer().start();
    }

    WebServer() throws IOException {
        String path = getClass().getClassLoader().getResource("credentials.yaml").getFile();
        File credentialsFile = new File(path);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(DeviceCertificate.class, new ItemDeserializer());

        ObjectMapper om = new ObjectMapper(new YAMLFactory())
                .registerModule(new KotlinModule())
                .registerModule(module);

        /*
        use credentials.yaml to decode a ServiceAccountApiConfiguration object
        required keys:
        privateKey: ""
        apiKey: ""
        clientCertificate: ""
         */
        configuration = om.readValue(credentialsFile, ServiceAccountApiConfiguration.class);
    }

    void start() throws ExecutionException, InterruptedException {
        System.out.println("Start " + getDate());
        hmkitFleet.setEnvironment(HMKitFleet.Environment.DEV);
        hmkitFleet.setConfiguration(configuration);

//        getClearanceStatuses();
//        requestClearances();


        Response<VehicleAccess> access = hmkitFleet.getVehicleAccess(testVin, Brand.DAIMLER_FLEET).get();
        if (access.getError() != null)
            throw new RuntimeException(access.getError().getDetail());

        Response<Bytes> diagnosticsResponse = hmkitFleet.sendCommand(
                new Diagnostics.GetState(),
                access.getResponse()
//                getTestVehicleAccess()
        ).get();

        if (diagnosticsResponse.getError() != null)
            throw new RuntimeException(diagnosticsResponse.getError().getTitle());

        Command command = CommandResolver.resolve(diagnosticsResponse.getResponse());
        if (command instanceof Diagnostics.State) {
            Diagnostics.State diagnostics = (Diagnostics.State) command;
            System.out.println(format(
                    "Got diagnostics response: %s", diagnostics.getOdometer().getValue().getValue()));
        }
    }

    VehicleAccess getTestVehicleAccess() {
        Bytes certBytes = new Bytes("AXRtY3PCraQBV0wVyyE9BPBK+P+iozdRW5wu2RpzHcgM8Wtfr+M/Opgf4uQjyKEkhC1VghuVLYF/VnO0XiiU+uz6I3DQNYs2+GsaXOWnTIDuUrU8SivxFAwJCQQZDAkJBBAQB//9/+//////HwAAAAAAj3wuvOhRoyk9edcp8LhAfL8hd75q/Tdb4pLgxC3GPGXuZcOuPdxrbxQvKc+8VMQPNQVffbCFD3Mj8c0QpKEUBg==");
        AccessCertificate cert = new AccessCertificate(certBytes);
        return new VehicleAccess(testVin,
                Brand.DAIMLER_FLEET,
                new AccessToken("1", "1", "1", 1, "1"),
                cert);
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
            System.out.println(format("clear vehicle status response: %s", status.getResponse()));
            System.out.println(format("clear vehicle status error: %s", status.getError().getTitle()));
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
                    System.out.println(format("clear vehicle status response"));
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

class ItemDeserializer extends StdDeserializer<DeviceCertificate> {
    public ItemDeserializer() {
        this(null);
    }

    public ItemDeserializer(Class<DeviceCertificate> t) {
        super(t);
    }

    @Override
    public DeviceCertificate deserialize(JsonParser p,
                                         DeserializationContext ctx) throws IOException {
        return new DeviceCertificate(new Bytes(p.getText()));
    }
}