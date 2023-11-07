package com.highmobility.credentials;

import com.highmobility.hmkitfleet.HMKitOAuthCredentials;
import com.highmobility.hmkitfleet.HMKitPrivateKeyCredentials;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import kotlinx.serialization.json.Json;

/**
 * Stores/reads Credentials object in credentialsOAuth.json file.
 * <p>
 * Credentials is encoded with Kotlin Serialization encoder, which results in
 * a JSON string. That string is written to the credentialsOAuth.json file.
 * Storing to file could be replaced with a database in production.
 * <p>
 * It is essential that Credentials is stored in the database with the
 * same level of security as passwords. This is because Credentials can be used
 * to access the real vehicle.
 */
public class CredentialsStore {
  static Path oauthPath = Paths.get("credentialsOAuth.json");
  static Path privateKeyPath = Paths.get("credentialsPrivateKey.json");

  public static void storeOAuthCredentials(HMKitOAuthCredentials vehicleAccess) throws IOException {
    String encoded = Json.Default.encodeToString(HMKitOAuthCredentials.Companion.serializer(), vehicleAccess);

    // TODO: store securely
    Files.write(oauthPath, encoded.getBytes(), StandardOpenOption.CREATE);
  }

  public static Optional<HMKitOAuthCredentials> readOAuthCredentials() {
    try {
      // read file from path
      String content = new String(Files.readAllBytes(oauthPath));

      HMKitOAuthCredentials oAuthCredentials = Json.Default.decodeFromString(
        HMKitOAuthCredentials.Companion.serializer(), content
      );

      return Optional.of(oAuthCredentials);
    } catch (IOException e) {
      System.out.println("Cannot read Credentials file");
    }

    return Optional.empty();
  }


  public static void storePrivateKeyCredentials(HMKitPrivateKeyCredentials vehicleAccess) throws IOException {
    String encoded = Json.Default.encodeToString(HMKitPrivateKeyCredentials.Companion.serializer(), vehicleAccess);
    // TODO: store securely
    Files.write(privateKeyPath, encoded.getBytes(), StandardOpenOption.CREATE);
  }

  public static Optional<HMKitPrivateKeyCredentials> readPrivateKeyCredentials() {
    try {
      // read file from path
      String content = new String(Files.readAllBytes(privateKeyPath));

      HMKitPrivateKeyCredentials privateKeyCredentials = Json.Default.decodeFromString(
        HMKitPrivateKeyCredentials.Companion.serializer(), content
      );

      return Optional.of(privateKeyCredentials);
    } catch (IOException e) {
      System.out.println("Cannot read Credentials file");
    }

    return Optional.empty();
  }
}
