package net.azisaba.autoreboot.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.azisaba.autoreboot.common.CommonLogger;
import net.azisaba.autoreboot.common.network.NamedPacket;
import net.azisaba.autoreboot.common.network.Packet;
import net.azisaba.autoreboot.common.network.PacketListener;
import net.azisaba.autoreboot.common.network.Protocol;
import net.azisaba.autoreboot.common.network.RedisKeys;
import net.azisaba.autoreboot.common.network.Side;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PubSubHandler implements Closeable {
    public static final byte @NotNull [] CHANNEL = RedisKeys.PUBSUB.getBytes(StandardCharsets.UTF_8);
    private final Map<String, List<Consumer<ByteBuf>>> handlers = new ConcurrentHashMap<>();
    private final ArrayDeque<Consumer<byte[]>> pingPongQueue = new ArrayDeque<>();
    private final PubSubListener listener = new PubSubListener();
    private final Side side;
    private final CommonLogger logger;
    private final JedisPool jedisPool;
    private final PacketListener packetListener;
    private final ScheduledExecutorService pingThread = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AutoReboot PubSub Ping Thread");
        t.setDaemon(true);
        return t;
    });
    private final ExecutorService subscriberThread = Executors.newFixedThreadPool(1, r -> {
        Thread t = new Thread(r, "AutoReboot PubSub Subscriber Thread");
        t.setDaemon(true);
        return t;
    });

    public PubSubHandler(@NotNull Side side, @NotNull CommonLogger logger, @NotNull JedisPool jedisPool, @NotNull PacketListener packetListener) {
        this.side = side;
        this.logger = logger;
        this.jedisPool = jedisPool;
        this.packetListener = packetListener;
        register();
    }

    private void register() {
        AtomicReference<Runnable> task = new AtomicReference<>();
        task.set(() -> {
            try {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.subscribe(listener, CHANNEL);
                } catch (JedisConnectionException e) {
                    e.printStackTrace();
                }
            } finally {
                subscriberThread.submit(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    task.get().run();
                });
            }
        });
        subscriberThread.submit(task.get());
        pingThread.scheduleAtFixedRate(() -> {
            try {
                long latency = ping();
                if (latency < 0) {
                    logger.warn("Disconnected from Redis server. Reconnecting...");
                    listener.unsubscribe();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    @Contract(pure = true)
    @NotNull
    public List<Consumer<ByteBuf>> getHandlerList(@NotNull String key) {
        return handlers.getOrDefault(key, Collections.emptyList());
    }

    private List<Consumer<ByteBuf>> getOrCreateHandlerList(@NotNull String key) {
        return handlers.computeIfAbsent(key, k -> new ArrayList<>());
    }

    public void subscribe(@NotNull String key, @NotNull Consumer<ByteBuf> handler) {
        if (Protocol.getByName(key) != null) {
            throw new IllegalArgumentException("Cannot subscribe to a defined packet");
        }
        getOrCreateHandlerList(key).add(handler);
    }

    public void unsubscribe(@NotNull String key, @NotNull Consumer<ByteBuf> handler) {
        getOrCreateHandlerList(key).remove(handler);
    }

    public void unsubscribeAll(@NotNull String key) {
        handlers.remove(key);
    }

    public void publish(@NotNull String key, @NotNull ByteBuf data) {
        ByteBuf buf = Unpooled.buffer();
        Packet.writeString(buf, key);
        buf.writeBytes(data);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(CHANNEL, ByteBufUtil.toByteArray(buf));
        }
    }

    public void publish(@NotNull String key, @NotNull Packet<?> packet) {
        ByteBuf buf = Unpooled.buffer();
        Packet.writeString(buf, key);
        packet.encode(buf);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(CHANNEL, ByteBufUtil.toByteArray(buf));
        }
    }

    private void processRawMessage(byte[] message) {
        ByteBuf buf = Unpooled.wrappedBuffer(message);
        String key = Packet.readString(buf);
        try {
            NamedPacket<?, ?> namedPacket = Protocol.getByName(key);
            if (namedPacket == null) {
                processUnknown(key, buf.slice());
            } else {
                handlePacket(namedPacket.create(), buf.slice());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (buf.refCnt() > 0) {
                buf.release();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends PacketListener> void handlePacket(@NotNull Packet<T> packet, @NotNull ByteBuf buf) {
        if (side == Side.PROXY && packet.getClass().getSimpleName().startsWith("Backend")) {
            return;
        }
        if (side == Side.BACKEND && packet.getClass().getSimpleName().startsWith("Proxy")) {
            return;
        }
        packet.decode(buf);
        packet.handle((T) packetListener);
    }

    private void processUnknown(@NotNull String key, @NotNull ByteBuf data) {
        getOrCreateHandlerList(key).forEach(handler -> handler.accept(data));
    }

    private long ping() {
        if (!listener.isSubscribed()) {
            return -2;
        }

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        long start = System.currentTimeMillis();

        pingPongQueue.add(arg -> thread.interrupt());
        try {
            listener.ping();
        } catch (JedisConnectionException e) {
            return -1;
        }

        try {
            thread.join(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return System.currentTimeMillis() - start;
    }

    @Override
    public void close() {
        subscriberThread.shutdownNow();
        pingThread.shutdownNow();
    }

    private class PubSubListener extends BinaryJedisPubSub {
        @Override
        public void onMessage(byte[] channel, byte[] message) {
            if (Arrays.equals(CHANNEL, channel)) {
                try {
                    PubSubHandler.this.processRawMessage(message);
                } catch (Exception e) {
                    logger.warn("Error while processing message");
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onPong(byte[] pattern) {
            Consumer<byte[]> consumer = pingPongQueue.poll();
            if (consumer != null) {
                try {
                    consumer.accept(pattern);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
