class WebServer {
    public static void main(String[] args) {
        FleetSdk sdk = FleetSdk.INSTANCE;
        sdk.init("apiKey");
    }
}