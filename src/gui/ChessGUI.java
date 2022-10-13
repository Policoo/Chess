package gui;

import board.Board;
import board.Move;
import engines.Counter;
import engines.Engine;
import engines.Random;
import engines.ThinkerUnoptimized;
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
import java.util.List;
import java.util.Scanner;

public class ChessGUI extends JFrame implements MouseListener {
    private final JPanel panel;
    private final JLabel[][] tileList = new JLabel[8][8];
    private final List<ImageIcon> piecesImages = new ArrayList<>();
    private final List<Integer> legalMoves = new ArrayList<>();
    private final List<Integer> lastClicked = new ArrayList<>();
    private final Board board;
    private Engine engine;

    public ChessGUI() {
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
    }

    public ChessGUI(String config) {
        initializeEngine(config);
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
        if (engine.isWhite()) {
            makeEngineMove();
        }
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
                this.engine = new ThinkerUnoptimized("b");
        }
    }

    private void addPiecesToBoard() {
        int piece;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (board.getTile(x, y) == null) {
                    continue;
                }
                piece = determinePieceNumber(x, y);
                tileList[x][y].setIcon(piecesImages.get(piece));
            }
        }
    }

    private int determinePieceNumber(int x, int y) {
        int piece;
        switch (board.getTile(x, y).getType()) {
            case "k":
                piece = 0;
                break;
            case "q":
                piece = 1;
                break;
            case "b":
                piece = 2;
                break;
            case "n":
                piece = 3;
                break;
            case "r":
                piece = 4;
                break;
            default:
                piece = 5;
        }
        if (board.getTile(x, y).isColor("b")) {
            piece += 6;
        }
        return piece;
    }

    private int determinePieceNumber(int x, int y, String type) {
        int piece;
        switch (type) {
            case "k":
                piece = 0;
                break;
            case "q":
                piece = 1;
                break;
            case "b":
                piece = 2;
                break;
            case "n":
                piece = 3;
                break;
            case "r":
                piece = 4;
                break;
            default:
                piece = 5;
        }
        if (board.getTile(x, y).isColor("b")) {
            piece += 6;
        }
        return piece;
    }

    private void generateBoard() {
        JLabel jLabel;
        boolean white = true;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                jLabel = new JLabel();
                if (white) {
                    jLabel.setBackground(new Color(240, 217, 181));
                } else {
                    jLabel.setBackground(new Color(181, 136, 99));
                }
                jLabel.setOpaque(true);
                jLabel.setHorizontalAlignment(JLabel.CENTER);
                jLabel.setBounds(x * 64, y * 64, 64, 64);
                panel.add(jLabel);
                tileList[x][y] = jLabel;
                white = !white;
            }
            white = !white;
        }
    }

    private void resetColors() {
        boolean white = true;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (white) {
                    tileList[x][y].setBackground(new Color(240, 217, 181));
                } else {
                    tileList[x][y].setBackground(new Color(181, 136, 99));
                }
                white = !white;
            }
            white = !white;
        }
    }

    private void showLegalMoves(int x, int y) {
        List<Move> moves = MoveGenerator.generateMoves(x, y, this.board);
        for (Move move : moves) {
            legalMoves.add(Utils.formatXY(move.endX(), move.endY()));
        }
        if (legalMoves.size() == 0) {
            return;
        }
        int moveX, moveY;
        for (Move move : moves) {
            moveX = move.endX();
            moveY = move.endY();
            if (moveY % 2 == 0) {
                if (moveX % 2 == 0) {
                    tileList[moveX][moveY].setBackground(new Color(248, 109, 91));
                } else {
                    tileList[moveX][moveY].setBackground(new Color(218, 68, 50));
                }
            } else {
                if (moveX % 2 == 0) {
                    tileList[moveX][moveY].setBackground(new Color(218, 68, 50));
                } else {
                    tileList[moveX][moveY].setBackground(new Color(248, 109, 91));
                }
            }
        }
    }

    private void highlightClickedTile(int x, int y) {
        if (board.getTile(x, y) != null) {
            tileList[x][y].setBackground(new Color(51, 89, 128));
        }
    }

    private void compareCountResults() {
        String[] results = engine.getResults().split("`");
        System.out.println("Enter StockFish results");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        List<String[]> stockFishResults = new ArrayList<>();
        int nr = 1;
        try {
            while ((line = br.readLine()) != null)
            {
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

    private void makeMove(int x, int y) {
        if (board.isPawn(lastClicked.get(0), lastClicked.get(1)) && (y == 7 || y == 0)) {
            Scanner keyboard = new Scanner(System.in);
            System.out.println("Choose what to promote to");
            String type = keyboard.nextLine();
            while (!type.equals("q") && !type.equals("b") && !type.equals("r") && !type.equals("n")) {
                System.out.println("Invalid promotion, choose between q, b, r, n");
                type = keyboard.nextLine();
            }
            promotePawn(x, y, type);
            board.promote(lastClicked.get(0), lastClicked.get(1), x, y, type);
            if (board.isGameOver()) {
                System.out.println("game over");
            }
            else {
                makeEngineMove();
            }
            return;
        }
        movePiece(new Move(lastClicked.get(0), lastClicked.get(1), x, y));
        movePieceInMemory(new Move(lastClicked.get(0), lastClicked.get(1), x, y));
        if (board.isGameOver()) {
            System.out.println("game over");
        }
        else {
            makeEngineMove();
        }
    }

    private void killPiece(int x, int y) {
        tileList[x][y].setIcon(null);
        tileList[x][y].revalidate();
    }

    private void movePiece(Move move) {
        int startX = move.startX();
        int startY = move.startY();
        int endX = move.endX();
        int endY = move.endY();
        int piece = determinePieceNumber(startX, startY);
        //castle if true
        if (board.isKing(startX, startY) && Math.abs(startX - endX) == 2) {
            if (startX - endX < 0) {
                tileList[startX + 1][startY].setIcon(piecesImages.get(piece + 4));
                tileList[startX + 1][startY].revalidate();
                tileList[startX + 3][startY].setIcon(null);
                tileList[startX + 3][startY].revalidate();
            } else {
                tileList[startX - 1][startY].setIcon(piecesImages.get(piece + 4));
                tileList[startX - 1][startY].revalidate();
                tileList[startX - 4][startY].setIcon(null);
                tileList[startX - 4][startY].revalidate();
            }
        }
        //if piece is pawn that moved on x-axis (means capture) but endPosition is empty => en passant
        if (board.isPawn(startX, startY) && startX != endX && board.getTile(endX, endY) == null) {
            tileList[endX][startY].setIcon(null);
            tileList[endX][startY].revalidate();
        }
        tileList[endX][endY].setIcon(piecesImages.get(piece));
        tileList[endX][endY].revalidate();
        killPiece(startX, startY);
    }

    private void movePieceInMemory(Move move) {
        board.makeMove(move);
    }

    private void makeEngineMove() {
        if (engine == null) {
            return;
        }
        Move move = engine.determineMove(board);
        if (engine.getName().equals("counter")) {
            compareCountResults();
            return;
        }
        movePiece(move);
        movePieceInMemory(move);
    }

    private void promotePawn(int x, int y, String type) {
        int piece = determinePieceNumber(lastClicked.get(0), lastClicked.get(1), type);
        tileList[lastClicked.get(0)][lastClicked.get(1)].setIcon(null);
        tileList[lastClicked.get(0)][lastClicked.get(1)].revalidate();
        tileList[x][y].setIcon(piecesImages.get(piece));
        tileList[x][y].revalidate();
    }

    private void manageClick(int x, int y) {
        if (lastClicked.size() == 0) {
            resetColors();
            highlightClickedTile(x, y);
            showLegalMoves(x, y);
            if (legalMoves.size() > 0) {
                lastClicked.add(x);
                lastClicked.add(y);
            }
            return;
        }
        if (legalMoves.contains(Utils.formatXY(x, y))) {
            makeMove(x, y);
            legalMoves.clear();
        }
        lastClicked.clear();
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
        int[] realCoordinates = Utils.getXYCoordinatesFromClick(e.getX(), e.getY());
        manageClick(realCoordinates[0], realCoordinates[1]);
    }

    @Override
    public void mousePressed(MouseEvent e) {

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
