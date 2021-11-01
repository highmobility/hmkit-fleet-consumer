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
import com.charleskorn.kaml.Yaml
import com.highmobility.hmkitfleet.model.VehicleAccess
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

    fun read(vin: String): VehicleAccess? {
        return try {
            val content = Files.readString(path)

            val vehicleAccess =
                Yaml.default.decodeFromString(
                    VehicleAccess.serializer(),
                    content
                )
            
            if (vehicleAccess.vin == vin) {
                vehicleAccess
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
