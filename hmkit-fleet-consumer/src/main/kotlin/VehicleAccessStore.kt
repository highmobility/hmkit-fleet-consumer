import com.charleskorn.kaml.Yaml
import model.VehicleAccess
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class VehicleAccessStore {
    val path = Paths.get("vehicleAccess.yaml")

    fun store(vehicleAccess: VehicleAccess) {
        val encoded = Yaml.default.encodeToString(
            VehicleAccess.serializer(),
            vehicleAccess
        )

        // TODO: this should be stored securely
        Files.write(path, encoded.toByteArray(), StandardOpenOption.CREATE)
    }

    fun read(vin:String): VehicleAccess? {
        return try {
            val content = Files.readString(path)

            val vehicleAccess =
                Yaml.default.decodeFromString(
                    VehicleAccess.serializer(),
                    content
                )

            vehicleAccess
        } catch (e: Exception) {
            null
        }
    }
}
