import com.highmobility.hmkitfleet.model.VehicleAccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import kotlinx.serialization.json.Json;

/**
 * Stores/reads VehicleAccess object in vehicleAccess.json file.
 * <p>
 * VehicleAccess is encoded with kotlinx.serialization encoder, which results in
 * a JSON formatted string. That string is written to the vehicleAccess.json file.
 * Storing to file could be replaced with a database in production.
 * <p>
 * It is essential that VehicleAccess is stored in the database with the
 * same level of security as passwords. This is because VehicleAccess can be used
 * to access the real vehicle.
 */
class VehicleAccessStore {
    Path path = Paths.get("vehicleAccess.json");

    public void store(VehicleAccess vehicleAccess) throws IOException {
        String encoded = Json.Default.encodeToString(VehicleAccess.Companion.serializer(), vehicleAccess);

        // TODO: store securely
        Files.write(path, encoded.getBytes(), StandardOpenOption.CREATE);
    }

    public Optional<VehicleAccess> read(String vin) {
        try {
            // read file from path

            String content = new String(Files.readAllBytes(path));

            VehicleAccess vehicleAccess = Json.Default.decodeFromString(
              VehicleAccess.Companion.serializer(), content
            );

            if (vehicleAccess.getVin().equals(vin)) {
                return Optional.of(vehicleAccess);
            }
        } catch (IOException e) {
            System.out.println("Cannot read vehicleAccess.json file");
        }

        return Optional.empty();
    }
}
