package com.carracing.shared.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;

public class WriteHandler implements Runnable, AutoCloseable {

	private static final Logger LOGGER = Logger.getLogger(WriteHandler.class.getSimpleName());
	private final ObjectOutputStream os;
	private final BlockingQueue<Command> queue = new LinkedBlockingQueue<>();

	public WriteHandler(OutputStream os) throws IOException {
		this.os = new ObjectOutputStream(os);
	}

	@Override public void run() {
		LOGGER.info("WriteHandler started");
		while (!Thread.interrupted()) {
			try {
				Command command = queue.take();
				if (command.getAction() == Action.CLOSE_BLOCKING_QUEUE) {
					break;
				}
				os.writeObject(command);
				os.flush();
				os.reset();
				LOGGER.info("Sended command - " + command);
			} catch (IOException | InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		LOGGER.info("WriteHandler stoped");
	}
	
	public void send(final Command command) {
		queue.add(command);
	}

	@Override public void close() throws Exception {
		queue.add(new Command(Action.CLOSE_BLOCKING_QUEUE));
		os.close();
	}
}