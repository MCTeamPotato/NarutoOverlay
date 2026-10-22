package me.kall.narutooverlay.event.ram;

import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.card.Field;
import me.kall.narutooverlay.event.api.RAMUpdateEvent;
import me.kall.narutooverlay.text.TextLine;
import me.kall.narutooverlay.theme.ValueLevel;
import me.kall.narutooverlay.util.Formats;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.kall.narutooverlay.util.Texts.label;
import static me.kall.narutooverlay.util.Texts.separator;
import static me.kall.narutooverlay.util.Texts.unit;
import static me.kall.narutooverlay.util.Texts.value;

public final class RamCard extends Card<RAMUpdateEvent> {
    private static final String PERCENT_SAMPLE = "100%";
    private static final String MEMORY_SAMPLE = "99999";

    private static final Field<RAMUpdateEvent> USAGE = Field.of("usage", true, true, false, (builder, event, config) -> {
        ValueLevel level = level(usage(event));
        String max = Formats.memory(event.max);

        if (config.showLabel()) builder.text(label("RAM: "));
        builder.slot(value(Formats.memory(event.used), level), max, TextLine.Align.RIGHT)
                .text(separator("/"))
                .text(value(max));
        if (config.showUnit()) builder.text(unit(Formats.memoryUnit()));
    });

    private static final Field<RAMUpdateEvent> PERCENT = Field.of("percent", false, false, false, (builder, event, config) -> {
        int percent = (int) Math.round(usage(event) * 100.0D);
        builder.slot(value(percent + "%"), PERCENT_SAMPLE, TextLine.Align.RIGHT);
    });

    private static final Field<RAMUpdateEvent> ALLOC = Field.of("alloc", true, true, false, (builder, event, config) -> {
        if (config.showLabel()) builder.text(label("ALLOC: "));
        builder.slot(value(Formats.memory(event.total)), MEMORY_SAMPLE, TextLine.Align.RIGHT);
        if (config.showUnit()) builder.text(unit(Formats.memoryUnit()));
    });

    public RamCard(ForgeConfigSpec.@NotNull Builder builder) {
        super(builder, "RAM", 10, 37, false, List.of(USAGE, PERCENT, ALLOC));
    }

    public void update(@NotNull RAMUpdateEvent event) {
        this.accept(event);
    }

    private static double usage(@NotNull RAMUpdateEvent event) {
        return event.max > 0L ? (double) event.used / (double) event.max : 0.0D;
    }

    private static @NotNull ValueLevel level(double usage) {
        return usage >= 0.9D ? ValueLevel.CRITICAL : usage >= 0.75D ? ValueLevel.WARN : ValueLevel.NORMAL;
    }
}
