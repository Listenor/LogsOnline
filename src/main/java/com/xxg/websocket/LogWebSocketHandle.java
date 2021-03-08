package com.xxg.websocket;

import com.xxg.websocket.util.CheckSystemOS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/log/{param}", configurator = GetHttpSessionConfigurator.class)
public class LogWebSocketHandle {
	
	private Process process;
	private InputStream inputStream;

	private TailLogThread thread;

	/**
	 * 新的WebSocket请求开启
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config, @PathParam("param") String param) {



		if(!CheckSystemOS.isLinux()){
			System.out.println("非Linux系统");
			try {
				//testSend(session);
				session.getBasicRemote().sendText("不支持非Linux系统<br>");
				session.getBasicRemote().sendText("quit");

			} catch (IOException e) {
				e.printStackTrace();
				onClose();
			}
		}else{
			HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
			String allLogsName = (String) httpSession.getAttribute(Const.AllFilesNameStr);
			//限制，防止拼接命令
			if(null!= allLogsName && null!= param && allLogsName.indexOf(param)!=-1){
				try {
					// 执行tail -f命令
					String logsDir = (String) httpSession.getAttribute(Const.logsDir);
					if(null != logsDir && !"".equals(logsDir)){
						String tailLog = "tail -f "+logsDir+File.separator+param;
						process = Runtime.getRuntime().exec(tailLog);
						inputStream = process.getInputStream();

						// 一定要启动新的线程，防止InputStream阻塞处理WebSocket的线程
						//TailLogThread thread = new TailLogThread(inputStream, session);
						thread = new TailLogThread(inputStream, session);
						thread.start();
						System.out.println("sessionid:"+session.getId()+",threadid:"+thread.getId());
					}else{
						try {

							session.getBasicRemote().sendText("目录不存在，（需先访问首页缓存数据）<br>");
							session.getBasicRemote().sendText("quit");

						} catch (IOException e) {
							e.printStackTrace();
							onClose();
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				try {

					session.getBasicRemote().sendText("缺少参数，或文件不存在（需先访问首页获取列表）<br>");
					session.getBasicRemote().sendText("quit");

				} catch (IOException e) {
					e.printStackTrace();
					onClose();
				}
			}

		}

	}
	
	/**
	 * WebSocket请求关闭
	 */
	@OnClose
	public void onClose() {
		System.out.println("关闭连接");
		try {
			if(inputStream != null)
				inputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		if(process != null)
			process.destroy();
	}
	
	@OnError
	public void onError(Throwable thr) {
		thr.printStackTrace();
	}

	@OnMessage
	public void onMessage(String message, Session session){

		try {
			if("heart".equals(message)){
				session.getBasicRemote().sendText("heart");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}