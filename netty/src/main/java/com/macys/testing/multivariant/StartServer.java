package com.macys.testing.multivariant;

import java.net.SocketAddress;

import javax.inject.Inject;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.macys.testing.multivariant.handlers.JsonEncoder;
import com.macys.testing.multivariant.handlers.WeightedRandomNumberGenerator;

public class StartServer extends AbstractIdleService {
	private final ServerBootstrap bootstrap;
	private final SocketAddress address;
	private final ChannelFactory channelFactory;
	private static final HttpResponseEncoder httpResponseEncoder = new HttpResponseEncoder();
	private static final JsonEncoder jsonEncoder = new JsonEncoder();
	private static final ExecutionHandler executionHandler = new ExecutionHandler(
			new OrderedMemoryAwareThreadPoolExecutor(10, 1048576, 1048576));

	@Inject
	public StartServer(ChannelFactory channelFactory, SocketAddress address,
			RedisAsyncConnection<String, String> connection) {
		this.channelFactory = channelFactory;
		bootstrap = new ServerBootstrap(channelFactory);
		this.address = address;
		final WeightedRandomNumberGenerator weightedRandomGenerator = new WeightedRandomNumberGenerator(
				connection);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("encoder", httpResponseEncoder);
				pipeline.addLast("executionHandler", executionHandler);
				pipeline.addLast("weightedRandomGenerator",
						weightedRandomGenerator);
				pipeline.addLast("JsonConverter", jsonEncoder);
				return pipeline;
			}
		});
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new NettyModule());
		final StartServer server = injector.getInstance(StartServer.class);
		server.startAndWait();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				server.stopAndWait();
			}
		});

	}

	@Override
	protected void shutDown() throws Exception {
		channelFactory.releaseExternalResources();
	}

	@Override
	protected void startUp() throws Exception {
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.bind(address);
	}

}
