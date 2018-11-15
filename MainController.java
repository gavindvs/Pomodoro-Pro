package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Pane;

public class MainController {
	
	@FXML private Pane timerCreator;
	@FXML private Pane timerMenu;
	@FXML private Button addTimer;
	@FXML private Spinner<Integer> timerHours;
	@FXML private Spinner<Integer> timerMins;
	
	
	public void openTimerCreator(ActionEvent event) {
		addTimer.setVisible(false);
		timerMenu.setVisible(false);
		timerCreator.setVisible(true);
		SpinnerValueFactory <Integer> hoursFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9, 0);
		SpinnerValueFactory <Integer> minsFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 55, 0, 5);
		timerHours.setValueFactory(hoursFactory);
		timerMins.setValueFactory(minsFactory);
	}
	public void closeTimerCreator(ActionEvent event) {
		timerCreator.setVisible(false);
		addTimer.setVisible(true);
		timerMenu.setVisible(true);
	}
}
