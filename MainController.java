package application;

import org.controlsfx.control.Notifications;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainController {
	
	@FXML private AnchorPane anchorPane;
	@FXML private Spinner<Integer> sessionHours;
	@FXML private Spinner<Integer> sessionMins;
	@FXML private Spinner<Integer> shortBreakMins;
	@FXML private Spinner<Integer> longBreakHours;
	@FXML private Spinner<Integer> longBreakMins;
	@FXML private Spinner<Integer> cyclesNumber;
	@FXML private Pane timerMenu;
	@FXML private Pane sessionDisplay;
	@FXML private Label sessionCountDown;
	@FXML private Label cycleCount;
	@FXML private Button pauseTimerSession;
	@FXML private Button resumeTimerSession;
	private PomodoroTimer pomodoroInstance;
	private PushNotification notificationInstance;

	public void initialize() {
		timerMenu.setVisible(true);
		sessionDisplay.setVisible(false);
		//Assign value factory to spinners
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
		//Make spinners editable
		sessionHours.setEditable(true);
		sessionMins.setEditable(true);
		shortBreakMins.setEditable(true);
		longBreakHours.setEditable(true);
		longBreakMins.setEditable(true);
		cyclesNumber.setEditable(true);
		//Add listener to spinners to account for edited values
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
	//Method handler for minimise button
	public void minButton(ActionEvent event) {
		Stage stage = (Stage)anchorPane.getScene().getWindow();
		stage = (Stage)((Button)event.getSource()).getScene().getWindow();
	    stage.setIconified(true);
	}
	//Method handler for close button
	public void closeButton(ActionEvent event) {
		Stage stage = (Stage)anchorPane.getScene().getWindow();
		stage = (Stage)((Button)event.getSource()).getScene().getWindow();
		if(pomodoroInstance != null) pomodoroInstance.sessionExitRoutine();
		if(notificationInstance != null) notificationInstance.notificationExitRoutine();
		stage.close();
		System.exit(0);
	}
	//Class for Pomodoro Timer. Consists of: session timer, short break timer and long break timer
	class PomodoroTimer {
		private int[] hours; 
		private int[] mins;
		private int[] cycles;
		private int[] sBreakMins;
		private int[] lBreakHours;
		private int[] lBreakMins;
		private Timer timer;
		private int delay = 500;
	    private int period = 500;
	    private String[] timerStage = {"session", "sBreak", "lBreak", "timerCompleted"};
	    private int stage = 0;
	    private Thread backgroundThread;
		
		public PomodoroTimer(int hr, int m, int c, int sbMins, int lbHours, int lbMins) {
			hours = new int[] {hr,hr};
			mins = new int[] {m,m};
			cycles = new int[] {1,c};
			sBreakMins = new int[] {sbMins, sbMins};
			lBreakHours = new int[] {lbHours, lbHours};
			lBreakMins = new int[] {lbMins, lbMins};
		}
		//Method which calls the running of the Pomodoro Timer
		public void runTimer() {
			//Create timer instance and schedule at fixed rate 1sec
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		        	//Create a Runnable Thread: backgroundThread, which will host the Pomodoro Timer
					backgroundThread = new Thread(()->startSession());
					backgroundThread.setDaemon(true);
					backgroundThread.start();
					//If long break timer is completed and cycle goal is met, close Runnable Thread
					if((lBreakHours[0]==0 && lBreakMins[0]==0) && (cycles[0] == cycles[1])) {
						try {
							backgroundThread.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
		        }
		    }, delay, period);
		}
		//Method handler for Pause/Stop Timer button. Cancels timer instance and closes Runnable Thread
		public void interruptTimer() {
			for(int i = 0; i < 10; i++) {
				try {
					timer.cancel();
					backgroundThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//Additional method handler for Stop Timer button. Clears from memory timer progress and purges timer instance
		public void clearTimerHistory() {
			hours[0] = 0;
			mins[0] = 0;
			cycles[0] = 0;
			sBreakMins[0] = 0;
			lBreakHours[0] = 0;
			lBreakMins[0] = 0;
			timer.purge();
		}
		//Method which acts as the main loop for the Pomodoro Timer 
		private void startSession() {
			//Make use of platform runLater to push backgroundThread onto FX application thread for UI updates
			Platform.runLater(() -> {
					//Switch case which will select Pomodoro Timer stage based on the value of the variable: stage
					switch(timerStage[stage]) {
					//Session timer stage
					case "session":
						cycleCount.setText(Integer.toString(cycles[0])+"/"+Integer.toString(cycles[1]));
						switch(mins[0]) {
							case 0:
								if (hours[0] == 0) {
									sessionCountDown.setText("Session Time \n"+Integer.toString(hours[0])+"H 0"+Integer.toString(mins[0])+"M");
									if(cycles[0]==cycles[1]) {
										sessionCountDown.setText("Session\nComplete!");
										stage=2; //Long-Break stage
										notificationInstance = new PushNotification();
										notificationInstance.runNotification(timerStage[stage]);
										break;
									}
									else {
										sessionCountDown.setText("Session\nComplete!");
										++cycles[0];
										loader();
										stage=1; //Short-Break stage
										notificationInstance = new PushNotification();
										notificationInstance.runNotification(timerStage[stage]);
										break;
									}
								}
								else if (hours[0] != 0) {
									sessionCountDown.setText("Session Time \n"+Integer.toString(hours[0])+"H 0"+Integer.toString(mins[0])+"M");
									--hours[0];
									mins[0]=59;
								}
								break;
							default:
								if(mins[0]<10) {
									sessionCountDown.setText("Session Time \n"+Integer.toString(hours[0])+"H 0"+Integer.toString(mins[0])+"M");
									--mins[0];	
								}
								else {
									sessionCountDown.setText("Session Time \n"+Integer.toString(hours[0])+"H "+Integer.toString(mins[0])+"M");
									--mins[0];
								}
								break;
						}
						break;
					//Short break stage
					case "sBreak":
						cycleCount.setText(Integer.toString(cycles[0])+"/"+Integer.toString(cycles[1]));
						switch(sBreakMins[0]) {
							case 0:
								sessionCountDown.setText("Break Ended");
								loader();
								stage=0; //Session stage
								notificationInstance = new PushNotification();
								notificationInstance.runNotification(timerStage[stage]);
								break;
							default:
								if(sBreakMins[0]<10) {
									sessionCountDown.setText("Break Time \n0"+Integer.toString(sBreakMins[0])+"M");
									--sBreakMins[0];	
								}
								else {
									sessionCountDown.setText("Break Time \n"+Integer.toString(sBreakMins[0])+"M");
									--sBreakMins[0];
								}
								break;
						}
						break;
					//Long break stage
					case "lBreak":
						cycleCount.setText(Integer.toString(cycles[0])+"/"+Integer.toString(cycles[1]));
						switch(lBreakMins[0]) {
							case 0:
								if (lBreakHours[0] == 0) {
									sessionCountDown.setText("Break Time \n"+Integer.toString(lBreakHours[0])+"H 0"+Integer.toString(lBreakMins[0])+"M");
									sessionCountDown.setText("Timer\nCompleted!");
									stage = 3; //Pomodoro timer completed stage
									notificationInstance = new PushNotification();
									notificationInstance.runNotification(timerStage[stage]);
									break;
								}
								else if (lBreakHours[0] != 0) {
									sessionCountDown.setText("Break Time \n"+Integer.toString(lBreakHours[0])+"H 0"+Integer.toString(lBreakMins[0])+"M");
									--lBreakHours[0];
									lBreakMins[0]=59;
								}
								break;
							default:
								if(lBreakMins[0]<10) {
									sessionCountDown.setText("Break Time \n"+Integer.toString(lBreakHours[0])+"H 0"+Integer.toString(lBreakMins[0])+"M");
									--lBreakMins[0];	
								}
								else {
									sessionCountDown.setText("Break Time \n"+Integer.toString(lBreakHours[0])+"H "+Integer.toString(lBreakMins[0])+"M");
									--lBreakMins[0];
								}
								break;
						}
						break;
					//Pomodoro timer completed stage
					case "timerCompleted":
						timer.cancel();
						break;
					}
			});
		}
		//Method which re-initialises session/short-break timer with values selected by user. 
		//Used prior to the running of each session/short-break timer
		private void loader() {
			hours[0]=hours[1]; 
			mins[0]=mins[1];
			sBreakMins[0]=sBreakMins[1];
			return;
		}
		//Exit routine which will clear resources used in the running of the Pomodoro timer
		public void sessionExitRoutine() {
			if(backgroundThread!=null) {
				if (backgroundThread.isAlive()!=false) {
					for(int i = 0; i < 10; i++) {
						try {
							timer.cancel();
							timer.purge();
							backgroundThread.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				else
					return;
			}
			else
				return;
		}
	}
	//Method handler for Create Timer button
	public void createTimer(ActionEvent event) {
		int hours = sessionHours.getValue();
		int mins = sessionMins.getValue();
		int cycles = cyclesNumber.getValue();
		int sBreakMins = shortBreakMins.getValue();
		int lBreakHours = longBreakHours.getValue();
		int lBreakMins = longBreakMins.getValue();
		//Create a Pomodoro timer instance and execute it
		pomodoroInstance = new PomodoroTimer(hours,mins,cycles,sBreakMins,lBreakHours,lBreakMins);
		timerMenu.setVisible(false);
		sessionDisplay.setVisible(true);
		pauseTimerSession.setVisible(true);
		resumeTimerSession.setVisible(false);
		pomodoroInstance.runTimer();
	}
	//Method handler for Pause Timer button
	public void pauseTimer() {
		pomodoroInstance.interruptTimer();
		pauseTimerSession.setVisible(false);
		resumeTimerSession.setVisible(true);
	}
	//Method handler for Resume Timer button
	public void resumeTimer() {
		pomodoroInstance.runTimer();
		pauseTimerSession.setVisible(true);
		resumeTimerSession.setVisible(false);
	}
	//Method handler for Stop Timer button
	public void stopTimer() {
		pomodoroInstance.interruptTimer();
		pomodoroInstance.clearTimerHistory();
		sessionCountDown.setText("Session Time");
		cycleCount.setText("Cycles");
		sessionDisplay.setVisible(false);
		timerMenu.setVisible(true);
	}
	//Class for Push Notifications which are executed after each Pomodoro Timer stage
	class PushNotification {
		private String header;
		private String body;
		private Stage stage;
		private Timer timer;
		private Thread pushNotification;
		
		private void setHeader(String h) {header = h;}
		private void setBody(String b) {body = b;}
		private String getHeader() {return header;}
		private String getBody() {return body;}
		
		//Method which executes a push notification
		public void runNotification(String timerStage) {
			//Fill notification title and text reflecting Pomodoro timer stage completed
			switch(timerStage) {
			case "session":
				this.setHeader("Break Ended");
				this.setBody("Time to get back to work");
				break;
			case "sBreak":
				this.setHeader("Session Completed!");
				this.setBody("Time for a short break");
				break;
			case "lBreak":
				this.setHeader("Session Goal Met!");
				this.setBody("Time for a long break");
				break;
			case "timerCompleted":
				this.setHeader("Timer Completed");
				this.setBody("Job well done!");
				break;
			}
			//Make use of platform runLater to push pushNotification onto FX application thread for UI updates
			Platform.runLater(() -> {
				//Create a transparent stage which will host the ControlFX push notification
				stage = new Stage(StageStyle.TRANSPARENT);
				StackPane pane = new StackPane();
				pane.setStyle("-fx-background-color: TRANSPARENT");
				Scene scene = new Scene(pane, 1, 1);
				scene.setFill(Color.TRANSPARENT);
				stage.setScene(scene);
				stage.setWidth(1);
				stage.setHeight(1);
				stage.toBack();
				stage.show();
				//Initialise image and audio for the Push Notification
	    		Image img = new Image("/application/PomodoroNotification.png");
	    		AudioClip audio = new AudioClip(this.getClass().getResource("/application/notification.wav").toString());
	    		audio.play();
	    		//Create ControlFX push notification instance and load it with initialised values
	    		Notifications notification = Notifications.create()
	    				.title(this.getHeader())
	    				.text(this.getBody())
	    				.graphic(new ImageView(img))
	    				.hideAfter(Duration.seconds(5))
	    				.position(Pos.BOTTOM_RIGHT);
	    		notification.show();
	    		//Create a timer instance which will monitor duration the Push Notification is run for
	    		//After 8secs transparent stage is closed and resources allocated to the Push Notification are cleared
	    		timer = new Timer();
	    		timer.schedule(new TimerTask() {
	    		        @Override
	    		        public void run() {
	    		        	//Create a Runnable thread: pushNotification, which will host the Push Notification automated exit routine
	    		        	pushNotification = new Thread(()-> {
	        		            Platform.runLater(() ->{
	        		            	stage.close();
	        		            });
	    		        	});
	    		        	pushNotification.setDaemon(true);
	    		        	pushNotification.start();
	    		    		for(int i = 0; i < 10; i++) {
	    						try {
	    							timer.cancel();
	    							timer.purge();
	    							pushNotification.join();
	    						} catch (InterruptedException e) {
	    							e.printStackTrace();
	    						}
	    		    		}
	    		        }
	    		    }, 8000);
			});
		}
		//Exit routine which will clear resources used in the running of the Push Notification
		public void notificationExitRoutine() {
			if (pushNotification !=null) {
				if (pushNotification.isAlive()!=false) {
					for(int i = 0; i < 10; i++) {
						try {
							timer.cancel();
							timer.purge();
							pushNotification.join();
							stage.close();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				else if (stage.isShowing())
					stage.close();
				else
					return;
			}
			else
				return;
		}
	}
}