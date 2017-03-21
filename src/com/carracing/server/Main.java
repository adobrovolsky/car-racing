package com.carracing.server;

/**
 * This is the entry point to the program. 
 * To start the server, you need to run this class.
 * 
 * @see Server
 * @version 1.0
 */
public class Main {

	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}
}
