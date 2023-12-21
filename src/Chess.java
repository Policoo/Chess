import gui.GamePane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Chess extends Application {
    public void start(Stage stage) throws Exception {
        GamePane coloredPane = new GamePane();

        Button button1 = new Button("Click me!");
        Button button2 = new Button("Press me!");

        VBox vbox = new VBox(button1, button2);
        coloredPane.getChildren().add(vbox);

        Scene scene = new Scene(coloredPane, 300, 200);

        stage.setTitle("Custom Pane Example");
        stage.setScene(scene);
        stage.show();
    }
}
