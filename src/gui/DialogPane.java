package gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

public class DialogPane extends VBox {
    private final VBox content;

    public DialogPane() {
        setPrefSize(300, 552);
        setStyle("-fx-background-color: BLACK;");
        setSpacing(1);

        HBox textContainer = new HBox();
        textContainer.setAlignment(Pos.CENTER);
        textContainer.setPrefSize(299, 40);
        textContainer.setStyle("-fx-background-color: #3d3d3d; -fx-padding: 5px");

        Label desc = new Label("Dialog box");
        desc.setStyle("-fx-text-fill: white; -fx-font-size: 18 ;-fx-font-weight: bold;");
        textContainer.getChildren().add(desc);

        getChildren().add(textContainer);

        content = new VBox();
        content.setPrefSize(299, 512);
        content.setSpacing(3);
        content.setStyle("-fx-background-color: #3d3d3d; -fx-padding: 3px");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        getChildren().add(scrollPane);
    }

    public void displayDebug(String debugInfo) {
        content.getChildren().clear();

        Text messageLabel = new Text(debugInfo);
        messageLabel.getStyleClass().add("debug-text");

        content.getChildren().add(messageLabel);
    }

    public void displayMessage(String message) {
        content.getChildren().clear();

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(280);
        messageLabel.getStyleClass().add("regular-text");

        content.getChildren().add(messageLabel);
    }

    public void displayPerftResults(List<String> results) {
        content.getChildren().clear();

        Label depthInfo = new Label(results.get(0));
        depthInfo.setStyle("-fx-text-fill: white;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-border-width: 0 0 0 1px;" +
                "-fx-border-color: black;" +
                "-fx-padding: 3px;"
        );
        content.getChildren().add(depthInfo);

        for (int index = 1; index < results.size(); index++) {
            Label messageLabel = new Label(results.get(index));
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(290);
            messageLabel.getStyleClass().add("regular-text");
            content.getChildren().add(messageLabel);
        }
    }
}
