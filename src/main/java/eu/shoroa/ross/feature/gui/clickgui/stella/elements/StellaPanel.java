package eu.shoroa.ross.feature.gui.clickgui.stella.elements;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.gui.GuiElement;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.gui.clickgui.stella.events.StellaEvents;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.module.ModuleManager;
import eu.shoroa.ross.utils.math.Mth;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.Images;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.DampFloat;
import io.github.humbleui.skija.*;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.List;

public class StellaPanel extends GuiElement {
    private final StellaCategory[] categories;
    final float sidebarW = 140;
    final float sideButtonH = 110;
    private int selectedCategoryIndex = 0;
    private Category selectedCategory = Category.values()[selectedCategoryIndex];
    private DampFloat selectedCategoryEase = new DampFloat();
    private float selectedCategoryEaseTarget = 0f;
    private DampFloat modulesEase = new DampFloat();
    private List<StellaModule> modules = new ArrayList<>();

    private final float moduleSpacing = 10f;
    private final float moduleHeight = 160;
    private final int modulesColumns = 4;

    public StellaPanel(float x, float y, float width, float height) {
        super(x, y, width, height);

        float moduleWidth = (getWidth() - sidebarW - (modulesColumns - 1) * moduleSpacing) / modulesColumns - moduleSpacing / 2;

        categories = new StellaCategory[Category.values().length];
        for (int i = 0; i < categories.length; i++) {
            categories[i] = new StellaCategory(0, 80 + sideButtonH * i, sidebarW, sideButtonH, Category.values()[i]);
        }

        for (Module module : ModuleManager.getModules()) {
            modules.add(new StellaModule(0f, 0f, moduleWidth, moduleHeight, module));
        }

        sortModules();
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        selectedCategoryEaseTarget = selectedCategoryIndex;
        Mth.smoothDamp(selectedCategoryEase, selectedCategoryEaseTarget, 1f / 15, (float) Animate.getDelta());
        Mth.smoothDamp(modulesEase, 1, 1f / 10f, (float) Animate.getDelta());

        final float categoryY = 80f;

        Canvas canvas = Client.INSTANCE.getSkia().getCanvas();

        try (Paint p = new Paint()) {
            p.setColor(StellaTheme.get().accent);
            UI.drawRRect(getX(), getY(), getWidth(), getHeight(), 16f, p);

            UI.save();
            UI.clipRRect(getX(), getY(), getWidth(), getHeight(), 16f);
            p.setColorFilter(ColorFilter.makeBlend(StellaTheme.get().accentHalftone, BlendMode.SRC_IN));
            canvas.drawImage(Images.HALFTONE_CIRCLE.getImage(), getX() - Images.HALFTONE_CIRCLE.getWidth() / 2 - 4, getY() + getHeight() - Images.HALFTONE_CIRCLE.getHeight() / 2 + 9, p);
            p.setColorFilter(null);
            UI.restore();

            p.setColor(StellaTheme.get().surface);
            UI.drawRRect(getX() + sidebarW, getY(), getWidth() - sidebarW, getHeight(), 16f, p);
            p.setColor(StellaTheme.get().surfaceDim);
            UI.drawRRect(getX() + sidebarW, getY() + 80, getWidth() - sidebarW, getHeight() - 80, 0f, 0f, 16f, 16f, p);

            p.setColor(StellaTheme.get().accentDeep);
            UI.drawRect(getX(), getY() + categoryY + selectedCategoryEase.value * sideButtonH, sidebarW, sideButtonH, p);

            float arrowSize = 35f;
            p.setColor(StellaTheme.get().surfaceDim);

            try (PathBuilder pb = new PathBuilder()) {
                pb.moveTo(getX() + sidebarW, getY() + categoryY + selectedCategoryEase.value * sideButtonH + (sideButtonH - arrowSize) / 2);
                pb.lineTo(getX() + sidebarW - 18, getY() + categoryY + selectedCategoryEase.value * sideButtonH + sideButtonH / 2);
                pb.lineTo(getX() + sidebarW, getY() + categoryY + selectedCategoryEase.value * sideButtonH + (sideButtonH + arrowSize) / 2);
                UI.drawPath(pb.build(), p);
            }

            p.setColor(StellaTheme.get().divider);
            UI.drawRect(getX() + sidebarW, getY() + categoryY, getWidth() - sidebarW, 2, p);

            p.setColor(Color.withA(StellaTheme.get().foreground, (int) (255 * modulesEase.value)));
            UI.drawText(selectedCategory.name, getX() + sidebarW + 80, getY() + 40f + 10f * (1 - modulesEase.value), Fonts.GoogleFlex.weight(500), 30f, Align.CENTER_LEFT, p);
            UI.drawText(MaterialIcons.LOCATION_ON, getX() + sidebarW + 40, getY() + 40f + 10f * (1 - modulesEase.value), Fonts.MaterialIcons.weight(600).grade(200).opticSize(20).fill(true), 30f, Align.CENTER_LEFT, p);
        }

        for (int i = 0; i < categories.length; i++) {
            StellaCategory category = categories[i];
            category.setX(getX());
            category.setY(getY() + 80 + sideButtonH * i);
            category.render(mouseX, mouseY, partialTicks);
        }

        int modX = 0;
        int modY = 0;
        UI.saveLayerAlpha(0f, 0f, Display.getWidth(), Display.getHeight(), modulesEase.value);
        UI.translate(0f, 10f * (1 - modulesEase.value));
        for (StellaModule module : modules) {
            if (!module.isVisible()) continue;
            float targetX = getX() + sidebarW + modX * (module.getWidth() + moduleSpacing) + moduleSpacing;
            float targetY = getY() + categoryY + modY * (module.getHeight() + moduleSpacing) + moduleSpacing;

            module.setX(targetX);
            module.setY(targetY);
            module.render(mouseX, mouseY, partialTicks);

            modX++;
            if (modX >= modulesColumns) {
                modX = 0;
                modY++;
            }
        }
        UI.restore();
    }

    @Subscribe
    @ApiStatus.Internal
    public void onChangeCategory(StellaEvents.ChangeCategory e) {
        int newIndex = e.category.ordinal();

        if (newIndex != selectedCategoryIndex) {
            selectedCategoryIndex = e.category.ordinal();
            selectedCategory = e.category;
            sortModules();
        }
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        for (StellaCategory category : categories) {
            if (category.input(mouseX, mouseY, event)) {
                return true;
            }
        }
        for (StellaModule module : modules) {
            if (module.isVisible() && module.input(mouseX, mouseY, event)) {
                return true;
            }
        }
        return false;
    }

    private void sortModules() {
        for (StellaModule module : modules) {
            if (!module.getModule().category.equals(selectedCategory)) {
                module.hide();
            } else {
                module.show();
            }
        }

        modulesEase.value = 0f;
        modulesEase.velocity = 0f;
    }
}
