import com.highmobility.cryptok.AccessCertificate
import com.highmobility.value.Bytes
import model.AccessToken
import model.Brand
import model.VehicleAccess
import org.junit.Test

class StoreTest {
    @Test
    fun storeVehicleAccess() {
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
            Brand.DAIMLER_FLEET,
            AccessToken("1", "1", "1", 1, "1"),
            cert
        )
    }
}