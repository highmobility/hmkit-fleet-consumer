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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.highmobility.crypto.DeviceCertificate;
import com.highmobility.hmkitfleet.ServiceAccountApiConfiguration;
import com.highmobility.value.Bytes;

import java.io.File;
import java.io.IOException;
/*
use credentials.yaml in resources folder to decode a ServiceAccountApiConfiguration object
required keys:
serviceAccountPrivateKey: ""
serviceAccountApiKey: ""

clientCertificate: ""
clientPrivateKey: ""

oauthClientId: ""
oauthClientSecret: ""
*/

class ServiceAccountApiConfigurationStore {
    String path;

    ServiceAccountApiConfigurationStore() {
        path = getClass().getClassLoader().getResource("credentials.yaml").getFile();
    }

    ServiceAccountApiConfigurationStore(String path) {
        this.path = getClass().getClassLoader().getResource(path).getFile();
    }

    public ServiceAccountApiConfiguration read() throws IOException {
        File credentialsFile = new File(path);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(DeviceCertificate.class, new DeviceCertificateSerialiser());

        ObjectMapper om = new ObjectMapper(new YAMLFactory())
          .registerModule(new KotlinModule())
          .registerModule(module);

        return om.readValue(credentialsFile, ServiceAccountApiConfiguration.class);
    }

    private static class DeviceCertificateSerialiser extends StdDeserializer<DeviceCertificate> {
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
