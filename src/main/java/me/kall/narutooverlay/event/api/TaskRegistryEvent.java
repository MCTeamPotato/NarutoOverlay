package me.kall.narutooverlay.event.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

public final class TaskRegistryEvent extends Event implements IModBusEvent {
    public static final TaskRegistryEvent INSTANCE = new TaskRegistryEvent();

    private static final List<Task> TASKS = new ObjectArrayList<>();

    public void register(IntSupplier interval, BooleanSupplier active, Runnable task) {
        TASKS.add(new Task(task, interval, active));
    }

    public static void update(TickEvent.@NotNull ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (Task task : TASKS) {
            if (task.check()) task.run();
        }
    }

    static final class Task implements Runnable {
        final Runnable task;
        final IntSupplier interval;
        final BooleanSupplier active;
        int cooldown;

        Task(Runnable task, IntSupplier interval, BooleanSupplier active) {
            this.task = task;
            this.interval = interval;
            this.active = active;
        }

        boolean check() {
            if (!this.active.getAsBoolean()) {
                this.cooldown = Integer.MAX_VALUE - 1;
                return false;
            }

            return ++this.cooldown >= this.interval.getAsInt();
        }

        @Override
        public void run() {
            this.cooldown = 0;
            this.task.run();
        }
    }
}
