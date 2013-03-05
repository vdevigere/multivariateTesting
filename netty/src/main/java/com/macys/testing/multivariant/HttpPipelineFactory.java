package com.macys.testing.multivariant;

import javax.inject.Inject;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;

import com.lambdaworks.redis.RedisAsyncConnection;
import com.macys.testing.multivariant.handlers.JsonEncoder;
import com.macys.testing.multivariant.handlers.WeightedRandomNumberGenerator;

public class HttpPipelineFactory implements ChannelPipelineFactory {

	private final ExecutionHandler executionHandler;
	private final RedisAsyncConnection<String, String> connection;
	
	@Inject
	HttpPipelineFactory(ExecutionHandler executionHandler, RedisAsyncConnection<String, String> connection) {
		this.executionHandler = executionHandler;
		this.connection = connection;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("redisDb", executionHandler);
		pipeline.addLast("handler", new WeightedRandomNumberGenerator(
				connection));
		pipeline.addLast("JsonConverter", new JsonEncoder());
		return pipeline;
	}

}
