package cn.ms.gateway.core.processor.connector.support;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateEvent;
import cn.ms.gateway.core.processor.connector.response.NettyHttpResponseFutureUtil;

public class NettyChannelPoolHandler extends SimpleChannelInboundHandler<HttpObject> {

//	private static final Logger logger = Logger.getLogger(NettyChannelPoolHandler.class.getName());
	private NettyChannelPool channelPool;

	/**
	 * @param channelPool
	 */
	public NettyChannelPoolHandler(NettyChannelPool channelPool) {
		super();
		this.channelPool = channelPool;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg)
			throws Exception {
		if (msg instanceof HttpResponse) {
			HttpResponse headers = (HttpResponse) msg;
			NettyHttpResponseFutureUtil.setPendingResponse(ctx.channel(), headers);
		}
		if (msg instanceof HttpContent) {
			HttpContent httpContent = (HttpContent) msg;
			NettyHttpResponseFutureUtil.setPendingContent(ctx.channel(), httpContent);
			if (httpContent instanceof LastHttpContent) {
				boolean connectionClose = NettyHttpResponseFutureUtil.headerContainConnectionClose(ctx.channel());

				NettyHttpResponseFutureUtil.done(ctx.channel());
				// the maxKeepAliveRequests config will cause server close the
				// channel, and return 'Connection: close' in headers
				if (!connectionClose) {
					channelPool.returnChannel(ctx.channel());
				}
			}
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
		if (evt instanceof IdleStateEvent) {
//			logger.log(Level.WARNING, "remove idle channel: " + ctx.channel());
			ctx.channel().close();
		} else {
			ctx.fireUserEventTriggered(evt);
		}
	}

	/**
	 * @param channelPool the channelPool to set
	 */
	public void setChannelPool(NettyChannelPool channelPool) {
		this.channelPool = channelPool;
	}
	
}
