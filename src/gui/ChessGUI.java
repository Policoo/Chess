package gui;

import game.Board;
import game.Move;
import engines.Counter;
import engines.Engine;
import engines.Random;
import engines.Greedy;
import game.Piece;
import utils.MoveGenerator;
import utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ChessGUI extends JFrame implements MouseListener {
    private final JPanel panel;
    private final JLabel[] tileList = new JLabel[64];
    private final List<ImageIcon> piecesImages = new ArrayList<>();
    private int lastClicked;
    private List<Move> legalMoves = new ArrayList<>();
    private final Board board;
    private Engine engine;

    public ChessGUI() {
        MoveGenerator.initialize();
        this.setTitle("ChessGUI");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(527, 550);
        this.setLayout(null);
        this.setResizable(false);
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(0, 0, 550, 550);
        board = new Board();
        fillPiecesImagesList();
        generateBoard();
        addPiecesToBoard();
        this.add(panel);
        panel.addMouseListener(this);
        this.setVisible(true);
        legalMoves = MoveGenerator.generateMoves(board);
        lastClicked = -1;
    }

    public ChessGUI(String config) {
        MoveGenerator.initialize();
        this.setTitle("ChessGUI");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(527, 550);
        this.setLayout(null);
        this.setResizable(false);
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(0, 0, 550, 550);
        board = new Board();
        fillPiecesImagesList();
        generateBoard();
        addPiecesToBoard();
        this.add(panel);
        panel.addMouseListener(this);
        this.setVisible(true);
        initializeEngine(config);
        if (engine.isWhite()) {
            makeEngineMove();
        }
        lastClicked = -1;
    }

    public ChessGUI(String fenString, String config) {
        MoveGenerator.initialize();
        this.setTitle("ChessGUI");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(527, 550);
        this.setLayout(null);
        this.setResizable(false);
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(0, 0, 550, 550);
        board = new Board(fenString);
        fillPiecesImagesList();
        generateBoard();
        addPiecesToBoard();
        this.add(panel);
        panel.addMouseListener(this);
        this.setVisible(true);
        initializeEngine(config);
        if (engine.isWhite()) {
            makeEngineMove();
        }
        lastClicked = -1;
    }

    public ChessGUI(String fenString, boolean yes) {
        MoveGenerator.initialize();
        this.setTitle("ChessGUI");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(527, 550);
        this.setLayout(null);
        this.setResizable(false);
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(0, 0, 550, 550);
        board = new Board(fenString);
        fillPiecesImagesList();
        generateBoard();
        addPiecesToBoard();
        this.add(panel);
        panel.addMouseListener(this);
        this.setVisible(true);
        legalMoves = MoveGenerator.generateMoves(board);
        lastClicked = -1;
    }

    private void initializeEngine(String config) {
        switch (config) {
            case "random":
                this.engine = new Random("b");
                break;
            case "counter":
                this.engine = new Counter("w");
                break;
            default:
                this.engine = new Greedy("b");
        }
    }

    private void addPiecesToBoard() {
        int piece;
        for (int index = 0; index < 64; index++) {
            if (board.isEmptyTile(index)) {
                continue;
            }

            piece = determinePieceNumber(index);
            tileList[index].setIcon(piecesImages.get(piece));
        }
    }

    private int determinePieceNumber(int index) {
        int piece;
        switch (board.getPieceType(index)) {
            case Piece.KING:
                piece = 0;
                break;
            case Piece.QUEEN:
                piece = 1;
                break;
            case Piece.BISHOP:
                piece = 2;
                break;
            case Piece.KNIGHT:
                piece = 3;
                break;
            case Piece.ROOK:
                piece = 4;
                break;
            default:
                piece = 5;
        }
        if (board.isColor(index, Piece.BLACK)) {
            piece += 6;
        }
        return piece;
    }

    private void generateBoard() {
        JLabel jLabel;
        boolean white = true;
        for (int index = 0; index < 64; index++) {
            jLabel = new JLabel();
            if (white) {
                jLabel.setBackground(new Color(240, 217, 181));
            } else {
                jLabel.setBackground(new Color(181, 136, 99));
            }
            jLabel.setOpaque(true);
            jLabel.setHorizontalAlignment(JLabel.CENTER);
            jLabel.setBounds((index % 8) * 64, (index / 8) * 64, 64, 64);
            panel.add(jLabel);
            tileList[index] = jLabel;
            if (index % 8 != 7) {
                white = !white;
            }
        }
    }

    private void resetColors() {
        boolean white = true;
        for (int index = 0; index < 64; index++) {
            if (white) {
                tileList[index].setBackground(new Color(240, 217, 181));
            } else {
                tileList[index].setBackground(new Color(181, 136, 99));
            }

            if (index % 8 != 7) {
                white = !white;
            }
        }
    }

    private void showLegalMoves(int index) throws NullPointerException {
        if (legalMoves.size() == 0) {
            return;
        }

        for (Move move : legalMoves) {
            if (move.start() != index) {
                continue;
            }

            if ((move.end() / 8) % 2 == 0) {
                if ((move.end() % 8) % 2 == 0) {
                    tileList[move.end()].setBackground(new Color(248, 109, 91));
                } else {
                    tileList[move.end()].setBackground(new Color(218, 68, 50));
                }
            } else {
                if ((move.end() % 8) % 2 == 0) {
                    tileList[move.end()].setBackground(new Color(218, 68, 50));
                } else {
                    tileList[move.end()].setBackground(new Color(248, 109, 91));
                }
            }
        }
    }

    private void highlightClickedTile(int index) {
        if (!board.isEmptyTile(index)) {
            tileList[index].setBackground(new Color(51, 89, 128));
        }
    }

    private void compareCountResults() {
        String[] results = engine.getResults().split("`");
        System.out.println("Enter StockFish results");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        List<String[]> stockFishResults = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    // Exit the loop if the input is empty (just Enter was pressed)
                    break;
                }
                stockFishResults.add(line.split("\\s"));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean matching = true;
        for (String result : results) {
            boolean found = false;
            String[] splitResult = result.split(":");
            for (String[] stockFishResult : stockFishResults) {
                String resultPosition = splitResult[0] + ":";
                if (!resultPosition.equals(stockFishResult[0])) {
                    continue;
                }
                found = true;
                if (Integer.parseInt(stockFishResult[1]) == Integer.parseInt(splitResult[1])) {
                    System.out.println(splitResult[0] + " is matching with " + splitResult[1] + " positions");
                    break;
                }
                matching = false;
                System.out.print(splitResult[0] + " is not matching, ");
                if (Integer.parseInt(stockFishResult[1]) > Integer.parseInt(splitResult[1])) {
                    System.out.println("StockFish found " + Math.abs(Integer.parseInt(stockFishResult[1]) - Integer.parseInt(splitResult[1])) + " extra move(s)");
                    break;
                }
                System.out.println("StockFish found " + Math.abs(Integer.parseInt(stockFishResult[1]) - Integer.parseInt(splitResult[1])) + " fewer move(s)");
                break;
            }
            if (!found) {
                System.out.println(splitResult[0] + " is in StockFish results, but not in counter");
                matching = false;
            }
        }
        if (matching) {
            System.out.println("Results are correct");
        }
    }

    private void makeMove(Move move) {
        board.makeMove(move);
        for (int index = 0; index < 64; index++) {
            tileList[index].setIcon(null);
            tileList[index].revalidate();
        }
        addPiecesToBoard();

        if (board.getGameOver()) {
            System.out.println("game over");
            return;
        }

        if (engine != null) {
            makeEngineMove();
        }

        //generate new legal moves
        legalMoves = MoveGenerator.generateMoves(board);
    }

    private void makeEngineMove() {
        Move move = engine.determineMove(board);
        if (engine.getName().equals("Counter")) {
            compareCountResults();
            return;
        }

        makeMove(move);
    }

    private void manageClick(int index) {
        //if this is the first time we click on a piece
        if (lastClicked == -1) {
            resetColors();
            try {
                highlightClickedTile(index);
                showLegalMoves(index);
            } catch (NullPointerException e) {
                resetColors();
                if (!board.isEmptyTile(index)) {
                    highlightClickedTile(index);
                }
                return;
            }
            if (legalMoves.size() > 0) {
                lastClicked = index;
            }
            return;
        }

        //if we have selected a piece and clicked another tile
        for (Move move : legalMoves) {
            if (move.start() == lastClicked && move.end() == index) {
                if (move.flag() == Move.PROMOTION) {
                    System.out.println("Enter what to promote to: ");
                    Scanner keyboard = new Scanner(System.in);
                    String typeInput = keyboard.nextLine();
                    while (!typeInput.equals("q") && !typeInput.equals("r") && !typeInput.equals("b") && !typeInput.equals("n")) {
                        typeInput = keyboard.nextLine();
                    }
                    switch (typeInput) {
                        case "q":
                            move.setPromotion(Piece.QUEEN);
                            break;
                        case "r":
                            move.setPromotion(Piece.ROOK);
                            break;
                        case "n":
                            move.setPromotion(Piece.KNIGHT);
                            break;
                        case "b":
                            move.setPromotion(Piece.BISHOP);
                            break;
                    }
                }
                makeMove(move);
                break;
            }
        }

        lastClicked = -1;
        resetColors();
    }

    private void fillPiecesImagesList() {
        BufferedImage all = null;
        try {
            all = ImageIO.read(new File("src/pieces/chess pieces.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int y = 0; y < 400; y += 200) {
            for (int x = 0; x < 1200; x += 200) {
                assert all != null;
                piecesImages.add(new ImageIcon(all.getSubimage(x, y, 200, 200).getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH)));
            }
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        int[] realCoordinates = Utils.getXYCoordinatesFromClick(e.getX(), e.getY());
        manageClick(Utils.XYToIndex(realCoordinates[0], realCoordinates[1]));
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
