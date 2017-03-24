package com.carracing.client.view;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.carracing.client.Client;
import com.carracing.client.RaceService;
import com.carracing.client.RaceService.ActionListener;
import com.carracing.server.RaceOrganizer;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.RaceSummary;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

/**
 * This view displays a race in which five cars participate.
 */
public class CarRacing extends StackPane implements ActionListener {

	public static final String TITLE = "Race";
	
	@FXML private Label carLeader;
	@FXML private Label timer;
	@FXML private Pane trackOne;
	@FXML private Pane trackTwo;
	@FXML private Pane trackThree;
	@FXML private Pane trackFour;
	@FXML private Pane trackFive;

	private final RaceService service = RaceService.getInstance();
	private final ClassLoader loader = Client.class.getClassLoader();
	private MediaPlayer startPlayer, finishPlayer, racePlayer;
	private Map<Long, CarView> map = new HashMap<>();
	private List<Pane> tracks;
	private File soundsDir;
	
	private Timer t = new Timer();
	private TimeTask timerTask;
	private boolean started;

	public CarRacing() {
		inflateLayout();
		tracks = Arrays.asList(trackOne, trackTwo, trackThree, trackFour, trackFive);
		
		try {
			URI dir = loader.getResource("sounds/races").toURI();
			URI startSound = loader.getResource("sounds/start_sound.wav").toURI();
			URI finishSound = loader.getResource("sounds/start_sound.wav").toURI();
			
			soundsDir = new File(dir);
			startPlayer = new MediaPlayer(new Media(startSound.toString()));
			finishPlayer = new MediaPlayer(new Media(finishSound.toString()));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a view based on the fxml file.
	 */
	private void inflateLayout() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("car_racing.fxml"));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void actionPerformed(Action action, Object data) {
		Platform.runLater(() -> {
			switch (action) {
			case CHANGE_SPEED: handleChangeSpeed((Set<Car>) data); break;
			case FINISH_GAME: handleFinishGame((RaceSummary) data); break;
			}
		});
	}
	
	/**
	 * Plays a sound start of the race. Then randomly 
	 * chooses the sound for the race itself.
	 */
	private void playSound(int soundID) {
		File[] sounds = soundsDir.listFiles();
		int soundIndex = soundID % sounds.length;
		URI sound = sounds[soundIndex].toURI();
		racePlayer = new MediaPlayer(new Media(sound.toString()));

		startPlayer.play();
		racePlayer.play();
	}
	
	/**
	 * After the race time has passed, all the cars stop 
	 * and play the sound of the end of the race.
	 */
	private void handleFinishGame(RaceSummary summary) {
		map.entrySet().stream().forEach(entry -> {
			entry.getValue().getTransition().stop();
		});
		racePlayer.stop();
		timerTask.cancel();
		t.schedule(new CloseTask(), RaceOrganizer.DELAY_AFTER_RACE * 1000);
		finishPlayer.play();
		getChildren().add(new RaceSummaryView(summary));
	}
	
	/**
	 * Change the speed of all cars.
	 * @param cars all cars with new speeds
	 */
	private void handleChangeSpeed(Set<Car> cars) {
		cars.stream().forEach(car -> {
			CarView view = map.get(car.getId());
			Car oldCar = view.getCar();

			double oldSpeed = oldCar.getSpeed();
			double newSpeed = car.getSpeed();
			double rate;
			if (newSpeed > oldSpeed) rate = newSpeed / oldSpeed;
			else rate = 2 - (oldSpeed / newSpeed);
			
			view.getTransition().setRate(rate);
			carLeader.setText(identifyLeader(cars).toString());
		});
	}
	
	private Car identifyLeader(Collection<Car> cars) {
		Optional<Car> leader = cars.stream()
				.max((x, y) -> Comparator.<Double>naturalOrder()
				.compare(x.getDistance(), y.getDistance()));
		
		return leader.get();
	}
	/**
	 * Creates a {@link CarView} for each car.
	 * @param race the race that is currently running
	 */
	public void startRace(Race race) {
		started = true;
		service.addListener(Action.CHANGE_SPEED, this);
		service.addListener(Action.FINISH_GAME, this);
		timerTask = new TimeTask();
		t.schedule(timerTask, 0, 1000);
		
		Iterator<Car> cars = race.getCars().iterator();

		tracks.stream().forEach(track -> {
			if (cars.hasNext()) {
				Car car = cars.next();
				CarView carView = new CarView(car);
				track.getChildren().add(carView);
				map.put(car.getId(), carView);
				
				TranslateTransition transition = carView.getTransition();
				transition.setFromX(-180);
				transition.setToX(getWidth());
				transition.play();
			}
		});
		playSound(race.getSoundID());
		Car leader = identifyLeader(race.getCars());
		carLeader.setText(leader.toString());
	}
	
	public void close() {
		if (started) {
			service.removeListener(Action.CHANGE_SPEED, this);
			service.removeListener(Action.FINISH_GAME, this);
			t.cancel();
			racePlayer.stop();
		}
		
		Stage stage = (Stage) getScene().getWindow();
		stage.close();
	}
	
	private class CloseTask extends TimerTask {
		@Override public void run() {
			Platform.runLater(() -> close());
		}
	}
	
	/**
	 * This class implements a timer that is used to display the duration of a race.
	 */
	private class TimeTask extends TimerTask {
		private int time;
		
		@Override public void run() {
			Platform.runLater(() -> {
				time++;
				String timeString = String.format("%02d:%02d", time / 60, time % 60);
				timer.setText(timeString);
			});
		}
	}
}
