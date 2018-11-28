package application;
	
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;


public class Main extends Application {
	private double xOffset = 0; 
	private double yOffset = 0;
	
	@Override
	public void init() throws Exception {
		//Do Nothing
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			//Setup JavaFx project with loading the .fxml file and setting up the stage
			Parent root = FXMLLoader.load(getClass().getResource("/application/Main.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());		
			primaryStage.setScene(scene);
			scene.setFill(Color.TRANSPARENT);
			//Make stage borderless
			primaryStage.initStyle(StageStyle.TRANSPARENT);
			primaryStage.setTitle("Pomodoro Timer");
			primaryStage.getIcons().add(new Image("/application/Pomodoro.png"));
			//Create window's event handler for onMousePressed/onMouseDragged events
			//Allows the user to move the borderless window
			root.setOnMousePressed(new EventHandler<MouseEvent>() {
				//Get scene's X and Y value upon mouse pressed and load it into xOffset and yOffset respectively
	            @Override
	            public void handle(MouseEvent event) {
	                xOffset = event.getSceneX();
	                yOffset = event.getSceneY();
	            }
	        });
			root.setOnMouseDragged(new EventHandler<MouseEvent>() {
				//Drag stage to the current location of the mouse's X and Y value
				//Achieved by passing the difference between the mouse's current X, Y value and xOffset, yOffset
	            @Override
	            public void handle(MouseEvent event) {
	                primaryStage.setX(event.getScreenX() - xOffset);
	                primaryStage.setY(event.getScreenY() - yOffset);
	            }
	        });
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		launch(args);
	}
}