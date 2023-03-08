# HMKit Fleet Consumer

HMKit Fleet is a Kotlin/Java library that combines different API-s: OAuth, Service Account API and
Telematics API to help car companies manage their fleet.

### Requirements

Java 8+

### Architecture

Java app that can be run with gradle or within an IDE.

### Setup

* import the Gradle project
* run the tests `./gradlew test`
* add credentials.yaml file to src/main/resources folder with keys described
  in `ServiceAccountApiConfigurationStore.java` or use the initialise snippet in WebServer.java, instead
  of `configurationStore.read()`
* run the WebServer.java main() method with `./gradlew run` or withing your IDE.

### vehicleAccess.json

* Optionally add pre-existing vehicleAccess.json at project root level with format:
```
{
  "vin": "VIN123",
  "accessToken": {
    "token_type": "bearer",
    "scope": "windows.get.positions vehicle_location.get.altitude vehicle_location.get.heading vehicle_location.get.coordinates usage.get.acceleration_evaluation trunk.get.position trunk.get.lock rooftop_control.get.sunroof_state rooftop_control.get.convertible_roof_state maintenance.get.kilometers_to_next_service maintenance.get.days_to_next_service lights.get.interior_lights lights.get.reading_lamps ignition.get.status fueling.get.gas_flap_lock doors.get.locks_state doors.get.positions doors.get.locks diagnostics.get.odometer diagnostics.get.tire_pressures diagnostics.get.battery_voltage diagnostics.get.fuel_level diagnostics.get.speed diagnostics.get.mileage charging.get.status charging.get.charging_rate_kw charging.get.battery_level charging.get.estimated_range",
    "refresh_token": "00000000-0000-0000-0000-000000000000",
    "expires_in": 3600,
    "access_token": "00000000-0000-0000-0000-000000000000"
  },
  "accessCertificate": "01786D6266A6....hex bytes"
}

```

kotlinx.serialization library is used to parse the json file. This means you need to add Kotlin dependencies to the project. These dependencies are visible in the `vehicle-access-store/build.gradle` file. Separate module is used for `vehicle-access-store` to avoid adding Kotlin dependencies to the main project.

### License

This repository is using the MIT license. See more in 📘[LICENSE](LICENSE)

### Contributing

Before starting, please read our contribution rules 📘[Contributing](CONTRIBUTING.md)