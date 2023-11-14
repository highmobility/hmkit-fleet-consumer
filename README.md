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

### Run the sample

* add your OAuth/Private key credentials to `WebServer.java`
```java
  Optional<HMKitCredentials> readCredentials() {
    return Optional.of(new HMKitOAuthCredentials(
      "client_id",
      "client_secret"
    ));
  }
```
* run the WebServer.java main() method with `./gradlew run` or within your IDE.

### Credentials storing

kotlinx.serialization library is used to encode the `HMKitPrivateKeyCredentials` / `HMKitOAuthCredentials` classes. This means you need to add Kotlin dependencies to the project.

Check out the `credentials-store` module to see what dependencies are required and how a sample implementation looks like.

### License

This repository is using the MIT license. See more in 📘[LICENSE](LICENSE)

### Contributing

Before starting, please read our contribution rules 📘[Contributing](CONTRIBUTING.md)