package me.kall.narutooverlay.card;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.kall.narutooverlay.backplane.AbstractBackplane;
import me.kall.narutooverlay.backplane.BackplaneRenderer;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.text.TextLine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CardRegistry {
    private static final List<Card<?>> CARDS = new ObjectArrayList<>(3);
    private static final int GROUP_ROW_GAP = 4;

    private static @Nullable Card<?> selected;

    private CardRegistry() {}

    static void register(@NotNull Card<?> card) {
        CARDS.add(card);
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView List<Card<?>> all() {
        return Collections.unmodifiableList(CARDS);
    }

    public static void render(@NotNull RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        Set<String> drawnGroups = new HashSet<>();

        for (Card<?> card : CARDS) {
            if (!card.active()) continue;

            String group = card.group();
            if (!group.isEmpty()) {
                if (!drawnGroups.add(group)) continue;

                List<Card<?>> members = activeMembers(group);
                if (members.size() > 1) {
                    renderGroup(graphics, members);
                    continue;
                }
            }

            card.render(graphics);
        }
    }

    private static @NotNull List<Card<?>> activeMembers(@NotNull String group) {
        List<Card<?>> members = new ArrayList<>(2);
        for (Card<?> card : CARDS) {
            if (card.active() && group.equals(card.group())) members.add(card);
        }
        return members;
    }

    public static @NotNull List<Card<?>> groupMembers(@NotNull Card<?> card) {
        String group = card.group();
        if (group.isEmpty()) return List.of(card);

        List<Card<?>> members = new ArrayList<>(2);
        for (Card<?> candidate : CARDS) {
            if (group.equals(candidate.group())) members.add(candidate);
        }
        return members;
    }

    private static void renderGroup(@NotNull GuiGraphics graphics, @NotNull List<Card<?>> members) {
        members.sort(Comparator.comparingInt((Card<?> card) -> card.top())
                .thenComparingInt(card -> card.left()));

        Font font = Minecraft.getInstance().font;
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;

        for (Card<?> card : members) {
            left = Math.min(left, card.left());
            top = Math.min(top, card.top());
            right = Math.max(right, card.left() + card.lineWidth());
            bottom = Math.max(bottom, card.top() + font.lineHeight);
        }

        int[] separators = new int[Math.max(0, members.size() - 1)];
        for (int i = 1; i < members.size(); i++) {
            separators[i - 1] = members.get(i).top() - 2;
        }

        if (RenderArgs.DRAW_BACKPLANE.get()) {
            int border = members.stream().anyMatch(CardRegistry::isSelected)
                    ? 0xFFFFC42E
                    : RenderArgs.borderColor();
            BackplaneRenderer.renderGroup(graphics, left, top, right - left, bottom - top, separators, border);
        } else {
            int color = RenderArgs.palette().separator() | 0xFF000000;
            for (int y : separators) {
                if (y <= top || y >= bottom) continue;
                graphics.fill(left, y, right, y + 1, color);
            }
        }

        for (Card<?> card : members) card.renderDividers(graphics, font);
        for (Card<?> card : members) card.renderLine(graphics, font);
    }

    public static @Nullable Card<?> pick(double mouseX, double mouseY) {
        Set<String> checkedGroups = new HashSet<>();

        for (int i = CARDS.size() - 1; i >= 0; i--) {
            Card<?> card = CARDS.get(i);
            if (!card.active()) continue;

            String group = card.group();
            if (!group.isEmpty()) {
                if (!checkedGroups.add(group)) continue;

                List<Card<?>> members = activeMembers(group);
                if (members.size() > 1 && groupContains(members, mouseX, mouseY)) {
                    for (Card<?> member : members) {
                        if (member.contains(mouseX, mouseY)) return member;
                    }
                    return members.get(0);
                }

                continue;
            }

            if (card.contains(mouseX, mouseY)) return card;
        }

        return null;
    }

    private static boolean groupContains(@NotNull List<Card<?>> members, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;

        for (Card<?> card : members) {
            left = Math.min(left, card.left());
            top = Math.min(top, card.top());
            right = Math.max(right, card.left() + card.lineWidth());
            bottom = Math.max(bottom, card.top() + font.lineHeight);
        }

        int accentExtra = RenderArgs.palette().hasAccent() ? AbstractBackplane.ACCENT_WIDTH + 1 : 0;
        int x0 = left - AbstractBackplane.PADDING_X - accentExtra;
        int x1 = right + AbstractBackplane.PADDING_RIGHT;
        int y0 = top - AbstractBackplane.PADDING_TOP;
        int y1 = bottom + AbstractBackplane.PADDING_BOTTOM;

        return mouseX >= x0 && mouseX <= x1 && mouseY >= y0 && mouseY <= y1;
    }

    public static boolean merge(@NotNull Card<?> first, @NotNull Card<?> second) {
        if (first == second || !first.active() || !second.active()) return false;

        String firstGroup = first.group();
        String secondGroup = second.group();
        if (!firstGroup.isEmpty() && firstGroup.equals(secondGroup)) return false;

        Font font = Minecraft.getInstance().font;

        if (!firstGroup.isEmpty() && !secondGroup.isEmpty()) {
            List<Card<?>> source = activeMembers(firstGroup);
            List<Card<?>> target = activeMembers(secondGroup);
            if (source.isEmpty() || target.isEmpty()) return false;

            int left = Integer.MAX_VALUE;
            int sourceBottom = Integer.MIN_VALUE;
            for (Card<?> member : source) {
                left = Math.min(left, member.left());
                sourceBottom = Math.max(sourceBottom, member.top() + font.lineHeight);
            }

            int targetTop = Integer.MAX_VALUE;
            for (Card<?> member : target) targetTop = Math.min(targetTop, member.top());

            for (Card<?> member : target) {
                member.moveTo(left, sourceBottom + GROUP_ROW_GAP + (member.top() - targetTop));
                member.setGroup(firstGroup);
            }

            return true;
        }

        if (firstGroup.isEmpty() && secondGroup.isEmpty()) {
            int left = Math.min(first.left(), second.left());
            int top = Math.min(first.top(), second.top());

            first.moveTo(left, top);
            second.moveTo(left, top + font.lineHeight + GROUP_ROW_GAP);

            String group = "g:" + first.id() + "+" + second.id();
            first.setGroup(group);
            second.setGroup(group);
            return true;
        }

        if (!firstGroup.isEmpty() && secondGroup.isEmpty()) {
            List<Card<?>> source = activeMembers(firstGroup);
            if (source.isEmpty()) return false;

            int sourceTop = Integer.MAX_VALUE;
            for (Card<?> member : source) sourceTop = Math.min(sourceTop, member.top());

            int left = second.left();
            int top = second.top();
            int offset = top + font.lineHeight + GROUP_ROW_GAP - sourceTop;

            for (Card<?> member : source) member.moveTo(left, member.top() + offset);

            second.setGroup(firstGroup);
            return true;
        }

        String group = firstGroup.isEmpty() ? secondGroup : firstGroup;
        Card<?> added = firstGroup.isEmpty() ? first : second;
        List<Card<?>> existing = activeMembers(group);
        if (existing.isEmpty()) return false;

        int left = Integer.MAX_VALUE;
        int bottom = Integer.MIN_VALUE;
        for (Card<?> member : existing) {
            left = Math.min(left, member.left());
            bottom = Math.max(bottom, member.top() + font.lineHeight);
        }

        added.moveTo(left, bottom + GROUP_ROW_GAP);
        added.setGroup(group);
        return true;
    }

    public static boolean split(@NotNull Card<?> card) {
        String group = card.group();
        if (group.isEmpty()) return false;

        List<Card<?>> members = groupMembers(card);
        members.sort(Comparator.comparingInt((Card<?> member) -> member.top())
                .thenComparingInt(member -> member.left()));

        if (members.isEmpty()) return false;

        Font font = Minecraft.getInstance().font;
        int top = members.get(0).top();

        for (Card<?> member : members) {
            member.setGroup("");
            member.moveTo(member.left(), top);
            top += font.lineHeight + GROUP_ROW_GAP + 2;
        }

        return members.size() > 1;
    }

    public static void select(@Nullable Card<?> card) {
        selected = card;
    }

    public static @Nullable Card<?> selected() {
        return selected;
    }

    public static boolean isSelected(@Nullable Card<?> card) {
        return card != null && card == selected;
    }

    static int sharedWidth() {
        int width = 0;
        for (Card<?> card : CARDS) {
            TextLine line = card.line();
            if (line != null) width = Math.max(width, line.width());
        }
        return width;
    }

    public static void resetPositions() {
        selected = null;
        for (Card<?> card : CARDS) {
            card.setGroup("");
            card.resetPosition();
        }
    }

    public static void resetAll() {
        selected = null;
        for (Card<?> card : CARDS) card.resetAll();
    }
}