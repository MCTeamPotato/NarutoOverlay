package me.kall.narutooverlay.card.impl;

import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.card.Field;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.event.api.TaskRegistryEvent;
import me.kall.narutooverlay.event.GCTracker;
import me.kall.narutooverlay.text.TextLine;
import me.kall.narutooverlay.util.Formats;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static me.kall.narutooverlay.util.Texts.*;

public final class GcCard extends Card<GcCard.Snapshot> {
    public record Snapshot(@NotNull String name, long cost, @NotNull Formats.Quantity elapsed) {}

    private static final String NAME_SAMPLE = "G1 Young Generation";
    private static final String COST_SAMPLE = "000";
    private static final String AGO_NUMBER_SAMPLE = "00:00";
    private static final String AGO_UNIT_SAMPLE = " min";

    private static final Field<Snapshot> NAME = Field.of("name", true, false, false, (builder, snapshot, config) -> {
        if (config.showLabel()) builder.text(label("GC: "));
        builder.slot(value(snapshot.name()), NAME_SAMPLE, TextLine.Align.LEFT);
    });

    private static final Field<Snapshot> COST = Field.of("cost", true, true, false, (builder, snapshot, config) -> {
        if (config.showLabel()) builder.text(label("COST: "));
        builder.slot(value(Long.toString(snapshot.cost())), COST_SAMPLE, TextLine.Align.RIGHT);
        if (config.showUnit()) builder.text(unit(" ms"));
    });

    private static final Field<Snapshot> AGO = Field.of("ago", true, true, false, (builder, snapshot, config) -> {
        if (config.showLabel()) builder.text(label("AGO: "));
        builder.slot(value(snapshot.elapsed().number()), AGO_NUMBER_SAMPLE, TextLine.Align.RIGHT);
        if (config.showUnit()) builder.slot(unit(snapshot.elapsed().unit()), AGO_UNIT_SAMPLE, TextLine.Align.LEFT);
    });

    public GcCard(ForgeConfigSpec.@NotNull Builder builder) {
        super(builder, "GC", 10, 21, false, List.of(NAME, COST, AGO));
    }

    public void listen(@NotNull TaskRegistryEvent event) {
        event.register(RenderArgs.GC_COOLDOWN::get, this::active, () -> {
            Instant time = GCTracker.time();
            String name = GCTracker.name();
            if (time == null || name == null) return;

            Duration duration = Duration.between(time, Instant.now());
            double ago = (double) duration.getSeconds() + duration.getNano() / 1_000_000_000.0D;

            this.accept(new Snapshot(name, GCTracker.duration(), Formats.elapsed(ago)));
        });
    }
}
