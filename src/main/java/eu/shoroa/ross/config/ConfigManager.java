package eu.shoroa.ross.config;

import eu.shoroa.nori.NoriSerializer;
import eu.shoroa.nori.parse.Node;
import eu.shoroa.nori.parse.Property;
import eu.shoroa.ross.feature.module.*;
import eu.shoroa.ross.feature.setting.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static eu.shoroa.ross.Client.mc;
import static eu.shoroa.ross.Client.nori;

public final class ConfigManager {
    private static final Logger LOGGER = LogManager.getLogger("RossConfig");
    private static final File configFile = new File(mc.mcDataDir, "ross/config.nori");

    private ConfigManager() {
    }

    public static void load() {
        if (!configFile.exists()) return;
        StringBuilder content = new StringBuilder();
        try (Reader reader = new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8)) {
            while (reader.ready()) {
                content.append((char) reader.read());
            }

            Node<?> root = nori.parse(content.toString());
            Node.Obj modules = (Node.Obj) root.get("modules");
            for (Property property : modules.value.properties) {
                Module module = ModuleManager.getModule(property.token.buffer.toString());
                if (module == null) continue;
                Node.Obj moduleNode = (Node.Obj) modules.get(property.token.buffer.toString());
                module.setEnabled(moduleNode.get("enabled").getBool());
                for (SettingCategory cat : module.getSettings()) {
                    Node.Obj catNode = (Node.Obj) moduleNode.get("settings").get(cat.id);
                    for (Setting<?> setting : cat.getSettings()) {
                        Node<?> settingNode = catNode.get(setting.getId());
                        if (settingNode == null) continue;
                        switch (setting.getType()) {
                            case BOOLEAN:
                                ((BooleanSetting) setting).set(settingNode.getBool());
                                break;
                            case NUMBER:
                                ((NumberSetting) setting).set(settingNode.getFloat());
                                break;
                            case MODE:
                            case BIND:
                            case COLOR:
                                setting.setFromString(settingNode.getString().toString());
                                break;
                        }
                    }
                }

                if (module instanceof HUDModule) {
                    HUDModule hm = (HUDModule) module;
                    Node.Obj hudNode = (Node.Obj) moduleNode.get("hud");
                    if (hudNode != null) {
                        try {
                            Node.Obj elementsNode = (Node.Obj) hudNode.get("elements");
                            for (Property elementProperty : elementsNode.value.properties) {
                                HUDElement element = hm.getElement(elementProperty.token.buffer.toString());
                                if (element == null) continue;
                                Node.Obj elementNode = (Node.Obj) elementsNode.get(elementProperty.token.buffer.toString());
                                element.setPlacement(
                                        HUDAnchor.valueOf(hudNode.get("anchor").getString().toString()),
                                        hudNode.get("x").getDouble(),
                                        hudNode.get("y").getDouble());
                            }
                        } catch (Exception e) {
                            LOGGER.warn("Ignoring bad HUD placement for " + module.name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        Node.Obj root = nori.newObject(null);
        Node.Obj modules = nori.newObject("modules");
        for (Module module : ModuleManager.getModules()) {
            Node.Obj moduleNode = nori.newObject(module.name.replaceAll("[\\s-]", "_"));
            nori.insertProperty(moduleNode, "enabled", nori.newBool(module.isEnabled()));

            Node.Obj settingNode = nori.newObject("settings");
            for (SettingCategory cat : module.getSettings()) {
                Node.Obj catNode = nori.newObject(cat.id);
                for (Setting<?> setting : cat.getSettings()) {
                    switch (setting.getType()) {
                        case BOOLEAN:
                            nori.insertProperty(catNode, setting.getId(), nori.newBool(((BooleanSetting) setting).get()));
                            break;
                        case NUMBER:
                            nori.insertProperty(catNode, setting.getId(), nori.newDouble(((NumberSetting) setting).get().doubleValue()));
                            break;
                        case MODE:
                            nori.insertProperty(catNode, setting.getId(), nori.newString(((ModeSetting<?>) setting).getCurrentString()));
                            break;
                        case COLOR:
                            nori.insertProperty(catNode, setting.getId(), nori.newString(String.format("#%08X", ((ColorSetting) setting).getRGB())));
                            break;
                        case BIND:
                            nori.insertProperty(catNode, setting.getId(), nori.newString(((BindSetting) setting).toConfigString()));
                            break;
                    }
                }
                nori.insertProperty(settingNode, null, catNode);
            }
            nori.insertProperty(moduleNode, null, settingNode);
            if (module instanceof HUDModule) {
                HUDModule hm = (HUDModule) module;
                Node.Obj hudNode = nori.newObject("hud");
                Node.Obj elementsNode = nori.newObject("elements");

                List<HUDElement> elements = ((HUDModule) module).getElements();

                if (!elements.isEmpty()) {
                    for (HUDElement element : elements) {
                        Node.Obj elementNode = nori.newObject(element.getId());
                        nori.insertProperty(elementNode, "anchor", nori.newString(element.getAnchor().name()));
                        nori.insertProperty(elementNode, "x", nori.newDouble(element.getOffsetX()));
                        nori.insertProperty(elementNode, "y", nori.newDouble(element.getOffsetY()));
                    }
                }

                nori.insertProperty(hudNode, null, elementsNode);
                nori.insertProperty(moduleNode, null, hudNode);
            }
            nori.insertProperty(modules, null, moduleNode);
        }
        nori.insertProperty(root, null, modules);

        configFile.getParentFile().mkdirs();
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8)) {
            writer.write(NoriSerializer.stringify(root));
        } catch (Exception e) {
            LOGGER.error("Failed to save nori config", e);
        }
    }
}
