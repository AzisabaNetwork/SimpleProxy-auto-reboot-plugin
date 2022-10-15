package net.azisaba.autoreboot.velocity.util;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class PlayerUtil {
    public static void announce(@NotNull ProxyServer proxy, @NotNull String ip, @NotNull Component component, boolean notifyOthers) {
        try {
            ClassLoader cl = proxy.getPluginManager()
                    .getPlugin("bungee-proxy-announcer")
                    .flatMap(PluginContainer::getInstance)
                    .orElseThrow(RuntimeException::new)
                    .getClass()
                    .getClassLoader();
            Class<?> clazz = cl.loadClass("net.azisaba.bungeeproxyannouncer.util.PlayerUtil");
            Method m = clazz.getMethod("announce", ProxyServer.class, String.class, Component.class, boolean.class);
            m.invoke(null, proxy, ip, component, notifyOthers);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
