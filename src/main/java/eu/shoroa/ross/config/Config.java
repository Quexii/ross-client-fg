package eu.shoroa.ross.config;

public class Config {
    private final BootstrapConfig BOOTSTRAP_CONFIG = new BootstrapConfig();

    public BootstrapConfig bootstrap() {
        return BOOTSTRAP_CONFIG;
    }
}
