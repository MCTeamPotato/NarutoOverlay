package me.kall.narutooverlay.card;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.narutooverlay.backplane.BackplaneRenderer;
import me.kall.narutooverlay.config.ConfigVersion;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.text.TextLine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public abstract class Card<D> {
    private final String id;
    private final int defaultLeft;
    private final int defaultTop;

    private final ForgeConfigSpec.BooleanValue enabled;
    private final ForgeConfigSpec.IntValue left;
    private final ForgeConfigSpec.IntValue top;
    private final ForgeConfigSpec.ConfigValue<String> group;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> order;

    private final Map<String, Field<D>> fields = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<String, FieldConfig> configs = new Object2ObjectOpenHashMap<>();

    private @Nullable D data;
    private @Nullable TextLine line;
    private int builtVersion = -1;

    protected Card(ForgeConfigSpec.@NotNull Builder builder, @NotNull String id, int defaultLeft, int defaultTop, boolean enabledByDefault, @NotNull List<Field<D>> fields) {
        this.id = id;
        this.defaultLeft = defaultLeft;
        this.defaultTop = defaultTop;

        for (Field<D> field : fields) {
            if (this.fields.put(field.id(), field) != null) {
                throw new IllegalArgumentException("Duplicate field id '" + field.id() + "' in card " + id);
            }
        }

        builder.push(id);

        this.enabled = builder
                .comment("Whether the " + id + " overlay is shown.")
                .define("enabled", enabledByDefault);
        this.left = builder
                .comment("Horizontal position of the " + id + " overlay.")
                .defineInRange("left", defaultLeft, 0, Integer.MAX_VALUE);
        this.top = builder
                .comment("Vertical position of the " + id + " overlay.")
                .defineInRange("top", defaultTop, 0, Integer.MAX_VALUE);
        this.group = builder
                .comment(
                        "Non-empty group id shared by cards merged into one outline.",
                        "Managed by Shift-clicking cards in the chat screen; leave empty for a standalone card."
                )
                .define("group", "");
        this.order = builder
                .comment(
                        "Order of the fields, from left to right.",
                        "Available fields: " + String.join(", ", this.fields.keySet()) + ".",
                        "Unknown entries are ignored; fields that are not listed are appended in their default order."
                )
                .defineListAllowEmpty("order", new ArrayList<>(this.fields.keySet()), String.class::isInstance);

        builder.push("Fields");
        for (Field<D> field : this.fields.values()) {
            builder.push(field.id());
            this.configs.put(field.id(), new FieldConfig(builder, field.hasLabel(), field.hasUnit(), field.enabledByDefault()));
            builder.pop();
        }
        builder.pop();

        builder.pop();

        CardRegistry.register(this);
    }

    public final @NotNull String id() {
        return this.id;
    }

    public final int left() {
        return this.left.get();
    }

    public final int top() {
        return this.top.get();
    }

    public final boolean active() {
        if (!this.enabled.get()) return false;

        for (FieldConfig config : this.configs.values()) {
            if (config.enabled()) return true;
        }

        return false;
    }

    protected final void accept(@NotNull D data) {
        this.data = data;
        this.builtVersion = -1;
    }

    public final void moveTo(double left, double top) {
        this.left.set(Math.max(0, (int) left));
        this.top.set(Math.max(0, (int) top));
    }

    public final void resetPosition() {
        this.left.set(this.defaultLeft);
        this.top.set(this.defaultTop);
    }

    public final void resetAll() {
        this.resetPosition();
        this.enabled.set(this.enabled.getDefault());
        this.group.set("");
        this.order.set(new ArrayList<>(this.fields.keySet()));
        for (FieldConfig config : this.configs.values()) config.reset();
        ConfigVersion.bump();
    }

    public final @NotNull String group() {
        return this.group.get();
    }

    public final void setGroup(@NotNull String group) {
        this.group.set(group);
    }

    public final boolean contains(double mouseX, double mouseY) {
        TextLine own = this.line();
        if (own == null) return false;

        double left = this.left.get();
        double top = this.top.get();
        double width = this.width(own);
        double height = Minecraft.getInstance().font.lineHeight;

        return mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height;
    }

    final void render(@NotNull GuiGraphics graphics) {
        TextLine line = this.line();
        if (line == null) return;

        Font font = Minecraft.getInstance().font;

        int left = this.left.get();
        int top = this.top.get();
        int width = this.width(line);
        int height = font.lineHeight;

        int[] dividers = line.dividers();

        if (RenderArgs.DRAW_BACKPLANE.get()) {
            int border = CardRegistry.isSelected(this) ? 0xFFFFC42E : RenderArgs.borderColor();
            BackplaneRenderer.render(graphics, left, top, width, height, dividers, border);
        } else {
            this.renderDividers(graphics, font);
        }

        line.draw(graphics, font, left, top, RenderArgs.DROP_SHADOW.get());
    }

    final void renderLine(@NotNull GuiGraphics graphics, @NotNull Font font) {
        TextLine line = this.line();
        if (line == null) return;

        line.draw(graphics, font, this.left.get(), this.top.get(), RenderArgs.DROP_SHADOW.get());
    }

    final void renderDividers(@NotNull GuiGraphics graphics, @NotNull Font font) {
        TextLine line = this.line();
        if (line == null) return;

        int left = this.left.get();
        int top = this.top.get();
        int color = RenderArgs.DRAW_BACKPLANE.get()
                ? RenderArgs.borderColor()
                : RenderArgs.palette().separator() | 0xFF000000;

        for (int offset : line.dividers()) {
            int x = left + offset;
            graphics.fill(x, top + 1, x + 1, top + font.lineHeight - 1, color);
        }
    }

    private int width(@NotNull TextLine own) {
        return RenderArgs.EQUAL_WIDTH.get() ? Math.max(own.width(), CardRegistry.sharedWidth()) : own.width();
    }

    final @Nullable TextLine line() {
        if (!this.enabled.get()) return null;

        D data = this.data;
        if (data == null) return null;

        int version = ConfigVersion.get();
        if (this.builtVersion != version) {
            this.line = this.build(data);
            this.builtVersion = version;
        }

        return this.line;
    }

    private @Nullable TextLine build(@NotNull D data) {
        TextLine.Builder builder = TextLine.builder(Minecraft.getInstance().font);
        boolean first = true;

        for (Field<D> field : this.orderedFields()) {
            FieldConfig config = this.configs.get(field.id());
            if (!config.enabled()) continue;

            if (!first) builder.divider();
            first = false;

            field.append(builder, data, config);
        }

        return first ? null : builder.build();
    }

    private @NotNull List<Field<D>> orderedFields() {
        Set<String> ids = new LinkedHashSet<>();
        for (String id : this.order.get()) {
            if (this.fields.containsKey(id)) ids.add(id);
        }
        ids.addAll(this.fields.keySet());

        List<Field<D>> ordered = new ArrayList<>(ids.size());
        for (String id : ids) ordered.add(this.fields.get(id));
        return ordered;
    }

    public final ForgeConfigSpec.@NotNull BooleanValue enabledValue() {
        return this.enabled;
    }

    public final ForgeConfigSpec.@NotNull IntValue leftValue() {
        return this.left;
    }

    public final ForgeConfigSpec.@NotNull IntValue topValue() {
        return this.top;
    }

    public final @NotNull @Unmodifiable List<Field<D>> allFields() {
        return List.copyOf(this.fields.values());
    }

    public final @NotNull FieldConfig fieldConfig(@NotNull String fieldId) {
        FieldConfig config = this.configs.get(fieldId);
        if (config == null) throw new IllegalArgumentException("Unknown field '" + fieldId + "' in card " + this.id);
        return config;
    }

    public final int lineWidth() {
        TextLine own = this.line();
        return own == null ? 0 : this.width(own);
    }
}
