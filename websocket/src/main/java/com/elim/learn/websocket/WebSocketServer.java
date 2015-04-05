package com.elim.learn.websocket;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * 每个连接将产生一个WebSocketServer对象与客户端进行通信。
 * 该WebSocket服务必须在实现了JSR356规范的应用服务器上运行。
 * @author elim
 *
 */
@ServerEndpoint(value="/ws/{param}")
public class WebSocketServer {

	private final static Map<Session, Session> sessionMap = new ConcurrentHashMap<>();
	private static ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
	static {
		ses.scheduleAtFixedRate(new TimerTask(), 1, 1, TimeUnit.SECONDS);
	}
	
	/**
	 * 当建立连接时将回调该方法
	 * @param session
	 */
	@OnOpen
	public void onOpen(Session session) {
		sessionMap.put(session, session);
		System.out.println("---建立了连接");
	}
	
	/**
	 * 当接收到信息的时候将回调该方法
	 * @param message 文本消息的内容
	 * @param session
	 * @param param 对应访问路径的路径参数
	 */
	@OnMessage
	public void onMessage(String message, Session session, @PathParam("param") String param) {
		System.out.println("收到了信息：" + message);
		try {
			session.getBasicRemote().sendText("Hello.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 当连接关闭时将回调该方法。连接的关闭既可以通过客户端的WebSocket对象的close()方法进行，也可以通过服务端的Session的close()方法进行。
	 * @param session
	 */
	@OnClose
	public void onClose(Session session) {
		System.out.println("连接已关闭");
		sessionMap.remove(session);
	}
	
	/**
	 * 将在出错时调用，对应的参数必须包括Throwable
	 * @param cause
	 */
	@OnError
	public void onError(Throwable cause) {
		System.out.println("出错了");
	}
	
	private final static class TimerTask implements Runnable {

		private int counter;
		
		@Override
		public void run() {
			String message = "来自服务端的消息，当前时间是：" + new Date();
			counter++;
			if (!sessionMap.isEmpty()) {
				for (Session session : sessionMap.keySet()) {
					session.getAsyncRemote().sendText(message);
					if (counter > 100) {//超过100s时自动关闭连接
						try {
							session.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
	}
	
}
