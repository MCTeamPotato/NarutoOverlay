package me.kall.narutooverlay.card;

import me.kall.narutooverlay.text.TextLine;
import org.jetbrains.annotations.NotNull;

public record Field<D>(@NotNull String id, boolean hasLabel, boolean hasUnit, boolean enabledByDefault, @NotNull Appender<D> appender) {
    @FunctionalInterface
    public interface Appender<D> {
        void append(@NotNull TextLine.Builder builder, @NotNull D data, @NotNull FieldConfig config);
    }

    public static <D> @NotNull Field<D> of(@NotNull String id, boolean hasLabel, boolean hasUnit, @NotNull Appender<D> appender) {
        return of(id, hasLabel, hasUnit, true, appender);
    }

    public static <D> @NotNull Field<D> of(@NotNull String id, boolean hasLabel, boolean hasUnit, boolean enabledByDefault, @NotNull Appender<D> appender) {
        return new Field<>(id, hasLabel, hasUnit, enabledByDefault, appender);
    }

    void append(@NotNull TextLine.Builder builder, @NotNull D data, @NotNull FieldConfig config) {
        this.appender.append(builder, data, config);
    }
}
