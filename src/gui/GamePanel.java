package gui;

import engines.Counter;
import engines.Engine;
import game.Board;
import game.Move;
import game.Piece;
import utils.MoveGenerator;
import utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements MouseListener {
    private final JLabel[] tileList;
    private final JPanel mainSouth;
    private final JPanel mainWest;

    private List<ImageIcon> piecesImages;
    private List<Move> legalMoves;
    private int lastClicked = -1;

    private Board board;
    private Engine engine;
    private final Counter counter;

    private int perspective;
    private final List<Integer> promotionTiles;
    private Move promotionMove;

    public GamePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(552, 552));

        JPanel mainNorth = new JPanel();
        JPanel mainEast = new JPanel();
        mainSouth = new JPanel();
        mainWest = new JPanel();
        JPanel mainCentre = new JPanel(new GridLayout(8, 8));

        mainNorth.setBackground(new Color(42, 42, 42));
        mainEast.setBackground(new Color(42, 42, 42));
        mainWest.setBackground(new Color(42, 42, 42));
        mainSouth.setBackground(new Color(42, 42, 42));
        mainCentre.setBackground(new Color(42, 42, 42));

        mainNorth.setPreferredSize(new Dimension(552, 20));
        mainEast.setPreferredSize(new Dimension(20, 552));
        mainWest.setPreferredSize(new Dimension(20, 552));
        mainSouth.setPreferredSize(new Dimension(552, 20));

        tileList = new JLabel[64];
        for (int index = 0; index < 64; index++) {
            tileList[index] = new JLabel();
            tileList[index].setPreferredSize(new Dimension(64, 64));
            mainCentre.add(tileList[index]);
        }

        addCoordinates();
        colorBoard();

        mainCentre.addMouseListener(this);

        add(mainNorth, BorderLayout.NORTH);
        add(mainEast, BorderLayout.EAST);
        add(mainSouth, BorderLayout.SOUTH);
        add(mainWest, BorderLayout.WEST);
        add(mainCentre, BorderLayout.CENTER);

        board = new Board();
        fillPiecesImagesList();
        perspective = Piece.WHITE;
        addPiecesToBoard();
        counter = new Counter();
        promotionTiles = new ArrayList<>();
        legalMoves = MoveGenerator.generateMoves(board);
    }

    public void flipBoard() {
        perspective = Math.abs(perspective - 8);

        resetColors();
        addPiecesToBoard();
        addCoordinates();
    }

    public String goPerft(int depth) {
        return counter.goPerft(board.deepCopy(), depth);
    }

    public String getFen() {
        return board.positionToFen();
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public int getPerspective() {
        return perspective;
    }

    public void boardFromFen(String fenString) {
        board = new Board(fenString);

        resetColors();
        addPiecesToBoard();

        legalMoves = MoveGenerator.generateMoves(board);
    }

    public void resetBoard() {
        board = new Board();

        resetColors();
        addPiecesToBoard();

        legalMoves = MoveGenerator.generateMoves(board);
    }

    private void addCoordinates() {
        mainWest.setLayout(new GridLayout(8, 1, 0, 0));
        mainSouth.setLayout(new GridLayout(1, 8, 0, 0));
        mainSouth.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        mainWest.removeAll();
        mainWest.revalidate();
        mainWest.repaint();

        mainSouth.removeAll();
        mainSouth.revalidate();
        mainSouth.repaint();

        if (perspective == Piece.WHITE) {
            for (int i = 8; i >= 1; i--) {
                JLabel label = new JLabel(Integer.toString(i));
                label.setHorizontalAlignment(JLabel.CENTER);

                label.setFont(new Font("Impact", Font.PLAIN, 16));
                label.setForeground(new Color(201, 201, 201));

                mainWest.add(label);
            }

            for (char c = 'A'; c <= 'H'; c++) {
                JLabel label = new JLabel(Character.toString(c));
                label.setHorizontalAlignment(JLabel.CENTER);

                label.setFont(new Font("Impact", Font.PLAIN, 16));
                label.setForeground(new Color(201, 201, 201));

                mainSouth.add(label);
            }

            return;
        }

        for (int i = 1; i < 9; i++) {
            JLabel label = new JLabel(Integer.toString(i));
            label.setHorizontalAlignment(JLabel.CENTER);

            label.setFont(new Font("Impact", Font.PLAIN, 16));
            label.setForeground(new Color(201, 201, 201));

            mainWest.add(label);
        }

        for (char c = 'H'; c >= 'A'; c--) {
            JLabel label = new JLabel(Character.toString(c));
            label.setHorizontalAlignment(JLabel.CENTER);

            label.setFont(new Font("Impact", Font.PLAIN, 16));
            label.setForeground(new Color(201, 201, 201));

            mainSouth.add(label);
        }
    }

    private void colorBoard() {
        boolean white = true;

        for (int index = 0; index < 64; index++) {
            if (white) {
                tileList[index].setBackground(new Color(240, 217, 181));
            } else {
                tileList[index].setBackground(new Color(181, 136, 99));
            }
            tileList[index].setOpaque(true);
            tileList[index].setHorizontalAlignment(JLabel.CENTER);

            if (index % 8 != 7) {
                white = !white;
            }
        }
    }

    private void fillPiecesImagesList() {
        piecesImages = new ArrayList<>();
        BufferedImage all = null;
        try {
            all = ImageIO.read(new File("src/images/chess pieces.png"));
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

    private void addPiecesToBoard() {
        int piece;

        for (int index = 0; index < 64; index++) {
            int perIndex = (perspective == Piece.WHITE) ? index : 63 - index;

            if (board.isEmptyTile(index)) {
                tileList[perIndex].setIcon(null);
                continue;
            }

            piece = determinePieceNumber(index);
            tileList[perIndex].setIcon(piecesImages.get(piece));
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
        int perIndex = (perspective == Piece.WHITE) ? index : 63 - index;

        if (legalMoves.size() == 0) {
            return;
        }

        for (Move move : legalMoves) {
            if (move.start() != perIndex) {
                continue;
            }

            int end = (perspective == Piece.WHITE) ? move.end() : 63 - move.end();
            if ((end / 8) % 2 == 0) {
                if ((end % 8) % 2 == 0) {
                    tileList[end].setBackground(new Color(248, 109, 91));
                } else {
                    tileList[end].setBackground(new Color(218, 68, 50));
                }
            } else {
                if ((end % 8) % 2 == 0) {
                    tileList[end].setBackground(new Color(218, 68, 50));
                } else {
                    tileList[end].setBackground(new Color(248, 109, 91));
                }
            }
        }
    }

    private void highlightClickedTile(int index) {
        int perIndex = (perspective == Piece.WHITE) ? index : 63 - index;

        if (!board.isEmptyTile(perIndex)) {
            tileList[index].setBackground(new Color(51, 89, 128));
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
            move = engine.determineMove(board);
            makeMove(move);
        }

        //generate new legal moves
        legalMoves = MoveGenerator.generateMoves(board);
    }

    private void manageClick(int index) {
        //special case for promotions
        if (promotionTiles.size() > 0) {
            //if we didn't promote, but clicked outside the options
            if (!promotionTiles.contains(index)) {
                promotionTiles.clear();
                resetColors();
                addPiecesToBoard();
                return;
            }

            if (index / 8 == 0 || index / 8 == 7) {
                promotionMove.setPromotion(Piece.QUEEN);
                makeMove(promotionMove);
                promotionTiles.clear();
                resetColors();
                addPiecesToBoard();
                return;
            }

            if (index / 8 == 1 || index / 8 == 6) {
                promotionMove.setPromotion(Piece.ROOK);
                makeMove(promotionMove);
                promotionTiles.clear();
                resetColors();
                addPiecesToBoard();
                return;
            }

            if (index / 8 == 2 || index / 8 == 5) {
                promotionMove.setPromotion(Piece.BISHOP);
                makeMove(promotionMove);
                promotionTiles.clear();
                resetColors();
                addPiecesToBoard();
                return;
            }

            if (index / 8 == 3 || index / 8 == 4) {
                promotionMove.setPromotion(Piece.KNIGHT);
                makeMove(promotionMove);
                promotionTiles.clear();
                resetColors();
                addPiecesToBoard();
                return;
            }
        }

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
            int perLastClicked = (perspective == Piece.WHITE) ? lastClicked : 63 - lastClicked;
            int perIndex = (perspective == Piece.WHITE) ? index : 63 - index;


            if (move.start() == perLastClicked && move.end() == perIndex) {
                if (move.flag() == Move.PROMOTION) {
                    getPromotion(move.end());
                    promotionMove = move;
                    return;
                }

                makeMove(move);
                break;
            }
        }

        lastClicked = -1;
        resetColors();
    }

    private void getPromotion(int index) {
        List<ImageIcon> promotionPieces = new ArrayList<>();
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

        if (perspective == Piece.WHITE) {
            int direction = (index < 8) ? 8 : -8;
            drawPromotionOptions(index, direction, promotionPieces);
        }

        if (perspective == Piece.BLACK) {
            int direction = (index < 8) ? -8 : 8;
            int tileIndex = 63 - index;
            drawPromotionOptions(tileIndex, direction, promotionPieces);
        }
    }

    private void drawPromotionOptions(int index, int direction, List<ImageIcon> promotionPieces) {
        for (int step = 0; step < 4; step++) {
            int curIndex = index + (direction * step);
            tileList[curIndex].setBackground(Color.WHITE);
            tileList[curIndex].setIcon(promotionPieces.get(step));

            promotionTiles.add(curIndex);
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
