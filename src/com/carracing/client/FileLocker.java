package com.carracing.client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileLocker {
	
	private static final Logger LOGGER = Logger.getLogger(FileLocker.class.getSimpleName());

	private final String tmpDir = System.getProperty("java.io.tmpdir");
	private final File file;
	private FileLock fileLock;
	private RandomAccessFile randomAccessFile;
	
	public FileLocker(final String fileName) {
		String filePath = tmpDir + File.separator + fileName;
		file = new File(filePath);
	}

	public boolean lock() {
		if (file.exists()) {
			return false;
		}

		try {
			randomAccessFile = new RandomAccessFile(file, "rw");
			fileLock = randomAccessFile.getChannel().tryLock();

			if (fileLock != null) {
				//Runtime.getRuntime().addShutdownHook(new ShutdownHook());
				return true;
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

		return false;
	}
	
	public void unlock() {
		try {
			fileLock.release();
			randomAccessFile.close();
			file.delete();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	private class ShutdownHook extends Thread {
		@Override public void run() {
			unlock();
		}
	}
}
