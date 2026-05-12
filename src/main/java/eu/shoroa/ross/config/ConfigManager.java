package eu.shoroa.ross.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.module.Bind;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.notification.Notifications;
import eu.shoroa.ross.settings.*;

import java.awt.*;
import java.io.*;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static eu.shoroa.ross.Client.mc;

public class ConfigManager {
    private File rootDir = new File(mc.mcDataDir, "ross");
    private File configsDir = new File(rootDir, "configs");
    private File initCfg = new File(configsDir, "init.cfg");

    private String currentConfig = "default";

    private final static Gson gson = new Gson();

    private static final long SAVE_DEBOUNCE_MS = 200L;
    private final Object saveLock = new Object();
    private final ScheduledExecutorService saveExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "ross-config-save");
            thread.setDaemon(true);
            return thread;
        }
    });
    private ScheduledFuture<?> pendingSave;

    public void init() throws IOException {
        if (!rootDir.exists()) rootDir.mkdir();
        if (!configsDir.exists()) configsDir.mkdir();
        if (!initCfg.exists()) {
            initCfg.createNewFile();
        }

        File defaultCfg = new File(configsDir, "default.json");
        if (!defaultCfg.exists()) {
            defaultCfg.createNewFile();
        }
    }

    public void save() throws IOException {
        saveInit();

        saveConfig(currentConfig);
    }

    public void saveQueued() {
        synchronized (saveLock) {
            if (pendingSave != null) {
                pendingSave.cancel(false);
            }
            pendingSave = saveExecutor.schedule(this::saveQuietly, SAVE_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void saveQuietly() {
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException {
        loadInit();

        loadConfig(currentConfig);
    }

    public void loadInit() throws IOException {
        Properties props = new Properties();
        try (FileReader reader = new FileReader(initCfg)) {
            props.load(reader);
        }
        currentConfig = props.getProperty("lastConfig", "default");
    }

    public void saveInit() throws IOException {
        Properties props = new Properties();
        props.setProperty("lastConfig", currentConfig);
        try (FileWriter writer = new FileWriter(initCfg)) {
            props.store(writer, "Saving initial configuration..");
        }
    }

    public void saveConfig(String name) {
        File file = new File(configsDir, name + ".json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // use gson
        Module[] modules = ModuleManager.getModules();
        JsonObject json = new JsonObject();
        for (Module module : modules) {
            JsonObject moduleJson = new JsonObject();
            JsonObject bindJson = new JsonObject();
            JsonObject settingsJson = new JsonObject();
            if (module.bind != null) {
                bindJson.addProperty("key", module.bind.key);
                bindJson.addProperty("type", module.bind.type.name());
                bindJson.addProperty("action", module.bind.action.name());
            }

            for (Setting<?> setting : module.getSettings()) {
                switch (setting.getType()) {
                    case BOOLEAN:
                        settingsJson.addProperty(setting.getId(), (Boolean) setting.get());
                        break;
                    case NUMBER:
                        settingsJson.addProperty(setting.getId(), (Number) setting.get());
                        break;
                    case MODE:
                        settingsJson.addProperty(setting.getId(), ((ModeSetting) setting).getCurrent().name());
                        break;
                    case COLOR:
                        settingsJson.addProperty(setting.getId(), ((ColorSetting) setting).get().getRGB());
                        break;
                }
            }

            moduleJson.addProperty("enabled", module.isEnabled());

            if (module.bind != null) {
                moduleJson.add("bind", bindJson);
            }

            moduleJson.add("settings", settingsJson);

            json.add(module.name, moduleJson);
        }

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig(String name) {
        File file = new File(configsDir, name + ".json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                if (json == null) {
                    // TODO: queue notification: failed to load config
                    Notifications.add("Config Error", "Failed to load config " + name);
                    if (currentConfig.equals("default")) {
                        return;
                    }
                    loadConfig("default");
                }
                for (Module module : ModuleManager.getModules()) {
                    if (json.has(module.name)) {
                        JsonObject moduleJson = json.getAsJsonObject(module.name);
                        JsonObject settingsJons = moduleJson.getAsJsonObject("settings");

                        boolean enabled = moduleJson.get("enabled").getAsBoolean();

                        Bind newBind = null;
                        if (moduleJson.has("bind")) {
                            JsonObject bindJson = moduleJson.getAsJsonObject("bind");
                            newBind = new Bind(
                                    bindJson.get("key").getAsInt(),
                                    EventInput.Type.valueOf(bindJson.get("type").getAsString()),
                                    EventInput.Action.valueOf(bindJson.get("action").getAsString())
                            );
                        }

                        for (Setting<?> setting : module.getSettings()) {
                            if (settingsJons.has(setting.getId())) {
                                switch (setting.getType()) {
                                    case BOOLEAN:
                                        ((BooleanSetting) setting).set(settingsJons.get(setting.getId()).getAsBoolean());
                                        break;
                                    case NUMBER:
                                        ((NumberSetting) setting).set(settingsJons.get(setting.getId()).getAsNumber().floatValue());
                                        break;
                                    case MODE:
                                        ModeSetting modeSetting = (ModeSetting) setting;
                                        String modeName = settingsJons.get(setting.getId()).getAsString();
                                        for (int i = 0; i < modeSetting.getModes().size(); i++) {
                                            if (((Enum) modeSetting.getModes().get(i)).name().equals(modeName)) {
                                                modeSetting.setIndex(i);
                                                break;
                                            }
                                        }
                                        break;
                                    case COLOR:
                                        ((ColorSetting) setting).set(new Color(settingsJons.get(setting.getId()).getAsInt(), true));
                                        break;
                                }
                            }
                        }

                        module.bind = newBind;
                        module.setEnabled(enabled);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loadConfig("default");
        }
    }
}
