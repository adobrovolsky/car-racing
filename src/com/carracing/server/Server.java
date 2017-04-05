package com.carracing.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.shared.Command;


public class Server {
	
	private static final Logger LOGGER = Logger.getLogger(Server.class.getSimpleName());
	
	private int port = 8024;
	
	/**
	 * This is the flag that determines whether the server will process
	 * client requests. If the value is false then the server will stops.
	 */
	private boolean listening;
	
	/**
	 * A thread pool that starts a new thread for each incoming request.
	 */
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * Stores a query handler for each client that is created once.
	 */
	private static final Map<String, ClientHandler> handlersMap = new HashMap<>();
	
	/**
	 * Receives client requests. For each client created request handler
	 * and stored in a map. The next call uses the existing handler from map.
	 */
	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			LOGGER.info("Server listening port " + port);
			
			listening = true;
			while(listening) {
				Socket clientSocket = serverSocket.accept();
				String hostAddress = clientSocket.getInetAddress().getHostAddress() + clientSocket.getPort();
				
				if (!handlersMap.containsKey(hostAddress)) {
					ClientHandler handler = new ClientHandler(clientSocket);
					handlersMap.put(hostAddress, handler);
					LOGGER.info("Created new handler");
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	/**
	 * Sets the flag to false, which is the output condition. 
	 * Also closes all open client sockets.
	 */
	public void stop() {
		listening = false;

		handlersMap.values().forEach(handler -> {
			try {
				handler.close();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		});
	}
	
	/**
	 * Sends this command to all clients.
	 */
	public static void notifyClients(Command command) {
		handlersMap.values().forEach(handler -> {
			handler.send(command);
		});
	}
	
	/**
	 * Performs this task through Service Executor.
	 */
	public static void runThread(Runnable task) {
		executor.execute(task);	
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}
}
