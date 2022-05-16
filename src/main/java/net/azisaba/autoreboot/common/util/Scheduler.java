package net.azisaba.autoreboot.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private static final Timer TIMER = new Timer(true);

    public static void schedule(long delayInMillis, @NotNull Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, delayInMillis);
    }
}
