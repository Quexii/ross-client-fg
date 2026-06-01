package eu.shoroa.ross.yoga;

import static org.lwjgl.util.yoga.Yoga.*;

public class YGConfig {
    private long config;

    private boolean useLegacyStretch;
    private boolean useWebDefaults;
    private boolean useExperimentalWebFlex;
    private boolean useExperimentalAbsPadding;
    private boolean useExperimentalFixColumn;
    private float scaleFactor = 1;
    private boolean printTree = false;
    private long context;

    public YGConfig() {
        this.config = YGConfigNew();

        useLegacyStretch = YGConfigGetUseLegacyStretchBehaviour(config);
        useWebDefaults = YGConfigGetUseWebDefaults(config);
        useExperimentalWebFlex = YGConfigIsExperimentalFeatureEnabled(config, YGExperimentalFeatureWebFlexBasis);
        useExperimentalAbsPadding = YGConfigIsExperimentalFeatureEnabled(config, YGExperimentalFeatureAbsolutePercentageAgainstPaddingEdge);
        useExperimentalFixColumn = YGConfigIsExperimentalFeatureEnabled(config, YGExperimentalFeatureFixAbsoluteTrailingColumnMargin);
        context = YGConfigGetContext(config);
    }

    public void setUseLegacyStretch(boolean value) {
        this.useLegacyStretch = value;
        YGConfigSetUseLegacyStretchBehaviour(config, value);
    }

    public void setUseWebDefaults(boolean value) {
        this.useWebDefaults = value;
        YGConfigSetUseWebDefaults(config, value);
    }

    public void setUseExperimentalWebFlex(boolean value) {
        this.useExperimentalWebFlex = value;
        YGConfigSetExperimentalFeatureEnabled(config, YGExperimentalFeatureWebFlexBasis, value);
    }

    public void setUseExperimentalAbsPadding(boolean value) {
        this.useExperimentalAbsPadding = value;
        YGConfigSetExperimentalFeatureEnabled(config, YGExperimentalFeatureAbsolutePercentageAgainstPaddingEdge, value);
    }

    public void setUseExperimentalFixColumn(boolean value) {
        this.useExperimentalFixColumn = value;
        YGConfigSetExperimentalFeatureEnabled(config, YGExperimentalFeatureFixAbsoluteTrailingColumnMargin, value);
    }

    public void setScale(float value) {
        this.scaleFactor = value;
        YGConfigSetPointScaleFactor(config, value);
    }

    public void setPrintTree(boolean value) {
        this.printTree = value;
        YGConfigSetPrintTreeFlag(config, value);
    }

    public void setContext(long value) {
        this.context = value;
        YGConfigSetContext(value, this.context);
    }

    public long getHandle() {
        return config;
    }
}
