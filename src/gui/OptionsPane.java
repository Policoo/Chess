package gui;

import engines.Engine;
import engines.Greedy;
import engines.Random;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class OptionsPane extends VBox {
    private final Engine[] availableEngines;

    private TextField fenInput;
    private TextField depthInput;
    private ComboBox<Engine> opponentChoice;
    private ComboBox<Engine> engine1Choice;
    private ComboBox<Engine> engine2Choice;

    public final BooleanProperty reset;
    public final BooleanProperty debug;
    public final BooleanProperty undo;
    public final BooleanProperty flip;
    public final StringProperty fen;
    public final StringProperty depth;

    public final ObjectProperty<Engine> opponent;
    public final ObjectProperty<Engine> engine1;
    public final ObjectProperty<Engine> engine2;

    public OptionsPane() {
        setPrefSize(300, 552);
        setStyle("-fx-background-color: BLACK;");
        setSpacing(1);

        availableEngines = new Engine[]{new Random("b"), new Greedy("b")};
        reset = new SimpleBooleanProperty(false);
        debug = new SimpleBooleanProperty(false);
        undo = new SimpleBooleanProperty(false);
        flip = new SimpleBooleanProperty(false);
        fen = new SimpleStringProperty("");
        depth = new SimpleStringProperty("");
        opponent = new SimpleObjectProperty<>(null);
        engine1 = new SimpleObjectProperty<>(null);
        engine2 = new SimpleObjectProperty<>(null);

        try {
            constructTopButtons();
        } catch (FileNotFoundException e) {
            System.out.println("Error: Flip board image was not found!");
            System.exit(1);
        }
        constructFenPane();
        constructPerftPane();
        constructOpponentPane();
        constructMatchPane();

        Pane placeHolder = new Pane();
        placeHolder.setStyle("-fx-background-color: #3d3d3d");
        placeHolder.setPrefSize(500, 300);
        getChildren().add(placeHolder);
    }

    private void constructTopButtons() throws FileNotFoundException {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.setPrefSize(299, 40);
        container.setStyle("-fx-background-color: #3d3d3d; -fx-padding: 5px");

        Pane spacer1 = new Pane();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Pane spacer2 = new Pane();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Pane spacer3 = new Pane();
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        Button resetButton = new Button("Reset");
        resetButton.setPrefSize(50, 25);
        resetButton.getStyleClass().add("std-button");
        resetButton.setOnAction(event -> reset.set(true));

        Button debugButton = new Button("Debug");
        debugButton.setPrefSize(50, 25);
        debugButton.getStyleClass().add("std-button");
        debugButton.setOnAction(event -> {
            for (int s = 0; s < debugButton.getStyleClass().size(); s++) {
                if (debugButton.getStyleClass().get(s).contains("std-button")) {
                    debugButton.getStyleClass().set(s, "debug-button-active");
                    break;
                }

                if (debugButton.getStyleClass().get(s).contains("debug-button-active")) {
                    debugButton.getStyleClass().set(s, "std-button");
                    break;
                }
            }
            debug.set(true);
        });

        Button undoButton = new Button("Undo");
        undoButton.setPrefSize(50, 25);
        undoButton.getStyleClass().add("std-button");
        undoButton.setOnAction(event -> undo.set(true));

        Button flipButton = new Button();
        flipButton.setPrefSize(50, 25);
        flipButton.getStyleClass().add("std-button");
        flipButton.getStyleClass().add("flip");
        flipButton.setOnAction(event -> flip.set(true));

        Image flipImage = new Image(new FileInputStream("src/gui/resources/flip board.png"));
        ImageView flipImageView = new ImageView(flipImage);
        flipImageView.setFitWidth(20);
        flipImageView.setFitHeight(20);
        flipButton.setGraphic(flipImageView);

        container.getChildren().addAll(resetButton, spacer1, debugButton, spacer2, undoButton, spacer3, flipButton);
        getChildren().add(container);
    }

    private void constructFenPane() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setPrefSize(299, 90);
        container.setSpacing(3);
        container.setStyle("-fx-background-color: #3d3d3d; -fx-padding: 5px");

        Label label = new Label("Make board from FEN string");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 13 ;-fx-font-weight: bold;");
        container.getChildren().add(label);

        fenInput = new TextField();
        fenInput.setMaxWidth(288);
        container.getChildren().add(fenInput);

        Button confirmButton = new Button("Confirm");
        confirmButton.setPrefSize(290, 17);
        confirmButton.getStyleClass().add("std-button");
        confirmButton.getStyleClass().add("flip");
        confirmButton.setOnAction(actionEvent -> {
            fen.set(fenInput.getText());
            fenInput.setText("");
        });

        container.getChildren().add(confirmButton);

        getChildren().add(container);
    }

    private void constructPerftPane() {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefSize(299, 40);
        container.setStyle("-fx-background-color: #3d3d3d; -fx-padding: 5px");

        Label label = new Label("Depth: ");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 13 ;-fx-font-weight: bold;");
        container.getChildren().add(label);

        depthInput = new TextField();
        depthInput.setMaxWidth(30);
        container.getChildren().add(depthInput);

        Region spacer = new Region();
        spacer.setPrefSize(10, 30);
        container.getChildren().add(spacer);

        Button goPerftButton = new Button("Go Perft");
        goPerftButton.setPrefSize(70, 25);
        goPerftButton.getStyleClass().add("std-button");
        goPerftButton.getStyleClass().add("flip");
        goPerftButton.setOnAction(actionEvent -> {
            depth.set(depthInput.getText());
            depthInput.setText("");
        });

        container.getChildren().add(goPerftButton);
        getChildren().add(container);
    }

    private void constructOpponentPane() {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefSize(299, 40);
        container.setStyle("-fx-background-color: #3d3d3d; -fx-padding: 5px");

        Label label = new Label("Opponent: ");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 13 ;-fx-font-weight: bold;");
        container.getChildren().add(label);

        opponentChoice = new ComboBox<>();
        opponentChoice.setPrefSize(120, 25);
        opponentChoice.getItems().add(null);
        opponentChoice.getItems().addAll(availableEngines);
        opponentChoice.setOnAction(actionEvent -> opponent.set(opponentChoice.getValue()));

        container.getChildren().add(opponentChoice);

        getChildren().add(container);
    }

    private void constructMatchPane() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setPrefSize(299, 90);
        container.setStyle("-fx-background-color: #3d3d3d; -fx-padding: 5px");

        Label label = new Label("Set up engine battle");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 13 ;-fx-font-weight: bold;");
        container.getChildren().add(label);

        HBox enginesContainer = new HBox();
        enginesContainer.setAlignment(Pos.CENTER);
        enginesContainer.setSpacing(11);
        enginesContainer.setPrefSize(299, 90);
        enginesContainer.setStyle("-fx-background-color: #3d3d3d; -fx-padding: 5px");

        engine1Choice = new ComboBox<>();
        engine1Choice.setPrefSize(120, 25);
        engine1Choice.getItems().addAll(availableEngines);
        enginesContainer.getChildren().add(engine1Choice);

        Label vs = new Label("vs.");
        vs.setStyle("-fx-text-fill: white; -fx-font-size: 13 ;-fx-font-weight: bold;");
        enginesContainer.getChildren().add(vs);

        engine2Choice = new ComboBox<>();
        engine2Choice.setPrefSize(120, 25);
        engine2Choice.getItems().addAll(availableEngines);
        enginesContainer.getChildren().add(engine2Choice);

        container.getChildren().add(enginesContainer);

        Image image = null;
        try {
            image = new Image(new FileInputStream("src/gui/resources/swords.png"));
        } catch (FileNotFoundException e) {
            System.out.println("Error: Swords image was not found!");
            System.exit(1);
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(17);
        imageView.setFitHeight(17);

        Label startButton = new Label();
        startButton.getStyleClass().add("std-button");
        startButton.getStyleClass().add("start-engine-match");
        startButton.setOnMouseClicked(mouseEvent -> {
            engine1.set(engine1Choice.getValue());
            engine2.set(engine2Choice.getValue());
        });

        Label startText = new Label("Start");
        startText.setStyle("-fx-text-fill: black; -fx-font-size: 13 ;-fx-font-weight: bold;");

        HBox graphicContainer = new HBox();
        graphicContainer.setSpacing(5);
        graphicContainer.setPrefSize(150, 25);
        graphicContainer.setAlignment(Pos.CENTER);
        graphicContainer.getChildren().addAll(startText, imageView);
        graphicContainer.setStyle("-fx-padding: 0px");
        startButton.setGraphic(graphicContainer);

        container.getChildren().add(startButton);

        getChildren().add(container);
    }
}
