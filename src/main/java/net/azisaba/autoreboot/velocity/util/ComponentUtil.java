package net.azisaba.autoreboot.velocity.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class ComponentUtil {
    public static final GsonComponentSerializer GSON_COMPONENT_SERIALIZER = GsonComponentSerializer.gson();
    public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .extractUrls()
            .build();

    @NotNull
    public static Component fromString(@NotNull String message) {
        Component component = null;
        if (message.startsWith("{") || message.startsWith("[")) {
            component = GSON_COMPONENT_SERIALIZER.deserializeOrNull(message);
        }
        if (component == null) {
            component = LEGACY_COMPONENT_SERIALIZER.deserialize(message);
        }
        return component;
    }
}
