package com.macys.testing.multivariant;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.macys.testing.multivariant.handlers.JsonEncoder;

public class StartServer extends AbstractIdleService {
	private final ServerBootstrap bootstrap;
	private final SocketAddress address;
	private final ChannelFactory channelFactory;
	private static final HttpResponseEncoder httpResponseEncoder = new HttpResponseEncoder();
	private static final JsonEncoder jsonEncoder = new JsonEncoder();

	@Inject
	public StartServer(ChannelFactory channelFactory, SocketAddress address,
			RedisAsyncConnection<String, String> connection, ExecutionHandler executionHandler) {
		this.channelFactory = channelFactory;
		bootstrap = new ServerBootstrap(channelFactory);
		this.address = address;
		bootstrap.setPipelineFactory(new HttpPipelineFactory(executionHandler, connection));
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

	public static void main0(String[] args) throws Exception {
		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		SocketAddress sockAddress = new InetSocketAddress(8080);

		RedisClient client = new RedisClient("127.0.0.1");
		final RedisAsyncConnection<String, String> connection = client.connectAsync();
		ExecutionHandler executionHandler = new ExecutionHandler(
				new OrderedMemoryAwareThreadPoolExecutor(5, 1048576, 1048576));
		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		bootstrap.setPipelineFactory(new HttpPipelineFactory(executionHandler, connection));
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.bind(sockAddress);
		System.out.println("listening on http://127.0.0.1:8080");
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
