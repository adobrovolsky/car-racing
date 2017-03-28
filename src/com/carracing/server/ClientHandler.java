package com.carracing.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.Bet;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.User;
import com.carracing.shared.model.reports.CarReport;
import com.carracing.shared.model.reports.GamblerReport;
import com.carracing.shared.model.reports.RaceReport;
import com.carracing.shared.network.ReadHandler;
import com.carracing.shared.network.WriteHandler;

/**
 * Processes all client requests. One instance is created for each client.
 */
public class ClientHandler implements AutoCloseable {
	
	private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getSimpleName());
	
	private final Socket socket;
	
	/**
	 * Writes a command to the output stream of the socket.
	 */
	private final WriteHandler writeHandler;
	
	/**
	 * Reads commands from the input stream of the socket.
	 */
	private final ReadHandler readHandler;
	
	
	/**
	 * After the ReadHandler detected the command, it gives it to perform this service.
	 */
	private final RaceService raceService;
	
	/**
	 * Creates two handlers, one will read the data from the input stream 
	 * and the second to write to the output stream. Each works in a separate thread.
	 * 
	 * @param socket open client socket
	 */
	public ClientHandler(Socket socket) {
		this.socket = socket;
		try {
			raceService = RaceService.getInstance();
			writeHandler = new WriteHandler(socket.getOutputStream());
			readHandler = new ServerReadHandler(socket.getInputStream());
			Server.runThread(readHandler);
			Server.runThread(writeHandler);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes data to the socket output stream.
	 */
	public void send(Command command) {
		writeHandler.send(command);
	}
	
	@Override public void close() throws Exception {
		writeHandler.close();
		readHandler.close();
		socket.close();
	}
	
	/**
	 * Reads a command from the input stream of the socket
	 * and determines what kind of command it is.
	 *
	 */
	class ServerReadHandler extends ReadHandler {

		public ServerReadHandler(InputStream is) throws IOException {
			super(is);
		}

		@Override protected void processCommand(Command command) {
			Action action = command.getAction();
			
			LOGGER.info("Handling command: " + command);
			
			switch (action) {
			case OBTAIN_ACTIVE_RACE: obtainActiveRace(); break;
			case OBTAIN_RASES: obtainRaces(); break;
			case OBTAIN_BETS: obtainBets(command.getData()); break;
			case OBTAIN_RACE_REPORTS: obtainRaceReports(); break;
			case MAKE_BET: makeBet(command.getData()); break;
			case LOGIN: login(command.getData()); break;
			case SIGNUP: signup(command.getData()); break;
			case OBTAIN_CAR_REPORTS: obtainCarReports(command.getData()); break;
			case OBTAIN_GAMBLER_REPORTS: obtainGamblerReports(); break;
			}
		}

		private void obtainGamblerReports() {
			List<GamblerReport> reports = raceService.obtainGamblerReports();
			Command command = new Command(Action.ADD_GAMBLER_REPORTS, reports);
			writeHandler.send(command);
		}

		private void obtainCarReports(CarReport.Query query) {
			List<CarReport> carReports = raceService.obtainCarReports(query);
			Command command = new Command(Action.ADD_CAR_REPORTS, carReports);
			writeHandler.send(command);
		}

		private void obtainRaces() {
			List<Race> races = raceService.obtainRaces();
			Command command = new Command(Action.ADD_RACES, races);
			writeHandler.send(command);
		}

		private void obtainActiveRace() {
			Race activeRace = raceService.obtainActiveRace();
			Command command = new Command(Action.ADD_ACTIVE_RACE, activeRace);
			writeHandler.send(command);
		}

		private void obtainBets(final Car car) {
			List<Bet> bets = raceService.obtainBets(car);
			Command command = new Command(Action.ADD_BETS, bets);
			writeHandler.send(command);
		}

		private void obtainRaceReports() {
			List<RaceReport> raceReports = raceService.obtainRaceReports();
			Command command = new Command(Action.ADD_RACE_REPORTS, raceReports);
			writeHandler.send(command);
		}

		private void signup(final User user) {
			boolean succeful = raceService.signup(user);
			Command command = new Command(Action.CHECK_SIGNUP_RESULT, succeful);
			writeHandler.send(command);
		}

		private void login(final User credentials) {
			User user = raceService.login(credentials);
			Command command = new Command(Action.ADD_USER, user);
			writeHandler.send(command);
		}

		public void makeBet(final Bet bet) {
			raceService.makeBet(bet);
		}
	}
}