import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.highmobility.crypto.DeviceCertificate;
import com.highmobility.value.Bytes;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import model.Brand;
import model.ControlMeasure;
import model.Odometer;
import network.Response;
import model.ClearanceStatus;

class WebServer {
    final String testVin = "C0NNECT0000000005";

    ServiceAccountApiConfiguration configuration;
    HMKitFleet hmkitFleet = HMKitFleet.INSTANCE;

    public static void main(String[] args) throws IOException {
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

    void start() {
        System.out.println("Start " + getDate());
        hmkitFleet.setEnvironment(HMKitFleet.Environment.DEV);
        hmkitFleet.setConfiguration(configuration);

        getClearanceStatuses();
//        requestClearances();
    }

    private void requestClearances() {
        ControlMeasure measure = new Odometer(110000, Odometer.Length.KILOMETERS);

        CompletableFuture<Response<ClearanceStatus>> requestClearance =
                hmkitFleet.requestClearance(
                        testVin,
                        Brand.MERCEDES_BENZ,
                        List.of(measure)
                );

        requestClearance.thenApply(status -> {
            System.out.println(String.format("clear vehicle status response: %s", status.getResponse()));
            System.out.println(String.format("clear vehicle status error: %s", status.getError().getTitle()));
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
                    System.out.println(String.format("clear vehicle status response"));
                    for (ClearanceStatus status : statuses.getResponse()) {
                        System.out.println(String.format(String.format("status: %s:%s",
                                status.getVin(),
                                status.getStatus())));
                    }
                }
            } else {
                System.out.println(String.format("clear vehicle status error: %s", statuses.getError().getTitle()));
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