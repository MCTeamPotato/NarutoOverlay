package me.kall.narutooverlay.event.api;

import net.minecraftforge.eventbus.api.Event;

public final class FpsUpdateEvent extends Event {
    public final double inst, min, avg, low;

    public FpsUpdateEvent(double inst, double min, double avg, double low) {
        this.inst = inst;
        this.min = min;
        this.avg = avg;
        this.low = low;
    }
}
