package me.kall.narutooverlay.card;

import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FieldConfig {
    private final ForgeConfigSpec.BooleanValue enabled;
    private final ForgeConfigSpec.@Nullable BooleanValue showLabel;
    private final ForgeConfigSpec.@Nullable BooleanValue showUnit;

    FieldConfig(ForgeConfigSpec.@NotNull Builder builder, boolean hasLabel, boolean hasUnit, boolean enabledByDefault) {
        this.enabled = builder
                .comment("Whether this field is shown.")
                .define("enabled", enabledByDefault);
        this.showLabel = hasLabel
                ? builder.comment("Whether the caption in front of the value is shown.").define("showLabel", true)
                : null;
        this.showUnit = hasUnit
                ? builder.comment("Whether the unit behind the value is shown.").define("showUnit", true)
                : null;
    }

    public boolean enabled() {
        return this.enabled.get();
    }

    public boolean showLabel() {
        return this.showLabel == null || this.showLabel.get();
    }

    public boolean showUnit() {
        return this.showUnit == null || this.showUnit.get();
    }

    public @NotNull ForgeConfigSpec.BooleanValue enabledValue() {
        return this.enabled;
    }

    public ForgeConfigSpec.@Nullable BooleanValue showLabelValue() {
        return this.showLabel;
    }

    public ForgeConfigSpec.@Nullable BooleanValue showUnitValue() {
        return this.showUnit;
    }

    public void reset() {
        this.enabled.set(this.enabled.getDefault());
        if (this.showLabel != null) this.showLabel.set(true);
        if (this.showUnit != null) this.showUnit.set(true);
    }
}
