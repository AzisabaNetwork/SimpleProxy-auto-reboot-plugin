package net.azisaba.autoreboot.common.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.autoreboot.common.Protocol;
import net.azisaba.autoreboot.common.Util;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public abstract class AbstractMessageHandler extends ChannelInboundHandlerAdapter {
    private static final boolean DEBUG = true;
    private static final int THRESHOLD = 3; // threshold before giving up
    protected final String token;
    private int count = 0;

    public AbstractMessageHandler(@NotNull String token) {
        Util.validateToken(token);
        this.token = token;
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        try {
            if (++count >= THRESHOLD) {
                ctx.channel().pipeline().remove(this);
            }
            if (msg instanceof ByteBuf && process(ctx.channel(), (ByteBuf) msg)) {
                if (count < THRESHOLD) {
                    ctx.channel().pipeline().remove(this);
                }
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.channelRead(ctx, msg);
    }

    private boolean process(Channel ch, ByteBuf buf) {
        int idx = buf.readerIndex();
        try {
            if (!Protocol.verifyMagic(buf)) {
                if (DEBUG) {
                    buf.readerIndex(idx);
                    String s = buf.readCharSequence(Protocol.MAGIC.length(), StandardCharsets.UTF_8).toString();
                    buf.readerIndex(idx); // set reader index before evaluating buf
                    throw new IllegalArgumentException("Magic does not match (received " + Util.filterAscii(s) + ", buf: " + buf + ")");
                } else {
                    throw new IllegalArgumentException("Magic does not match");
                }
            }
            String readToken = buf.readCharSequence(token.length(), StandardCharsets.UTF_8).toString();
            if (!token.equals(readToken)) {
                if (DEBUG) {
                    throw new IllegalArgumentException("Token does not match (received " + Util.filterAscii(readToken) + ")");
                } else {
                    throw new IllegalArgumentException("Token does not match");
                }
            }
            String packetId = Protocol.readString(buf);
            processPacket(ch, packetId, buf);
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        } finally {
            buf.readerIndex(idx);
        }
        return false;
    }

    public abstract void processPacket(@NotNull Channel ch, @NotNull String packetId, @NotNull ByteBuf buf) throws Exception;
}
