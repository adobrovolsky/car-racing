package com.carracing.client.view;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.carracing.client.Main;
import com.carracing.client.RaceService;
import com.carracing.client.RaceService.ActionListener;
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

public class CarRacing extends StackPane implements ActionListener {

	public static final String TITLE = "Game";
	
	@FXML private Label carLeader;
	@FXML private Label timer;
	@FXML private Pane trackOne;
	@FXML private Pane trackTwo;
	@FXML private Pane trackThree;
	@FXML private Pane trackFour;
	@FXML private Pane trackFive;

	private RaceService service = RaceService.getInstance();
	private MediaPlayer startPlayer, finishPlayer, gamePlayer;
	private ClassLoader loader = Main.class.getClassLoader();
	private Map<Long, CarView> map = new HashMap<>();
	private List<Pane> tracks;
	private RaceSummaryView summaryView;
	private Race race;
	private Timer t = new Timer();
	private TimeTask timerTask;

	public CarRacing() {
		inflateLayout();
		tracks = Arrays.asList(trackOne, trackTwo, trackThree, trackFour, trackFive);

		service.addListener(Action.ADD_ACTIVE_RACE, this);
		service.addListener(Action.CHANGE_SPEED, this);
		service.addListener(Action.FINISH_GAME, this);
	}

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
			case ADD_ACTIVE_RACE:
				handleAddActiveRace((Race) data);
				break;
			case CHANGE_SPEED:
				handleChangeSpeed((Set<Car>) data);
				break;
			case FINISH_GAME:
				handleFinishGame((RaceSummary) data);
				break;
			}
		});
	}

	private void playSound() {
		try {
			URI soundsDir = loader.getResource("sounds/races").toURI();
			URI startSound = loader.getResource("sounds/start_sound.wav").toURI();
			File dir = new File(soundsDir);
			File[] sounds = dir.listFiles();

			int soundIndex = race.getSoundID() % sounds.length;
			URI sound = sounds[soundIndex].toURI();
			startPlayer = new MediaPlayer(new Media(startSound.toString()));
			gamePlayer = new MediaPlayer(new Media(sound.toString()));

			startPlayer.play();
			gamePlayer.play();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private void handleFinishGame(RaceSummary summary) {
		map.entrySet().stream().forEach(entry -> {
			entry.getValue().getTransition().stop();
		});
		gamePlayer.stop();
		timerTask.cancel();

		try {
			URI finishSound = loader.getResource("sounds/start_sound.wav").toURI();
			finishPlayer = new MediaPlayer(new Media(finishSound.toString()));
			finishPlayer.play();

			summaryView = new RaceSummaryView(summary);
			getChildren().add(summaryView);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}

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
		});
	}

	private void handleAddActiveRace(Race race) {
		this.race = race;
		getChildren().remove(summaryView);
		timerTask = new TimeTask();
		t.schedule(timerTask, 0, 1000);
		Iterator<Car> cars = race.getCars().iterator();

		tracks.stream().forEach(track -> {
			track.getChildren().clear();

			if (cars.hasNext()) {
				Car car = cars.next();
				CarView carView = new CarView(car);
				track.getChildren().add(carView);
				map.put(car.getId(), carView);
				
				TranslateTransition transition = carView.getTransition();
				transition.setToX(getWidth() - 150);
				transition.play();
			}
		});
		playSound();
	}
	
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
