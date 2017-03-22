package com.carracing.server;

import java.util.List;

import com.carracing.server.repository.ReportsJdbcRepository;
import com.carracing.server.repository.ReportsJdbcRepository.SelectAllRaceReportsQuery;
import com.carracing.server.repository.Repository;
import com.carracing.server.repository.Specification;
import com.carracing.server.repository.UserJdbcRepository;
import com.carracing.server.repository.UserJdbcRepository.SelectUserByLoginAndPasswordQuery;
import com.carracing.shared.model.Bet;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.RaceOrganizer;
import com.carracing.shared.model.RaceReport;
import com.carracing.shared.model.User;


public class RaceService {
	
	private final RaceOrganizer organizer;
	private static volatile RaceService instance;
	private final Repository<User> userRepository = new UserJdbcRepository();
	private final Repository<RaceReport> reportsRepository = new ReportsJdbcRepository();
	
	private RaceService() {
		organizer = new RaceOrganizer();
		organizer.initialize();
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

	public List<Race> obtainRaces() {
		return organizer.getRaces();
	}
	
	public Race obtainActiveRace() {
		return organizer.getActiveRace();
	}
	
	public List<Bet> obtainBets(final Car car) {
		return organizer.getBetsByCar(car);
	}
	
	public List<RaceReport> obtainReports() {
		List<RaceReport> reports = reportsRepository.query(new SelectAllRaceReportsQuery());
		return reports;
	}
	
	public void makeBet(final Bet bet) {
		organizer.makeBet(bet);
	}
	
	public User login(final User user) {
		Specification spec = new SelectUserByLoginAndPasswordQuery(user.getLogin(), user.getPassword());
		List<User> users = userRepository.query(spec);
		if (users.isEmpty()) return null;
		return users.get(0);
	}
	
	public boolean signup(User user) {
		return userRepository.add(user);
	}
	
	public RaceOrganizer getRaceOrganizer() {
		return organizer;
	}
}