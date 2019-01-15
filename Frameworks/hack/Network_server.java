public abstract class AbstractRpcRemotingServer extends AbstractRpcRemoting implements RemotingServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRpcRemotingServer.class);
    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup eventLoopGroupWorker;
    private final EventLoopGroup eventLoopGroupBoss;
    private final NettyServerConfig nettyServerConfig;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private int listenPort;

    public void setListenPort(int listenPort) {
    	
    	if(listenPort <= 0) {
    		throw new IllegalArgumentException("listen port: " + listenPort +" is invalid!");
    	}
    	
        this.listenPort = listenPort;
    }

    public int getListenPort() {
        return listenPort;
    }

    /**
     * Instantiates a new Rpc remoting server.
     *
     * @param nettyServerConfig the netty server config
     */
    public AbstractRpcRemotingServer(final NettyServerConfig nettyServerConfig) {
        this(nettyServerConfig, null);
    }

    /**
     * Instantiates a new Rpc remoting server.
     *
     * @param nettyServerConfig the netty server config
     * @param messageExecutor   the message executor
     * @param handlers          the handlers
     */
    public AbstractRpcRemotingServer(final NettyServerConfig nettyServerConfig,
                                     final ThreadPoolExecutor messageExecutor, final ChannelHandler... handlers) {
        super(messageExecutor);
        this.serverBootstrap = new ServerBootstrap();
        this.nettyServerConfig = nettyServerConfig;
        this.eventLoopGroupBoss = new NioEventLoopGroup(nettyServerConfig.getBossThreadSize(),
            new NamedThreadFactory(nettyServerConfig.getBossThreadPrefix(), nettyServerConfig.getBossThreadSize()));
        if (NettyServerConfig.enableEpoll()) {
            this.eventLoopGroupWorker = new EpollEventLoopGroup(nettyServerConfig.getServerWorkerThreads(),
                new NamedThreadFactory(nettyServerConfig.getWorkerThreadPrefix(),
                    nettyServerConfig.getServerWorkerThreads()));
        } else {
            this.eventLoopGroupWorker = new NioEventLoopGroup(nettyServerConfig.getServerWorkerThreads(),
                new NamedThreadFactory(nettyServerConfig.getWorkerThreadPrefix(),
                    nettyServerConfig.getServerWorkerThreads()));
        }
        if (null != handlers) {
            channelHandlers = handlers;
        }
        // init listenPort in constructor so that getListenPort() will always get the exact port
        setListenPort(nettyServerConfig.getDefaultListenPort());
    }

    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
            nettyServerConfig.getServerWorkerThreads(),
            new NamedThreadFactory(nettyServerConfig.getExecutorThreadPrefix(),
                nettyServerConfig.getServerWorkerThreads()));

        this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupWorker)
            .channel(nettyServerConfig.SERVER_CHANNEL_CLAZZ)
            .option(ChannelOption.SO_BACKLOG, nettyServerConfig.getSoBackLogSize())
            .option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_SNDBUF, nettyServerConfig.getServerSocketSendBufSize())
            .childOption(ChannelOption.SO_RCVBUF, nettyServerConfig.getServerSocketResvBufSize())
            .childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK,
                nettyServerConfig.getWriteBufferHighWaterMark())
            .childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, nettyServerConfig.getWriteBufferLowWaterMark())
            .localAddress(new InetSocketAddress(listenPort))
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new IdleStateHandler(nettyServerConfig.getChannelMaxReadIdleSeconds(), 0, 0))
                        .addLast(new MessageCodecHandler());
                    if (null != channelHandlers) {
                        addChannelPipelineLast(ch, channelHandlers);
                    }

                }
            });

        if (nettyServerConfig.isEnableServerPooledByteBufAllocator()) {
            this.serverBootstrap.childOption(ChannelOption.ALLOCATOR, NettyServerConfig.DIRECT_BYTE_BUF_ALLOCATOR);
        }

        try {
            LOGGER.info("Server starting ... ");
            ChannelFuture future = this.serverBootstrap.bind(listenPort).sync();
            LOGGER.info("Server started ... ");
            future.channel().closeFuture().sync();
        } catch (InterruptedException exx) {
            throw new RuntimeException(exx);
        }

    }

    @Override
    public void shutdown() {
        try {
            this.eventLoopGroupBoss.shutdownGracefully();
            this.eventLoopGroupWorker.shutdownGracefully();
            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception exx) {
            LOGGER.error(exx.getMessage());
        }
    }

    @Override
    public void destroyChannel(String serverAddress, Channel channel) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("will destroy channel:" + channel + ",address:" + serverAddress);
        }
        channel.disconnect();
        channel.close();
    }

}
