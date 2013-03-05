package com.macys.testing.multivariant.handlers;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.google.common.base.Charsets;
import com.google.gson.Gson;

public class JsonEncoder extends OneToOneEncoder {

	private final Gson gson = new Gson();

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.setContent(ChannelBuffers.copiedBuffer(
				gson.toJson(msg), Charsets.UTF_8));
		return response;
	}

}
