package com.carracing.client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allows you to synchronize between processes, that is, between several
 * Java applications. Used to run only one screen of reports.
 */
public class FileLocker {
	
	private static final Logger LOGGER = Logger.getLogger(FileLocker.class.getSimpleName());
	
	/**
	 * The default directory is specified by the system property java.io.tmpdir. 
	 * On UNIX systems the default value of this property is typically "/tmp" or "/var/tmp"; 
	 * on Microsoft Windows systems it is typically "C:\Users\Username\AppData\Local\Temp"
	 */
	private final String tmpDir = System.getProperty("java.io.tmpdir");
	private final File file;
	private FileLock fileLock;
	private RandomAccessFile randomAccessFile;
	
	public FileLocker(final String fileName) {
		String filePath = tmpDir + File.separator + fileName;
		file = new File(filePath);
	}
	
	/**
	 * Locks the specified file.
	 * 
	 * @return true if the file was locked.
	 */
	public boolean lock() {
		if (file.exists()) {
			return false;
		}

		try {
			randomAccessFile = new RandomAccessFile(file, "rw");
			fileLock = randomAccessFile.getChannel().tryLock();

			if (fileLock != null) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

		return false;
	}
	
	/**
	 * Unlocks the specified file.
	 */
	public void unlock() {
		try {
			fileLock.release();
			randomAccessFile.close();
			file.delete();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
