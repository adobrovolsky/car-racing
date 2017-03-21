package com.carracing.shared.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.shared.Command;

/**
 * This class allows you to read commands from the input stream that was
 * received when the socket was opened. For reading is used {@link ObjectInputStream}
 * that can receive serialized objects. Since this handler runs continuously, 
 * it must be started in a separate thread, for this it implements the {@link Runnable}.
 * 
 * @see ObjectInputStream
 * @see Command
 */
public abstract class ReadHandler implements Runnable, AutoCloseable {
	
	private static final Logger LOGGER = Logger.getLogger(ReadHandler.class.getName());
	
	protected final ObjectInputStream is;
	
	/**
	 * This is the flag to stop reading.
	 */
	private boolean closed = false;
	
	public ReadHandler(InputStream is) throws IOException {
		this.is = new ObjectInputStream(is);
	}

	@Override public void run() {
		LOGGER.info("ReadHandler started");
		
		while (!closed) {
			try {
				Command command = (Command) is.readObject();
				processCommand(command);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				break;
			}
		}
		
		LOGGER.info("ReadHandler stoped");
	}
	
	/**
	 * Identifies the command and performs the corresponding actions.
	 * 
	 * @param command a command read from the input stream
	 */
	protected abstract void processCommand(Command command) throws Exception;
	
	@Override public void close() throws Exception {
		closed = false;
		is.close();
	}
}