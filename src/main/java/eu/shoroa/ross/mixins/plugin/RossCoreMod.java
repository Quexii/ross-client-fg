package eu.shoroa.ross.mixins.plugin;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

public class RossCoreMod implements IFMLLoadingPlugin {

    public RossCoreMod() {
        Launch.classLoader.addTransformerExclusion("eu.shoroa.ross.hud.");
        Launch.classLoader.addClassLoaderExclusion("io.github.humbleui.");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
