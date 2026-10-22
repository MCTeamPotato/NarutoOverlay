package me.kall.narutooverlay.event.fps;

import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.card.Field;
import me.kall.narutooverlay.event.api.FpsUpdateEvent;
import me.kall.narutooverlay.text.TextLine;
import me.kall.narutooverlay.theme.ValueLevel;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.ToDoubleFunction;

import static me.kall.narutooverlay.util.Texts.label;
import static me.kall.narutooverlay.util.Texts.value;

public final class FpsCard extends Card<FpsUpdateEvent> {
    private static final String FPS_SAMPLE = "0000";

    public FpsCard(ForgeConfigSpec.@NotNull Builder builder) {
        super(builder, "FPS", 10, 5, true, List.of(
                metric("inst", "INST: ", true, event -> event.inst),
                metric("min", "MIN: ", false, event -> event.min),
                metric("avg", "AVG: ", false, event -> event.avg),
                metric("low", "1% LOW: ", false, event -> event.low)
        ));
    }

    public void update(@NotNull FpsUpdateEvent event) {
        this.accept(event);
    }

    private static @NotNull Field<FpsUpdateEvent> metric(@NotNull String id, @NotNull String caption, boolean enabledByDefault, @NotNull ToDoubleFunction<FpsUpdateEvent> getter) {
        return Field.of(id, true, false, enabledByDefault, (builder, event, config) -> {
            if (config.showLabel()) builder.text(label(caption));
            builder.slot(fps(getter.applyAsDouble(event)), FPS_SAMPLE, TextLine.Align.RIGHT);
        });
    }

    private static @NotNull MutableComponent fps(double value) {
        int fps = (int) value;
        ValueLevel level = fps < 30 ? ValueLevel.CRITICAL : fps < 60 ? ValueLevel.WARN : ValueLevel.NORMAL;
        return value(String.valueOf(fps), level);
    }
}
