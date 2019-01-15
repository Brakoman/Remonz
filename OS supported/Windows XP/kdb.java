import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.fescar.common.Constants;
import com.alibaba.fescar.core.protocol.*;
import com.alibaba.fescar.core.protocol.RegisterRMRequest;
import com.alibaba.fescar.core.protocol.RegisterTMRequest;
import com.alibaba.fescar.core.rpc.netty.NettyPoolKey.TransactionRole;

import io.netty.channel.Channel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type channel manager.
 *
 * @Author: jimin.jm @alibaba-inc.com
 * @Project: fescar-all
 * @DateTime: 2018 /12/07 10:50
 * @FileName: ChannelManager
 * @Description:
 */
public class ChannelManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelManager.class);
    private static final ConcurrentMap<Channel, RpcContext> IDENTIFIED_CHANNELS
        = new ConcurrentHashMap<Channel, RpcContext>();

    /**
     * dbkey+appname+ip port context
     */
    private static final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<Integer,
        RpcContext>>>>
        RM_CHANNELS
        = new ConcurrentHashMap<String, ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<Integer,
        RpcContext>>>>();

    /**
     * ip+appname,port
     */
    private static final ConcurrentMap<String, ConcurrentMap<Integer, RpcContext>> TM_CHANNELS
        = new ConcurrentHashMap<String, ConcurrentMap<Integer, RpcContext>>();

    private static final ConcurrentMap<String, String> DB_GROUP_MAPPING = new ConcurrentHashMap<String, String>();

    /**
     * Is registered boolean.
     *
     * @param channel the channel
     * @return the boolean
     */
    public static boolean isRegistered(Channel channel) {
        return IDENTIFIED_CHANNELS.containsKey(channel);
    }

    /**
     * Gets get role from channel.
     *
     * @param channel the channel
     * @return the get role from channel
     */
    public static TransactionRole getRoleFromChannel(Channel channel) {
        if (IDENTIFIED_CHANNELS.containsKey(channel)) {
            return IDENTIFIED_CHANNELS.get(channel).getClientRole();
        }
        return null;
    }
