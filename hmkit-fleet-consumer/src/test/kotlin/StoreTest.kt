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
import com.highmobility.crypto.AccessCertificate
import com.highmobility.value.Bytes
import io.mockk.*
import com.highmobility.hmkitfleet.model.AccessToken
import com.highmobility.hmkitfleet.model.VehicleAccess
import org.junit.Test
import java.nio.file.Files

class StoreTest {
    @Test
    fun storeVehicleAccess() {
        mockkStatic(Files::class)
        every { Files.write(any(), any<ByteArray>(), any()) } returns mockk()

        val store = VehicleAccessStore()
        val vehicleAccess = getTestVehicleAccess()
        store.store(vehicleAccess)
    }

    fun getTestVehicleAccess(): VehicleAccess {
        val certBytes =
            Bytes("AXRtY3PCraQBV0wVyyE9BPBK+P+iozdRW5wu2RpzHcgM8Wtfr+M/Opgf4uQjyKEkhC1VghuVLYF/VnO0XiiU+uz6I3DQNYs2+GsaXOWnTIDuUrU8SivxFAwJCQQZDAkJBBAQB//9/+//////HwAAAAAAj3wuvOhRoyk9edcp8LhAfL8hd75q/Tdb4pLgxC3GPGXuZcOuPdxrbxQvKc+8VMQPNQVffbCFD3Mj8c0QpKEUBg==")
        val cert = AccessCertificate(certBytes)
        return VehicleAccess(
            "000000000000000000",
            AccessToken("1", "1", "1", 1, "1"),
            cert
        )
    }
}