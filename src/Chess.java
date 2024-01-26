import game.Piece;
import gui.DialogPane;
import gui.GamePane;
import gui.OptionsPane;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Chess extends Application {
    private DialogPane dialogPane;
    private GamePane gamePane;

    private boolean debugMode;

    public void start(Stage stage) throws Exception {
        dialogPane = new DialogPane();
        gamePane = new GamePane();
        gamePane.move.addListener((observable, oldValue, newValue) -> {
            if (newValue && debugMode) {
                dialogPane.displayDebug(gamePane.getBoardState());
                gamePane.move.set(false);
            }
        });
        debugMode = false;

        OptionsPane optionsPane = new OptionsPane();
        optionsPane.reset.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                gamePane.resetBoard();
                if (debugMode) {
                    dialogPane.displayDebug(gamePane.getBoardState());
                } else {
                    dialogPane.displayMessage("Board has been reset!");
                }
                optionsPane.reset.set(false);
            }
        });

        optionsPane.debug.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                debugMode = !debugMode;
                if (debugMode) {
                    dialogPane.displayDebug(gamePane.getBoardState());
                } else {
                    dialogPane.displayMessage("Debug mode deactivated!");
                }

                optionsPane.debug.set(false);
            }
        });

        optionsPane.undo.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                gamePane.undoMove();
                if (debugMode) {
                    dialogPane.displayDebug(gamePane.getBoardState());
                } else {
                    dialogPane.displayMessage("Undoing move!");
                }
                optionsPane.undo.set(false);
            }
        });

        optionsPane.flip.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                gamePane.flipBoard();
                if (!debugMode) {
                    String per = (gamePane.getPerspective() == Piece.WHITE) ? "white" : "black";
                    dialogPane.displayMessage("You are now playing as " + per + "!");
                }
                optionsPane.flip.set(false);
            }
        });

        optionsPane.fen.addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(newValue, oldValue)) {
                if (newValue.equals("")) {
                    return;
                }

                if (newValue.length() < 15) {
                    dialogPane.displayMessage("Error: Not a valid FEN string!");
                    return;
                }

                gamePane.makeBoardFromFen(newValue);
                if (debugMode) {
                    dialogPane.displayDebug(gamePane.getBoardState());
                } else {
                    dialogPane.displayMessage("Board has been made from FEN string: " + newValue);
                }
                optionsPane.fen.set("");
            }
        });

        optionsPane.depth.addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(newValue, oldValue)) {
                if (newValue.equals("")) {
                    return;
                }

                try {
                    int depth = Integer.parseInt(newValue);
                    new Thread(() -> {
                        goPerft(depth);
                    }).start();
                } catch (NumberFormatException e) {
                    dialogPane.displayMessage("Error: Not a valid number!");
                }

                optionsPane.depth.set("");
            }
        });

        HBox hBox = new HBox();
        hBox.setStyle("-fx-background-color: black");
        hBox.setSpacing(1);

        hBox.getChildren().add(optionsPane);
        hBox.getChildren().add(gamePane);
        hBox.getChildren().add(dialogPane);

        Scene scene = new Scene(hBox);
        scene.getStylesheets().add(getClass().getResource("gui/style.css").toExternalForm());

        stage.setTitle("Custom Pane Example");
        stage.setScene(scene);
        stage.show();
    }

    private void goPerft(int depth) {
        try {
            List<String> results = gamePane.goPerft(depth);
            Platform.runLater(() -> {
                dialogPane.displayPerftResults(results);
            });
        } catch (IOException | InterruptedException e) {
            Platform.runLater(() -> {
                dialogPane.displayMessage("Perft error: " + e.getMessage());
            });
        }
    }
}
