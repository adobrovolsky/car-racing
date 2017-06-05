package com.carracing.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.network.ReadHandler;
import com.carracing.shared.network.WriteHandler;

/**
 * This class is responsible for interacting with the server.
 * The server and the client communicate by means of sockets. 
 * For the input and output streams are created with separate handlers, 
 * which are executed in different threads.
 * 
 * @see WriteHandler
 * @see ReadHandler
 * @see Socket
 */
public class RaceService implements AutoCloseable {
	
	@FunctionalInterface
	public interface ActionListener {
		void actionPerformed(Action action, Object data);
	}
	
	private static final Logger LOGGER = Logger.getLogger(RaceService.class.getSimpleName());
	
	private static final String HOST = "localhost";
	private static final int PORT = 8024;
	
	private static volatile RaceService instance;
	private WriteHandler writeHandler;
	private ReadHandler readHandler;
	private Socket socket;
	private ExecutorService executor = Executors.newFixedThreadPool(2);
	private AtomicInteger requestID = new AtomicInteger(0);
	
	/**
	 * Listeners the server events which grouping by action.
	 */
	private Map<Action, Set<ActionListener>> listeners = new HashMap<>();
	
	private Map<Integer, ActionListener> sessionListeners = new HashMap<>(); 
	
	private RaceService() {
		try { 
			socket = new Socket(HOST, PORT);
			
			LOGGER.info("Connection established with the server");
			
			writeHandler = new WriteHandler(socket.getOutputStream());
			readHandler = new ClientReadHandler(socket.getInputStream());
			executor.submit(writeHandler);
			executor.submit(readHandler);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * This service is created in one instance for the entire application.
	 * 
	 * @return instance of this service
	 */
	public static RaceService getInstance() {
		if (instance == null) {
			synchronized (RaceService.class) {
				if (instance == null) {
					instance = new RaceService();
				}
			}
		}
		return instance;
	}
	
	/**
	 * Stops threads and closes an open socket.
	 */
	@Override public void close() throws Exception {
		readHandler.close();
		writeHandler.close();
		socket.close();
		executor.shutdownNow();
	}
	
	/**
	 * Adds a listener to the specified action.
	 * 
	 * @param action the listener will receive this action
	 * @param listener it will be called when the action happens
	 */
	public void addListener(Action action, ActionListener listener) {
		 Set<ActionListener> set = listeners.get(action);
		 if (set == null) {
			 set = new HashSet<>();
			 listeners.put(action, set);
		 }
		 set.add(listener);
		 
	}
	
	public void removeListener(Action action, ActionListener listener) {
		listeners.get(action).remove(listener);
	}
	
	/**
	 * Sends the transmitted command to the server.
	 */
	public void send(Command command) {
		writeHandler.send(command);
	}
	
	/**
	 * Allows you to communicate with the server in the request-response mode. 
	 * This means that only one listener will receive a response. Since the work with the server
	 * is asynchronously, we add a unique identifier to the command.
	 * 
	 * @param command describes the action which to be performed on the server.
	 * @param listener receives a response from the server.
	 */
	public void send(Command command, ActionListener listener) {
		int id = requestID.getAndIncrement();
		command.setMeta(id);
		sessionListeners.put(id, listener);
		writeHandler.send(command);
	}
	
	/**
	 * This class reads commands from the server.
	 * Notifies all listeners about getting command.
	 */
	class ClientReadHandler extends ReadHandler {
		
		public ClientReadHandler(InputStream is) throws IOException {
			super(is, null);
		}

		@Override
		protected void processCommand(Command command) throws Exception {
			LOGGER.info("Recived command - " + command);
			
			ActionListener listener = sessionListeners.get(command.getMetadata());
			if (listener != null) {
				listener.actionPerformed(command.getAction(), command.getData());
				sessionListeners.remove(listener);
			} else {
				notifyListeners(command); 
			}
		}
		
		private void notifyListeners(Command command) {
			Set<ActionListener> set = listeners.get(command.getAction());
			if (set != null) {
				set.stream().forEach(listener -> 
					listener.actionPerformed(command.getAction(), command.getData()));
				
				LOGGER.info("Notified " + set.size() + " listeners");
			}
		}
	}
}
