package net.azisaba.autoreboot.common;

import net.azisaba.autoreboot.common.util.ProxyUtil;
import org.jetbrains.annotations.NotNull;

public interface CommonLogger {
    // these methods should be usable, just remove the comment to use it.

    //void info(String msg);

    void warn(String msg);

    //void error(String msg);

    @NotNull
    static CommonLogger create(@NotNull Object instance) {
        return ProxyUtil.newProxyWithInstance(CommonLogger.class, instance);
    }
}
