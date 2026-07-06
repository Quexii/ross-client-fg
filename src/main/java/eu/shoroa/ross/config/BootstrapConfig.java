package eu.shoroa.ross.config;

public final class BootstrapConfig {

    public final boolean verbose;

    public BootstrapConfig() {
        this.verbose = Boolean.parseBoolean(System.getProperty("ross.verbose", "false"));
    }
}