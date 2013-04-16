package com.macys.testing.multivariant;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;

public class NettyModule extends AbstractModule {

	@Override
	protected void configure() {
		// TODO Auto-generated method stub

	}

	@Provides
	public SocketAddress getSocketAddress() {
		SocketAddress sockAddress;
		String sPort = System.getenv("PORT");
		if (sPort != null) {
			sockAddress = new InetSocketAddress(Integer.valueOf(sPort));
		} else {
			sockAddress = new InetSocketAddress(8080);
		}
		return sockAddress;
	}

	@Provides
	public ChannelFactory getChannelFactory() {
		ChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), 2,
				Executors.newCachedThreadPool(), 2);
		return factory;
	}

	@Provides
	public ExecutionHandler getExecutionHandler() {
		ExecutionHandler executionHandler = new ExecutionHandler(
				new OrderedMemoryAwareThreadPoolExecutor(10, 1048576, 1048576));
		return executionHandler;

	}

	@Provides
	@Singleton
	public RedisAsyncConnection<String, String> getRedisConnection() {
		RedisAsyncConnection<String, String> connection = null;
		try {
			String sURL = System.getenv("REDISTOGO_URL");
			if (sURL != null) {
				URI redisURI = new URI(sURL);
				RedisClient client = new RedisClient(redisURI.getHost(),
						redisURI.getPort());
				connection = client.connectAsync();
				connection.auth(redisURI.getUserInfo().split(":", 2)[1]);
			} else {
				RedisClient client = new RedisClient("127.0.0.1");
				connection = client.connectAsync();
			}
		} catch (URISyntaxException e) {
		}
		return connection;
	}
}
