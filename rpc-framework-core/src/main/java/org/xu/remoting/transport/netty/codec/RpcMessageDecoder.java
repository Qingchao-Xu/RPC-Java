package org.xu.remoting.transport.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.xu.enums.SerializationTypeEnum;
import org.xu.extension.ExtensionLoader;
import org.xu.remoting.constants.RpcConstants;
import org.xu.remoting.dto.RpcMessage;
import org.xu.remoting.dto.RpcRequest;
import org.xu.remoting.dto.RpcResponse;
import org.xu.serialize.Serializer;

import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * 自定义协议解码器
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 * LengthFieldBasedFrameDecoder 是一个基于长度的解码器 , 用来解决TCP粘包和拆包的问题.
 *
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder{

    public RpcMessageDecoder() {
        /*
        lengthFieldOffset: 魔法数占用 4B, 版本号占用 1B, 然后就是消息总长了. 所以要跳过 5 B
        lengthFieldLength: 消息总长占用 4B.
        lengthAdjustment: 消息总长包括所有数据，有效数据之前需要读取9个字节（魔法数+版本号+消息总长），因此剩余消息长度为（消息总长-9）。所以值是-9
        initialBytesToStrip: 我们需要手动检查魔法数和版本，所以不要跳过任何字节。所以值为0
         */
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * @param maxFrameLength 最大帧长。决定可以接收的最大数据长度。如果超过，数据将被丢弃。
     * @param lengthFieldOffset 字段偏移长度。跳过指定字节长度的字段。
     * @param lengthFieldLength 长度字段占用的字节数。
     * @param lengthAdjustment 要添加到长度字段值的补偿值（因为长度字段存储的是整个消息的长度，所以要加整个补偿值）
     * @param initialBytesToStrip 要跳过的字节数。
     *                            如果需要接收所有标头+正文数据，则此值为0
     *                            如果只想接收正文数据，那么你需要跳过标头消耗的字节数。
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in); // 父类的解码方法，读取数据做一些判断并返回 ByteBuf
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) { // 可读长度要大于 16B
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release(); // 减少引用计数，释放ByteBuf
                }
            }
        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf in) {
        // 检查魔法数和版本号
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        // 创建RpcMessage对象
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .messageType(messageType)
                .codec(codecType)
                .compress(compressType)
                .requestId(requestId).build();
        // 心跳检测数据, 后续补充
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            // 解压缩,后续补充

            // 反序列化
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}]", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(tmpValue);
            } else {
                RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;
    }

    private void checkMagicNumber(ByteBuf in) {
        // 读取前4位魔法数，并进行比较
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }

    private void checkVersion(ByteBuf in) {
        // 读取版本号并进行比较
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible: " + version);
        }
    }

}
