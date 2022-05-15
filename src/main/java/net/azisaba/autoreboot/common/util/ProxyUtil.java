package net.azisaba.autoreboot.common.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

public class ProxyUtil {
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> T newProxyWithInstance(@NotNull Class<T> interfaceClass, @NotNull Object instance) {
        Objects.requireNonNull(interfaceClass, "interfaceClass cannot be null");
        Objects.requireNonNull(instance, "instance cannot be null");
        Class<?> instClass = instance.getClass();
        return (T) Proxy.newProxyInstance(ProxyUtil.class.getClassLoader(), new Class[] { interfaceClass }, (proxy, method, args) -> {
            Method m = ReflectionUtil.findMethod(instClass, method);
            if (m == null) throw new RuntimeException(instClass.getTypeName() + " does not implements " + method.toGenericString());
            return m.invoke(instance, args);
        });
    }
}
