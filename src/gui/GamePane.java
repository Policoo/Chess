package gui;

import engines.Counter;
import engines.Engine;
import game.Board;
import game.Move;
import game.Piece;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import utils.MoveGenerator;
import utils.StockFish;
import utils.Zobrist;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class GamePane extends BorderPane {
    private final VBox leftPane;
    private final HBox bottomPane;

    private final Label[] tile;
    private List<ImageView> piecesImages;

    private Board board;
    private Engine engine;

    private List<Move> legalMoves;
    private final List<Integer> pieceMoves;
    private final List<Integer> promotionTiles;
    private Move promotionMove;
    private int lastClick;
    private int perspective;

    // <--> INITIALIZATION <--> //

    public GamePane() {
        setPrefSize(552, 552);

        Pane topPane = new Pane();
        topPane.setPrefSize(552, 20);
        topPane.setStyle("-fx-background-color: #363636;");

        Pane rightPane = new Pane();
        rightPane.setPrefSize(20, 552);
        rightPane.setStyle("-fx-background-color: #363636;");

        bottomPane = new HBox();
        bottomPane.setPrefSize(552, 20);
        bottomPane.setStyle("-fx-background-color: #363636;");

        leftPane = new VBox();
        leftPane.setPrefSize(20, 552);
        leftPane.setStyle("-fx-background-color: #363636;");

        GridPane middlePane = new GridPane(8, 8);
        middlePane.setPrefSize(512, 512);
        middlePane.setHgap(0);
        middlePane.setVgap(0);

        tile = new Label[64];
        for (int index = 0; index < 64; index++) {
            tile[index] = new Label();
            tile[index].setPrefSize(64, 64);

            middlePane.add(tile[index], index % 8, index / 8);
            int finalIndex = index;
            tile[index].setOnMousePressed(event -> handleClick(finalIndex));
        }

        setTop(topPane);
        setRight(rightPane);
        setBottom(bottomPane);
        setLeft(leftPane);
        setCenter(middlePane);

        perspective = Piece.WHITE;
        addCoordinates();
        resetColors();
        try {
            fillPieceImages();
        } catch (FileNotFoundException e) {
            System.out.println("Error: Chess pieces image was not found!");
            System.exit(1);
        }

        Zobrist.initialize();
        MoveGenerator.initialize();

        board = new Board();
        updateBoard();

        pieceMoves = new ArrayList<>();
        promotionTiles = new ArrayList<>();
        legalMoves = MoveGenerator.generateMoves(board);
    }

    private void fillPieceImages() throws FileNotFoundException {
        piecesImages = new ArrayList<>();
        Image allPiecesImage = new Image(new FileInputStream("src/images/pieces.png"));
        PixelReader pixelReader = allPiecesImage.getPixelReader();

        for (int y = 0; y < allPiecesImage.getHeight(); y += 200) {
            for (int x = 0; x < allPiecesImage.getWidth(); x += 200) {
                ImageView pieceImageView = new ImageView();
                pieceImageView.setImage(new WritableImage(pixelReader, x, y, 200, 200));
                piecesImages.add(pieceImageView);
            }
        }
    }

    private void addCoordinates() {
        leftPane.getChildren().clear();
        bottomPane.getChildren().clear();

        Integer[] numbers = {8, 7, 6, 5, 4, 3, 2, 1};
        Character[] letters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'};

        if (perspective == Piece.BLACK) {
            List<Integer> numbersList = Arrays.asList(numbers);
            Collections.reverse(numbersList);
            numbers = numbersList.toArray(new Integer[0]);

            List<Character> lettersList = Arrays.asList(letters);
            Collections.reverse(lettersList);
            letters = lettersList.toArray(new Character[0]);
        }

        Region spacer = new Region();
        spacer.setPrefSize(20, 20);
        bottomPane.getChildren().add(spacer);

        for (int index = 0; index < 8; index++) {
            Label number = new Label();
            number.setPrefSize(20, 64);
            number.setAlignment(Pos.CENTER);
            number.setText(String.valueOf(numbers[index]));
            number.setFont(Font.font("Impact", 15));
            number.setStyle("-fx-text-fill: white;");
            leftPane.getChildren().add(number);

            Label letter = new Label();
            letter.setPrefSize(64, 20);
            letter.setAlignment(Pos.CENTER);
            letter.setText(String.valueOf(letters[index]));
            letter.setFont(Font.font("Impact", 15));
            letter.setStyle("-fx-text-fill: white;");
            bottomPane.getChildren().add(letter);
        }
    }

    private void updateBoard() {
        for (int index = 0; index < 64; index++) {
            int perIndex = (perspective == Piece.WHITE) ? index : 63 - index;

            if (board.isEmpty(perIndex)) {
                tile[index].setGraphic(null);
                continue;
            }

            int pieceIndex = 0;
            switch (board.getPieceType(perIndex)) {
                case Piece.QUEEN:
                    pieceIndex = 1;
                    break;
                case Piece.BISHOP:
                    pieceIndex = 2;
                    break;
                case Piece.KNIGHT:
                    pieceIndex = 3;
                    break;
                case Piece.ROOK:
                    pieceIndex = 4;
                    break;
                case Piece.PAWN:
                    pieceIndex = 5;
                    break;
            }

            pieceIndex = (board.getPieceColor(perIndex) == Piece.BLACK) ? pieceIndex + 6 : pieceIndex;
            ImageView imageView = new ImageView(piecesImages.get(pieceIndex).getImage());
            imageView.setFitWidth(64);
            imageView.setFitHeight(64);
            tile[index].setGraphic(imageView);
        }
    }

    // <--> INITIALIZATION <--> //

    // <--> COLORING <--> //

    private void resetColors() {
        for (int index = 0; index < 64; index++) {
            if (((index % 8) + (index / 8)) % 2 == 0) {
                tile[index].setStyle("-fx-background-color: #ebd5b1;");
            } else {
                tile[index].setStyle("-fx-background-color: #b48662;");
            }
        }
    }

    private void highlightClick(int index) {
        int perIndex = (perspective == Piece.WHITE) ? index : 63 - index;
        if (board.isEmpty(perIndex)) {
            return;
        }

        if (((index % 8) + (index / 8)) % 2 == 0) {
            tile[index].setStyle("-fx-background-color: #f4e979;");
        } else {
            tile[index].setStyle("-fx-background-color: #d9c252;");
        }
    }

    private void highlightMove(Move move) {
        int start = (perspective == Piece.WHITE) ? move.start() : 63 - move.start();
        int end = (perspective == Piece.WHITE) ? move.end(): 63 - move.end();

        if (((start % 8) + (start / 8)) % 2 == 0) {
            tile[start].setStyle("-fx-background-color: #f4e979;");
        } else {
            tile[start].setStyle("-fx-background-color: #d9c252;");
        }

        if (((end % 8) + (end / 8)) % 2 == 0) {
            tile[end].setStyle("-fx-background-color: #f4e979;");
        } else {
            tile[end].setStyle("-fx-background-color: #d9c252;");
        }
    }

    private void showLegalMoves(int index) {
        for (Move move: legalMoves) {
            if (move.start() != index) {
                continue;
            }

            int end = (perspective == Piece.WHITE) ? move.end() : 63 - move.end();
            if (((end % 8) + (end / 8)) % 2 == 0) {
                tile[end].setStyle("-fx-background-color: #f86d5b");
            } else {
                tile[end].setStyle("-fx-background-color: #da4432");
            }
        }
    }

    private void showPromotionOptions(int index) {
        List<ImageView> promotionPieces = new ArrayList<>();
        if (index < 8) {
            promotionPieces.add(piecesImages.get(1));
            promotionPieces.add(piecesImages.get(4));
            promotionPieces.add(piecesImages.get(2));
            promotionPieces.add(piecesImages.get(3));
        } else {
            promotionPieces.add(piecesImages.get(7));
            promotionPieces.add(piecesImages.get(10));
            promotionPieces.add(piecesImages.get(8));
            promotionPieces.add(piecesImages.get(9));
        }

        int direction;
        if (perspective == Piece.WHITE) {
            direction = (index < 8) ? 8 : -8;
        } else {
            direction = (index < 8) ? -8 : 8;
            index = 63 - index;
        }

        for (int step = 0; step < 4; step++) {
            int curIndex = index + (direction * step);
            tile[curIndex].setStyle("-fx-background-color: white;");

            promotionPieces.get(step).setFitWidth(64);
            promotionPieces.get(step).setFitHeight(64);
            tile[curIndex].setGraphic(promotionPieces.get(step));

            promotionTiles.add(curIndex);
        }
    }

    // <--> COLORING <--> //

    // <--> CLICKING, MAKING MOVES AND BUTTON FUNCTIONS <--> //

    private void handleClick(int index) {
        //special case for promotions
        if (!promotionTiles.isEmpty()) {
            //if we didn't promote, but clicked outside the options
            if (!promotionTiles.contains(index)){
                promotionTiles.clear();
                resetColors();
                updateBoard();
                return;
            }

            if (index / 8 == 0 || index / 8 == 7) {
                promotionMove.setPromotion(Piece.QUEEN);
            }

            if (index / 8 == 1 || index / 8 == 6) {
                promotionMove.setPromotion(Piece.ROOK);
            }

            if (index / 8 == 2 || index / 8 == 5) {
                promotionMove.setPromotion(Piece.BISHOP);
            }

            if (index / 8 == 3 || index / 8 == 4) {
                promotionMove.setPromotion(Piece.KNIGHT);
            }

            makeMove(promotionMove);
            promotionTiles.clear();
            resetColors();
            highlightMove(promotionMove);
            return;
        }

        //variables that have been edited to reflect the perspective of the board
        int perIndex = (perspective == Piece.WHITE) ? index : 63 - index;
        int perLastClick = (perspective == Piece.WHITE) ? lastClick : 63 - lastClick;

        //if this is a legal move
        if (pieceMoves.contains(perIndex)){
            for (Move move : legalMoves) {
                if (move.start() == perLastClick && move.end() == perIndex){
                    if (move.flag() == Move.PROMOTION){
                        showPromotionOptions(move.end());
                        promotionMove = move;
                        return;
                    }

                    makeMove(move);
                    resetColors();
                    highlightMove(move);
                    break;
                }
            }

            lastClick = -1;
            pieceMoves.clear();
            return;
        }

        //if we clicked on an empty tile that is not a legal move
        if (board.isEmpty(perIndex)) {
            resetColors();
            lastClick = -1;
            pieceMoves.clear();
            return;
        }

        //if we click on a piece whose turn it is
        if (board.isColor(perIndex, board.getTurn())) {
            resetColors();
            highlightClick(index);
            showLegalMoves(perIndex);

            //add all possible end squares of piece to vector
            if (!legalMoves.isEmpty()) {
                lastClick = index;
                pieceMoves.clear();
                for (Move move : legalMoves) {
                    if (move.start() == perIndex){
                        pieceMoves.add(move.end());
                    }
                }
            }
        } else {
            //if we get here that means the user clicked on a tile that is not a move, but an opponents tile
            resetColors();
            highlightClick(index);
            pieceMoves.clear();
            lastClick = -1;
        }
    }

    private void makeMove(Move move) {
        board.makeMove(move);
        updateBoard();

        if (board.isGameOver()) {
            System.out.println("game over");
            return;
        }

        if (engine != null) {
            move = engine.determineMove(board);
            makeMove(move);
        }

        //generate new legal moves
        legalMoves = MoveGenerator.generateMoves(board);
    }

    public void flipBoard() {
        perspective = (perspective == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        updateBoard();
        resetColors();
        addCoordinates();
    }

    public void resetBoard() {
        board = new Board();
        legalMoves = MoveGenerator.generateMoves(board);
        resetColors();
        updateBoard();
    }

    public void makeBoardFromFen(String fenString) {
        board = new Board(fenString);
        legalMoves = MoveGenerator.generateMoves(board);
        resetColors();
        updateBoard();
    }

    public List<String> goPerft(int depth) throws IOException, InterruptedException {
        List<String> results = new ArrayList<>();
        results.add("Depth " + depth);
        Counter counter = new Counter();

        System.out.println(board.positionToFen());
        Map<String, Integer> counterResults = counter.goPerft(board.positionToFen(), depth);
        HashMap<String, Integer> fishResults = StockFish.goPerft(board.positionToFen(), depth);

        Iterator<Map.Entry<String, Integer>> iterator = counterResults.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String move = entry.getKey();
            int count = entry.getValue();

            if (!fishResults.containsKey(move)) {
                continue;
            }

            if (count == fishResults.get(move)) {
                results.add(move + ": " + count);
            } else {
                results.add(move + ": " + count + ", StockFish: " + fishResults.get(move));
            }

            iterator.remove();
            fishResults.remove(move);
        }

        if (counterResults.size() > 0) {
            StringBuilder line = new StringBuilder("Illegal moves found: ");
            for (String move : counterResults.keySet()) {
                line.append(move).append(", ");
            }

            results.add(line.toString());
        }

        if (fishResults.size() > 0) {
            StringBuilder line = new StringBuilder("Missed moves: ");
            for (String move : fishResults.keySet()) {
                line.append(move).append(", ");
            }

            results.add(line.toString());
        }

        return results;
    }

    public void setOpponent(Engine engine) {
        this.engine = engine;

    }

    // <--> CLICKING, MAKING MOVES AND BUTTON FUNCTIONS <--> //

    public int getPerspective() {
        return perspective;
    }
}
