package eu.shoroa.ross.feature.module.impl.misc;

import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.mixins.interfaces.ISoundHandler;
import eu.shoroa.ross.mixins.interfaces.ISoundManager;
import eu.shoroa.ross.utils.player.ChatUtil;
import net.minecraft.client.audio.SoundManager;
import org.jetbrains.annotations.ApiStatus;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecWav;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

import static eu.shoroa.ross.Client.mc;

public class ModuleSilence extends Module {
    private final File soundsFolder;
    private final WatchService watchService;

    {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final SettingCategory settings = addCategory("Settings", "settings", "settings");
    private final NumberSetting minInterval = register(new NumberSetting("Min Interval", "minInterval", 10, 1, 100, 1), settings);
    private final NumberSetting maxInterval = register(new NumberSetting("Max Interval", "maxInterval", 30, 1, 100, 1), settings);

    private static final float MIN_DELAY = 5_000f;
    private static final float MAX_DELAY = 3_600_000f;
    private final List<SoundSource> sounds = new ArrayList<>();
    private SoundSystem sndSystem;
    private boolean loaded;
    private final Random random = new Random();

    public ModuleSilence() {
        super("Silence", "Not silence at all lol", Category.MISC, "\uf3b0");

        soundsFolder = new File(mc.mcDataDir, "ross/silence");
        if (!soundsFolder.exists()) {
            soundsFolder.mkdirs();
        }

        Path path = soundsFolder.toPath();
        try {
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean resolveSndSystem() {
        SoundManager soundManager = ((ISoundHandler) mc.getSoundHandler()).getSoundManager();
        if (soundManager == null || ((ISoundManager) soundManager).getSoundSystem() == null) return true;

        if (((ISoundManager) soundManager).getSoundSystem() != sndSystem) {
            sndSystem = ((ISoundManager) soundManager).getSoundSystem();
            loaded = false;
            sounds.clear();
            registerCodecs();
        }

        return false;
    }

    private static float toDelay(float dial) {
        return MIN_DELAY * (float) Math.pow(MAX_DELAY / MIN_DELAY, (dial - 1f) / 99f);
    }

    private void registerCodecs() {
        try {
            SoundSystemConfig.setCodec("wav", CodecWav.class);
        } catch (SoundSystemException e) {
            ChatUtil.info("Failed to register wav codec: " + e.getMessage());
        }
    }

    private void populateSounds() throws MalformedURLException {
        if (mc.thePlayer != null && mc.theWorld != null) {
            ChatUtil.info("Reloading sounds from " + soundsFolder.getAbsolutePath());
        }

        if (resolveSndSystem()) return;

        for (SoundSource sound : sounds) {
            sndSystem.removeSource(sound.id);
        }
        sounds.clear();

        File[] files = soundsFolder.listFiles();
        if (files == null) return;

        for (File s : files) {
            if (s.isFile()) {
                SoundSource src = new SoundSource(s.getName() + System.currentTimeMillis(), s.toURI().toURL());
                sounds.add(src);
                sndSystem.newStreamingSource(true, src.id, src.url, s.getName(), false, 0, 0, 0, 0, 0);
                src.scheduleNext();


                if (mc.thePlayer != null && mc.theWorld != null) {
                    ChatUtil.info("Loaded sound: " + s.getName());
                }
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        sounds.clear();
        try {
            populateSounds();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        for (SoundSource sound : sounds) {
            sound.scheduleNext();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (sndSystem == null) return;

        for (SoundSource sound : sounds) {
            sndSystem.stop(sound.id);
        }
    }

    @Subscribe
    @ApiStatus.Internal
    public void onTick(EventTick e) {
        if (resolveSndSystem()) return;

        if (!loaded) {
            try {
                populateSounds();
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
            loaded = true;
        }

        WatchKey key = watchService.poll();
        if (key != null) {
            boolean dirty = false;
            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE || event.kind() == StandardWatchEventKinds.ENTRY_DELETE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    dirty = true;
                }
            }
            key.reset();

            if (dirty) {
                try {
                    populateSounds();
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        long now = System.currentTimeMillis();
        for (SoundSource sound : sounds) {
            if (now < sound.nextPlay) continue;

            if (!sndSystem.playing(sound.id)) {
                sndSystem.play(sound.id);
            }
            sound.scheduleNext();
        }
    }

    private class SoundSource {
        final String id;
        final URL url;
        long nextPlay;

        private SoundSource(String id, URL url) {
            this.id = id;
            this.url = url;
        }

        /** Each source rolls its own delay so they never fire in lockstep. */
        void scheduleNext() {
            float a = toDelay(minInterval.get());
            float b = toDelay(maxInterval.get());
            float lo = Math.min(a, b);
            float hi = Math.max(a, b);

            nextPlay = System.currentTimeMillis() + (long) (lo + random.nextFloat() * (hi - lo));
        }
    }
}
