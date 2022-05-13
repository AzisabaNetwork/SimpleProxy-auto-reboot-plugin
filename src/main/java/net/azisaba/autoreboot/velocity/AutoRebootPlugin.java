package net.azisaba.autoreboot.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import net.azisaba.autoreboot.common.Util;
import net.blueberrymc.nativeutil.NativeUtil;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Plugin(id = "autoreboot", name = "AutoReboot", version = "1.0.0")
public class AutoRebootPlugin {
    private final Logger logger;
    private String token = null;

    @Inject
    public AutoRebootPlugin(Logger logger, @DataDirectory Path dataDirectory) throws Exception {
        this.logger = logger;
        try {
            Map<Object, Object> map = new Yaml().load(Files.newInputStream(dataDirectory.resolve("config.yml")));
            token = Util.mapIfNotNull(map.get("token"), String::valueOf);
        } catch (FileNotFoundException ignored) {
        }
        Util.validateToken(token);
        transform();
    }

    private void transform() throws Exception {
        var classpath = AutoRebootPlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        NativeUtil.registerClassLoadHook((classLoader, s, aClass, protectionDomain, bytes) -> {
            if (!s.equals("com/velocitypowered/proxy/connection/MinecraftConnection")) {
                return null;
            }
            try {
                var cp = new ClassPool(true);
                cp.appendClassPath(classpath);
                cp.insertClassPath(new ByteArrayClassPath(s.replace('/', '.'), bytes));
                var cc = cp.get(s.replace("/", "."));
                var methodChannelActive = cc.getMethod("channelActive", "(Lio/netty/channel/ChannelHandlerContext;)V");
                String escapedToken = token.replace("\\", "\\\\").replace("\"", "\\\"");
                methodChannelActive.insertBefore("$1.channel().pipeline().addFirst(\"autoreboot-message-handler\", new net.azisaba.autoreboot.velocity.connection.BackendMessageHandler(\"" + escapedToken + "\"));");
                byte[] bc = cc.toBytecode();
                NativeUtil.appendToSystemClassLoaderSearch(classpath);
                return bc;
            } catch (Exception e) {
                logger.error("Failed to transform MinecraftConnection", e);
            }
            return null;
        });
    }
}
