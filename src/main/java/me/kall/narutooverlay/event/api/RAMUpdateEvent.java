package me.kall.narutooverlay.event.api;

import net.minecraftforge.eventbus.api.Event;

public final class RAMUpdateEvent extends Event {
    public final long max, total, used;

    public RAMUpdateEvent(long max, long total, long used) {
        this.max = max;
        this.total = total;
        this.used = used;
    }
}
