package com.carracing.server;

import static com.carracing.shared.model.Race.DURATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.server.repository.BetJdbcRepository;
import com.carracing.server.repository.BetJdbcRepository.SelectBetsByCar;
import com.carracing.server.repository.CarJdbcRepository;
import com.carracing.server.repository.RaceJdbcRepository;
import com.carracing.server.repository.RaceJdbcRepository.SelectUnfinishedRacesQuery;
import com.carracing.server.repository.RaceSummaryJdbcRepository;
import com.carracing.server.repository.Repository;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.Bet;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.Race.RaceStatus;
import com.carracing.shared.model.RaceSummary;
import com.carracing.shared.model.User;
import com.carracing.shared.model.reports.RaceReport;

public class RaceOrganizer {
	
	private static final Logger LOGGER = Logger.getLogger(RaceOrganizer.class.getSimpleName());
	
	private final Repository<Race> raceRepo = new RaceJdbcRepository();
	private final Repository<Car> carRepo = new CarJdbcRepository();
	private final Repository<Bet> betRepo = new BetJdbcRepository();
	private final Repository<RaceSummary> summaryRepo = new RaceSummaryJdbcRepository();
	
	private final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_RACES);
	
	/** 
	 * The maximum number of races.
	 */
	public static final int NUMBER_RACES = 10;
	
	/**
	 * Delay after the race is finished in seconds.
	 */
	public static final int DELAY_AFTER_RACE = 60;
	
	/** 
	 * The percentage of bets which takes the organizer.
	 */
	public static final int PERCENT = 5;
	
	/** 
	 * After how much time will be changing speed cars.
	 */
	public static final int CHANGE_SPEED_INTERVAL = 30;
	
	private final List<Race> races = new ArrayList<>(NUMBER_RACES);
	private final Map<Race, BetsByCarMap> betsMap = new HashMap<>();
	private boolean initialized;
	private Race activeRace;
	
	final Lock lock = new ReentrantLock();
	final Condition raceNotActive = lock.newCondition();
	
	/**
	 * Fills the object with all the necessary data.
	 * Loads all unfinished races from the database.
	 * Loads bets. And generates new races if necessary
	 */
	public void initialize() {
		loadRaces();
		loadBets();
		generateRaces(NUMBER_RACES - races.size());
		
		races.stream().forEach(race -> {
			executor.submit(new RaceWorker(race, this));
		});
		
		initialized = true;
	}
	
	/**
	 * Performs the basic algorithm of the race. Runs for each race
	 * Starts are performed when the race has a status "Active"
	 */
	public static class RaceWorker implements Runnable {
		
		private final Race race;
		private final RaceOrganizer organizer;
		
		public RaceWorker(Race race, RaceOrganizer organizer) {
			this.race = race;
			this.organizer = organizer;
		}

		@Override
		public void run() {
			LOGGER.info("Running race " + race);
			Thread.currentThread().setName("RaceWorker-" + race.toString());
			organizer.lock.lock();
			
			try {
				while (!race.isActive()) {
					organizer.raceNotActive.await();
				}
				
				organizer.setActiveRace(race);
				race.start();
				race.calcDistance(CHANGE_SPEED_INTERVAL);
				
				Server.notifyClients(new Command(Action.ADD_ACTIVE_RACE, race));
				
				int repeat = DURATION / CHANGE_SPEED_INTERVAL;
				for (int i = 0; i < repeat; i++) {
					TimeUnit.SECONDS.sleep(CHANGE_SPEED_INTERVAL);
					race.randomSpeed();
					race.calcDistance(CHANGE_SPEED_INTERVAL);
					
					Server.notifyClients(new Command(Action.CHANGE_SPEED, race.getCars()));
				}
				
				if (DURATION % CHANGE_SPEED_INTERVAL != 0) {
					int x = DURATION -(CHANGE_SPEED_INTERVAL * repeat);
					race.calcDistance(x);
				}
				
				race.finish();
				
				organizer.saveRace(race);
				organizer.sendResultToClients(race);
				
				TimeUnit.SECONDS.sleep(DELAY_AFTER_RACE);
				
				organizer.setActiveRace(null);
				organizer.removeRace(race);
				organizer.nextRace();
				organizer.raceNotActive.signalAll();
				
				LOGGER.info("Finished race " + race);
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				organizer.lock.unlock();
			}
		}
	}
	
	
	public void sendResultToClients(Race race) {
		BetsByCarMap bets = betsMap.get(race);
		Car carWinner = identifyCarWinner(race.getCars());
		Map<User, Double> usersPorfit = calcUsersProfit(carWinner, bets);
		double systemProfit = culcSystemProfit(bets, carWinner);
		
		RaceSummary summary = new RaceSummary();
		summary.setRace(race);
		summary.setSystemProfit(systemProfit);
		summary.setWinner(carWinner);
		summary.setUsers(usersPorfit);
		summary.setTotalBets(bets.getCountBets());
		summary.setAmountBets(bets.getSumBets());
		
		RaceReport report = new RaceReport();
		report.setCarName(carWinner.toString());
		report.setAmountBets(bets.getSumBets());
		report.setSystemProfit(systemProfit);
		report.setTotalBets(bets.getCountBets());
		report.setRaceName(race.toString());
		report.setDate(race.getStarted().toString());
		
		summaryRepo.add(summary);
		
		Server.notifyClients(new Command(Action.FINISH_GAME, summary));
		Server.notifyClients(new Command(Action.ADD_RACE_REPORT, report));
	}
	
	/**
	 * Returns the amount of money that the system received on this race
	 * 
	 * @param bets all bets on this race
	 * @param winner car winning the race
	 */
	private double culcSystemProfit(BetsByCarMap bets, Car winner) {
		double sumBets = bets.getSumBets();
		if (bets.forCar(winner).isEmpty()) {
			return sumBets;
		}
		return sumBets * PERCENT / 100;
	}
	
	/**
	 * Determines the car that traveled the longest distance
	 * 
	 * @param cars collection of cars that participated in this race
	 * @return the car that traveled the longest distance
	 */
	private Car identifyCarWinner(Collection<Car> cars) {
		Optional<Car> winner = cars.stream()
			.max((x, y) -> Comparator.<Double>naturalOrder()
			.compare(x.getDistance(), y.getDistance()));
		
		return winner.get();
	}
	
	/** 
	 * Finds all users who made bets on the winning car
	 * @return users with their winnings
	 */
	private Map<User, Double> calcUsersProfit(Car carWinner, BetsByCarMap map) {
		int totalAmountBets = map.getSumBets();
		List<Bet> betsForCarWinner = map.forCar(carWinner);
		Map<User, Double> usersProfit = new HashMap<>();
		
		Map<User, Double> profitForWinners = caclProfitForWinners(betsForCarWinner, totalAmountBets);
		
		map.forEach((car, bets) -> {
			if (!car.equals(carWinner)) {
				bets.forEach(bet -> {
					if (!profitForWinners.containsKey(bet.getUser())) {
						double amount = -bet.getAmount();
						if (usersProfit.containsKey(bet.getUser())) {
							amount += usersProfit.get(bet.getUser());
						}
						usersProfit.put(bet.getUser(), amount);
					}
				});
			}
		});
		usersProfit.putAll(profitForWinners);
		
		return usersProfit;
	}
	
	private Map<User, Double> caclProfitForWinners(List<Bet> bets, int totalAmountBets) {
		Map<User, Double> usersProfit = new HashMap<>();
		
		Integer amountBetsForCar = bets.stream()
				.map(bet -> bet.getAmount())
				.reduce(0, Integer::sum);
		
		bets.stream().forEach(bet -> {
			double userShare = (bet.getAmount() * 100D) / amountBetsForCar;
			if (usersProfit.containsKey(bet.getUser())) {
				userShare += usersProfit.get(bet.getUser());
			}
			usersProfit.put(bet.getUser(), userShare);
		});
		
		usersProfit.forEach((user, userShare)  -> {
			double userProfit = totalAmountBets * userShare / 100;
			userProfit -= userProfit * PERCENT / 100;
			usersProfit.put(user, userProfit);
		});
		
		return usersProfit;
	}
	
	/**
	 * Returns the race with the highest total bets
	 */
	public void nextRace () {
		int highestBetsSum = 0;
		Race nextRace = null;
		
		for (Race race : races) {
			if (race.isReady()) {
				BetsByCarMap bets = betsMap.get(race);
				int betsSum = bets.getSumBets();
				if (betsSum > highestBetsSum) {
					highestBetsSum = betsSum;
					nextRace = race;
				}
			}
		}
		
		if (nextRace != null && activeRace == null) {
			lock.lock();

			nextRace.setStatus(RaceStatus.ACTIVE);
			raceNotActive.signalAll();

			lock.unlock();
		}
	}
	
	public void removeRace(Race race) {
		races.remove(race);
	}

	public void saveRace(Race race) {
		raceRepo.update(race);
		race.getCars().stream().forEach(car -> carRepo.update(car));
	}

	public void setActiveRace(Race race) {
		this.activeRace = race;
	}
	
	private void loadRaces() {
		List<Race> loadedRaces = raceRepo.query(new SelectUnfinishedRacesQuery());
		races.addAll(loadedRaces);
		LOGGER.info("Loaded " + loadedRaces.size() + " races");
	}
	
	private void loadBets() {
		int count = 0;
		for (Race race : races) {
			BetsByCarMap betMap = new BetsByCarMap();
			for (Car car : race.getCars()) {
				List<Bet> bets = betRepo.query(new SelectBetsByCar(car));
				betMap.addAll(bets);
				count += bets.size();
			}
			betsMap.put(race, betMap);
		}
		LOGGER.info("Loaded " + count + " bets");
	}
	
	private void generateRaces(int num) {
		for (int i = 0; i < num; i++) {
			Race race = new Race();
			races.add(race);
			raceRepo.add(race);
			
			for (int j = 0; j < Race.NUMBER_CARS; j++) {
				Car car = new Car();
				car.fillRandom();
				car.setRace(race);
				if (carRepo.add(car)) {
					race.addCar(car);
				}
			}
		}
		LOGGER.info("Generated " + num + " races");
	}
	
	/**
	 * 
	 * @param bet determines which car to bet on and how much money
	 */
	public void makeBet(Bet bet) {
		requireInitialize();

		if (!betRepo.add(bet)) {
			return;
		}

		Race race = findRaceByCar(bet.getCar());
		BetsByCarMap map = betsMap.get(race);
		if (map == null) {
			map = new BetsByCarMap();
			betsMap.put(race, map);
		}
		map.add(bet);

		if (map.size() > 2 && !race.isReady()) {
			race.setStatus(RaceStatus.READY);
			raceRepo.update(race);
			bet.setRaceStateChanged(true);
		}

		nextRace();

		Server.notifyClients(new Command(Action.ADD_BET, bet));
	}

	public List<Race> getRaces() {
		requireInitialize();
		return races;
	}
	
	public Race getActiveRace() {
		requireInitialize();
		return activeRace;
	}
	
	public List<Bet> getBetsByCar(final Car car) {
		requireInitialize();
		Race race = findRaceByCar(car);
		BetsByCarMap bets = betsMap.get(race);
		if (bets == null) {
			return Collections.emptyList();
		}
		return bets.forCar(car);
	}
	
	private Race findRaceByCar(Car car) {
		for(Race race : races) {
			if (race.hasCar(car)) {
				return race;
			}
		}
		return null;
	}

	private void requireInitialize() {
		if (!initialized) {
			throw new IllegalStateException("You must first call initialize()");
		}
	}
	
	private static class BetsByCarMap {
		
		private final Map<Car, List<Bet>> map = new HashMap<>();
		private int countBets;
		private int sumBets;
		
		public void add(final Bet bet) {
			List<Bet> bets = map.get(bet.getCar());
			if (bets == null) {
				bets = new ArrayList<>();
				map.put(bet.getCar(), bets);
			}
			bets.add(bet);
			countBets++;
			sumBets += bet.getAmount();
		}
		
		public void addAll(Collection<Bet> newBets) {
			newBets.stream().forEach(bet -> add(bet));
		}
		
		public List<Bet> forCar(final Car car) {
			List<Bet> list = map.get(car);
			if (list == null) {
				return Collections.emptyList();
			}
			return list;
		}
		
		public void forEach(BiConsumer<? super Car, List<Bet>> consumer) {
			map.forEach(consumer);
		}
		
		public int getSumBets() {
			return sumBets;
		}

		public int size() {
			return map.size();
		}
		
		public int getCountBets() {
			return countBets;
		}
	}
}
