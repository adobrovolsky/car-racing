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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.User;
import com.carracing.shared.network.ReadHandler;
import com.carracing.shared.network.WriteHandler;

public class RaceService implements AutoCloseable {
	
	@FunctionalInterface
	public interface ActionListener {
		void actionPerformed(Action action, Object data);
	}
	
	private static final Logger LOGGER = Logger.getLogger(RaceService.class.getSimpleName());
	
	private static final String HOST = "localhost";
	private static final int PORT = 8024;
	
	private User user;
	private static volatile RaceService instance;
	private WriteHandler writeHandler;
	private ReadHandler readHandler;
	private Socket socket;
	private ExecutorService executor = Executors.newFixedThreadPool(2);
	private Map<Action, Set<ActionListener>> listeners = new HashMap<>();
	
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
	
	@Override
	public void close() throws Exception {
		readHandler.close();
		writeHandler.close();
		socket.close();
		executor.shutdownNow();
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
	
	public boolean isLogin() {
		return user != null;
	}
	
	public void addListener(Action action, ActionListener listener) {
		 Set<ActionListener> set = listeners.get(action);
		 if (set == null) {
			 set = new HashSet<>();
		 }
		 set.add(listener);
		 listeners.put(action, set);
	}
	
	public void removeListener(Action action, ActionListener listener) {
		listeners.get(action).remove(listener);
	}
	
	public void send(Command command) {
		writeHandler.send(command);
	}
	
	class ClientReadHandler extends ReadHandler {
		
		public ClientReadHandler(InputStream is) throws IOException {
			super(is);
		}

		@Override
		protected void processCommand(Command command) throws Exception {
			LOGGER.info("Recived command - " + command);
			notifyListeners(command);
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
