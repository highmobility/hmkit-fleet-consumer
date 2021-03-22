### Requirements

Java 11 runtime

# HMKit Fleet Consumer

HMKit Fleet is a Kotlin/Java library that combines different API-s: OAuth, Service Account API and
Telematics API to help car companies manage their fleet.

# Table of contents

* [Requirements](#Requirements)
* [Architecture](#Architecture)
* [Setup](#Setup)
* [Contributing](#Contributing)
* [Licence](#Licence)

### Requirements

Java 11+

### Architecture

HMKit Fleet Consumer is a Java application that provides examples about how to use Fleet SDK
methods.

### Setup

* import the Gradle project
* run the tests `./gradlew test`
* add credentials.yaml file to src/main/resources folder with keys described
  in `ServiceAccountApiConfiguration.java` or use the initialise snippet in WebServer.java, instead
  of `configurationStore.read()`
* run the WebServer.java main() method with `./gradlew run` or withing your IDE.

### Contributing

Before starting please read our contribution rules 📘[Contributing](CONTRIBUTE.md)

### Licence

This repository is using MIT licence. See more in 📘[LICENCE](LICENCE.md)