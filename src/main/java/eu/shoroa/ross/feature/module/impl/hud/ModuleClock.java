package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.feature.module.HUDAnchor;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.skia.font.MaterialIcons;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ModuleClock extends TextHUDModule {
    private final SettingCategory settings = addCategory("Settings", ".", "settings");

    private final BooleanSetting twentyFourHour = register(new BooleanSetting("24-hour format", "format_24h", true), settings);

    private final BooleanSetting showSeconds = register(new BooleanSetting("Show seconds", "seconds", false), settings);

    public ModuleClock() {
        super("Clock", "Shows the local time", MaterialIcons.SCHEDULE, MaterialIcons.SCHEDULE);

        setDefaultPosition(HUDAnchor.RIGHT_TOP, -10, 12);
    }

    @Override
    protected String value() {
        StringBuilder pattern = new StringBuilder(twentyFourHour.get() ? "HH:mm" : "h:mm");

        if (showSeconds.get()) {
            pattern.append(":ss");
        }

        if (!twentyFourHour.get()) {
            pattern.append(" a");
        }

        return new SimpleDateFormat(pattern.toString()).format(new Date());
    }

    @Override
    protected String suffix() {
        return "";
    }
}
