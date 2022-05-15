package net.azisaba.autoreboot.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import net.azisaba.autoreboot.common.CommonLogger;
import net.azisaba.autoreboot.common.Util;
import net.azisaba.autoreboot.common.network.Side;
import net.azisaba.autoreboot.common.util.JedisBox;
import net.azisaba.autoreboot.velocity.network.BackendPacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Plugin(id = "autoreboot", name = "AutoReboot", version = "1.0.0")
public class AutoRebootPlugin {
    private final BackendPacketListenerImpl backendPacketListener = new BackendPacketListenerImpl(this);
    private final Logger logger;
    private final JedisBox jedisBox;

    @SuppressWarnings("unchecked")
    @Inject
    public AutoRebootPlugin(Logger logger, @DataDirectory Path dataDirectory) throws Exception {
        this.logger = logger;
        try {
            Map<Object, Object> map = new Yaml().load(Files.newInputStream(dataDirectory.resolve("config.yml")));
            String redisHostname = Util.mapIfNotNull(((Map<Object, Object>) map.get("redis")).get("hostname"), String::valueOf);
            int redisPort = (int) ((Map<Object, Object>) map.get("redis")).get("port");
            String redisUsername = Util.mapIfNotNull(((Map<Object, Object>) map.get("redis")).get("username"), String::valueOf);
            String redisPassword = Util.mapIfNotNull(((Map<Object, Object>) map.get("redis")).get("password"), String::valueOf);
            jedisBox = new JedisBox(
                    Side.BACKEND,
                    CommonLogger.create(getLogger()),
                    getBackendPacketListener(),
                    redisHostname,
                    redisPort,
                    redisUsername,
                    redisPassword);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent e) {
        jedisBox.close();
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }

    @NotNull
    public JedisBox getJedisBox() {
        return jedisBox;
    }

    @NotNull
    public BackendPacketListenerImpl getBackendPacketListener() {
        return backendPacketListener;
    }
}
