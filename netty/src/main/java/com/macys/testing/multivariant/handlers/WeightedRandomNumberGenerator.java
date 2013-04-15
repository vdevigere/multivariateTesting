package com.macys.testing.multivariant.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.lambdaworks.redis.RedisAsyncConnection;

public class WeightedRandomNumberGenerator extends SimpleChannelUpstreamHandler {
	private RedisAsyncConnection<String, String> connection;

	public WeightedRandomNumberGenerator(RedisAsyncConnection<String, String> connection) {
		this.connection = connection;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Map<String, Map<String, String>> testMap = new HashMap<String, Map<String, String>>();
		Future<Set<String>> sMembersFuture = connection.smembers("testgroups");
		if (connection.awaitAll(sMembersFuture) == true) {
			Set<String> testGroups = sMembersFuture.get();
			Map<String, Future<Map<String, String>>> hGetAllFuturesMap = new HashMap<String, Future<Map<String, String>>>();
			for (String testGroup : testGroups) {
				hGetAllFuturesMap.put(testGroup, connection.hgetall(testGroup));
			}

			if (connection.awaitAll() == true) {
				for (String testGroup : hGetAllFuturesMap.keySet()) {
					testMap.put(testGroup, hGetAllFuturesMap.get(testGroup).get());
				}
			}
		}

		List<String> randomVariantList = getRandomVariantList(testMap);
		e.getChannel().write(randomVariantList).addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				future.getChannel().close();
			}
		});
	}

	protected final List<String> getRandomVariantList(Map<String, Map<String, String>> testMap)
			throws NumberFormatException, InterruptedException, ExecutionException {
		List<String> randomVariantList = new ArrayList<String>();
		for (Map<String, String> variantsAndWeight : testMap.values()) {
			// Compute the total weight of all items together
			double totalWeight = 100.0d;
			// Now choose a random item
			double random = Math.random() * totalWeight;
			for (String variant : variantsAndWeight.keySet()) {
				random -= Double.valueOf(variantsAndWeight.get(variant));
				if (random <= 0.0d) {
					randomVariantList.add(variant);
					break;
				}
			}// End of For loop (Variants).
		}// End of For loop (Test Groups)
		return randomVariantList;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
