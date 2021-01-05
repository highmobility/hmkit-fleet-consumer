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
/*
use credentials.yaml to decode a ServiceAccountApiConfiguration object
required keys:
privateKey: ""
apiKey: ""
clientCertificate: ""
clientPrivateKey: ""
clientId: ""
clientSecret: ""
 */

class ServiceAccountApiConfigurationStore {
    String path = getClass().getClassLoader().getResource("credentials.yaml").getFile();

    public ServiceAccountApiConfiguration read() throws IOException {
        File credentialsFile = new File(path);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(DeviceCertificate.class, new DeviceCertificateSerialiser());

        ObjectMapper om = new ObjectMapper(new YAMLFactory())
                .registerModule(new KotlinModule())
                .registerModule(module);


        return om.readValue(credentialsFile, ServiceAccountApiConfiguration.class);
    }

    private class DeviceCertificateSerialiser extends StdDeserializer<DeviceCertificate> {
        public DeviceCertificateSerialiser() {
            this(null);
        }

        public DeviceCertificateSerialiser(Class<DeviceCertificate> t) {
            super(t);
        }

        @Override
        public DeviceCertificate deserialize(JsonParser p,
                                             DeserializationContext ctx) throws IOException {
            return new DeviceCertificate(new Bytes(p.getText()));
        }
    }
}
