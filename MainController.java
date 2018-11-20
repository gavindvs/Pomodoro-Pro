package application;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

public class MainController {
	
	@FXML private Spinner<Integer> sessionHours;
	@FXML private Spinner<Integer> sessionMins;
	@FXML private Spinner<Integer> shortBreakMins;
	@FXML private Spinner<Integer> longBreakHours;
	@FXML private Spinner<Integer> longBreakMins;
	@FXML private Spinner<Integer> cyclesNumber;
	@FXML private TabPane menu;
	@FXML private Pane timerMenu;
	@FXML private Pane sessionDisplay;
	@FXML private Label sessionCountDown;

	public void initialize() {
		timerMenu.setVisible(true);
		sessionDisplay.setVisible(false);
		SingleSelectionModel<Tab> selectionModel = menu.getSelectionModel();
		selectionModel.select(1);
		SpinnerValueFactory <Integer> sessionHoursFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9, 0);
		SpinnerValueFactory <Integer> sessionMinsFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 55, 0, 5);
		SpinnerValueFactory <Integer> shortBreakMinsFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 55, 0, 5);
		SpinnerValueFactory <Integer> longBreakHoursFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9, 0);
		SpinnerValueFactory <Integer> longBreakMinsFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 55, 0, 5);
		SpinnerValueFactory <Integer> cyclesFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 1);
		sessionHours.setValueFactory(sessionHoursFactory);
		sessionMins.setValueFactory(sessionMinsFactory);
		shortBreakMins.setValueFactory(shortBreakMinsFactory);
		longBreakHours.setValueFactory(longBreakHoursFactory);
		longBreakMins.setValueFactory(longBreakMinsFactory);
		cyclesNumber.setValueFactory(cyclesFactory);
		sessionHours.setEditable(true);
		sessionMins.setEditable(true);
		shortBreakMins.setEditable(true);
		longBreakHours.setEditable(true);
		longBreakMins.setEditable(true);
		cyclesNumber.setEditable(true);
		sessionHours.valueProperty().addListener((observableValue, oldValue, newValue) -> handleSpin(observableValue, oldValue, newValue, sessionHours));
		sessionMins.valueProperty().addListener((observableValue, oldValue, newValue) -> handleSpin(observableValue, oldValue, newValue, sessionMins));
		shortBreakMins.valueProperty().addListener((observableValue, oldValue, newValue) -> handleSpin(observableValue, oldValue, newValue, shortBreakMins));
		longBreakHours.valueProperty().addListener((observableValue, oldValue, newValue) -> handleSpin(observableValue, oldValue, newValue, longBreakHours));
		longBreakMins.valueProperty().addListener((observableValue, oldValue, newValue) -> handleSpin(observableValue, oldValue, newValue, longBreakMins));
		cyclesNumber.valueProperty().addListener((observableValue, oldValue, newValue) -> handleSpin(observableValue, oldValue, newValue, cyclesNumber));
	}
	private void handleSpin(ObservableValue<?> observableValue, Number oldValue, Number newValue, Spinner<Integer> spinner) {
        try {
            if (newValue == null) {
                spinner.getValueFactory().setValue((int)oldValue);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
	public void startTimer(ActionEvent event) {
		class sessionTimer {
			private int hours;
			private int mins;
			private Timer timer;
			private int delay = 1000;
		    private int period = 1000;
			
			public sessionTimer(int hr, int m) {
				hours = hr;
				mins = m;
			}
			public void getTime() {
				timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
			        public void run() {
						Runnable task = new Runnable() {
							public void run() {
								countDown();
							}
						};
						Thread backgroundThread = new Thread(task);
						backgroundThread.setDaemon(true);
						backgroundThread.start();
						System.out.println("Is thread alive? "+backgroundThread.isAlive());
						if(mins==-1) {
							try {
								backgroundThread.join();
								System.out.println("Is thread alive? "+backgroundThread.isAlive());
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
			        }
			    }, delay, period);
			}
			private void countDown() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
					    if (mins == -1) {
					        timer.cancel();					       
					    }
					    else {
					    	sessionCountDown.setText(Integer.toString(mins));
					    	--mins;
					    }
					}
				});
			}
		}
		int hours = sessionHours.getValue();
		int mins = sessionMins.getValue();
		sessionTimer timer = new sessionTimer(hours,mins);
		timerMenu.setVisible(false);
		sessionDisplay.setVisible(true);
		timer.getTime();
	}
}