package gui;

import engines.Engine;
import engines.Greedy;
import engines.Random;
import game.Piece;
import utils.MoveGenerator;
import utils.StockFish;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class ChessGUI extends JFrame implements ActionListener {
    private JPanel leftPanel;
    private JButton resetButton;
    private JTextField fenInput;
    private JButton confirmFen;
    private JFormattedTextField depthInput;
    private JButton perftButton;
    private JPanel dialogPanel;
    private JButton flipBoard;

    private JPanel rightPanel;
    private JComboBox<Engine> evalEngineBox;
    private JComboBox<Engine> opponentOptionsChoice;

    private final GamePanel gamePanel;

    public ChessGUI() {
        MoveGenerator.initialize();

        setTitle("Chess");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        buildLeftPanel();
        buildRightPanel();
        gamePanel = new GamePanel();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
        add(gamePanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildLeftPanel() {
        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setPreferredSize(new Dimension(300, 300));
        leftPanel.setBackground(new Color(51, 51, 51));

        //create new panel for reset and flip board buttons
        JPanel topPanel = new JPanel();
        topPanel.setPreferredSize(new Dimension(300, 35));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.setBackground(new Color(51, 51, 51));

        //reset button
        resetButton = new JButton("Reset");
        resetButton.setBackground(new Color(183, 133, 111));
        resetButton.setFocusPainted(false);
        resetButton.setBorderPainted(false);
        resetButton.addActionListener(this);

        //flip board button
        ImageIcon originalIcon = new ImageIcon("src/images/flip board.png");
        Image originalImage = originalIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        flipBoard = new JButton(resizedIcon);
        flipBoard.setBorderPainted(false);
        flipBoard.setFocusPainted(false);
        flipBoard.setContentAreaFilled(false);
        flipBoard.addActionListener(this);

        topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        topPanel.add(resetButton);
        topPanel.add(Box.createRigidArea(new Dimension(185, 0)));
        topPanel.add(flipBoard);

        //create new panel for position from fen
        JPanel fenPanel = new JPanel(new FlowLayout());
        fenPanel.setPreferredSize(new Dimension(300, 60));
        fenPanel.setBackground(new Color(51, 51, 51));

        //text description for fen input
        JLabel fenText = new JLabel("Set up board from fen string");
        fenText.setFont(new Font("Ariel", Font.BOLD, 12));
        fenText.setBackground(new Color(51, 51, 51));
        fenText.setForeground(Color.WHITE);
        fenText.setOpaque(true);

        //input box for fen string
        fenInput = new JTextField();
        fenInput.setPreferredSize(new Dimension(200, 25));

        //confirm button for fen string
        confirmFen = new JButton("Confirm");
        confirmFen.setBackground(new Color(183, 133, 111));
        confirmFen.setFocusPainted(false);
        confirmFen.setBorderPainted(false);
        confirmFen.addActionListener(this);

        fenPanel.add(fenText);
        fenPanel.add(fenInput);
        fenPanel.add(confirmFen);

        //create new panel for perft
        JPanel perftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        perftPanel.setPreferredSize(new Dimension(300, 35));
        perftPanel.setBackground(new Color(51, 51, 51));

        //input box for go perft
        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(1);

        depthInput = new JFormattedTextField(formatter);
        depthInput.setPreferredSize(new Dimension(10, 25));
        depthInput.setColumns(10);

        //go perft button
        perftButton = new JButton("Go perft");
        perftButton.setBackground(new Color(183, 133, 111));
        perftButton.setFocusPainted(false);
        perftButton.setBorderPainted(false);
        perftButton.addActionListener(this);

        perftPanel.add(depthInput);
        perftPanel.add(perftButton);

        //dialog description
        JPanel dialogDesc = new JPanel(new FlowLayout(FlowLayout.CENTER)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(89, 89, 89));
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        dialogDesc.setBackground(new Color(51, 51, 51));
        dialogDesc.setPreferredSize(new Dimension(300, 25));

        JLabel desc = new JLabel("DIALOG BOX");
        desc.setForeground(Color.WHITE);
        desc.setFont(new Font("Ariel", Font.BOLD, 12));
        dialogDesc.add(desc);

        //dialog panel
        dialogPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(89, 89, 89));
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        dialogPanel.setPreferredSize(new Dimension(300, 397));
        dialogPanel.setBackground(new Color(51, 51, 51));
        dialogPanel.setBorder(new EmptyBorder(1, 3, 1, 3));

        leftPanel.add(topPanel);
        leftPanel.add(fenPanel);
        leftPanel.add(perftPanel);
        leftPanel.add(dialogDesc);
        leftPanel.add(dialogPanel);
    }

    private void buildRightPanel() {
        rightPanel = new JPanel(new FlowLayout());
        rightPanel.setBackground(new Color(51, 51, 51));
        rightPanel.setPreferredSize(new Dimension(300, 100));

        //panel for choosing engine eval
        JPanel engineChoice = new JPanel(new FlowLayout(FlowLayout.LEFT));
        engineChoice.setPreferredSize(new Dimension(300, 35));
        engineChoice.setBackground(new Color(51, 51, 51));

        //combo box for choosing engine eval
        Engine[] engineOptions = {new Random("b"), new Greedy("b")};
        evalEngineBox = new JComboBox<>(engineOptions);

        engineChoice.add(evalEngineBox);

        //panel for showing top moves
        JPanel evalPanel = new JPanel();
        evalPanel.setPreferredSize(new Dimension(300, 100));
        evalPanel.setLayout(new BoxLayout(evalPanel, BoxLayout.X_AXIS));
        evalPanel.setBackground(new Color(51, 51, 51));

        //moves
        JLabel moveEval = new JLabel("+4.3");
        moveEval.setBackground(Color.WHITE);
        moveEval.setOpaque(true);

        JLabel moveCoord = new JLabel("a1b1");
        moveCoord.setBackground(Color.WHITE);
        moveCoord.setOpaque(true);

        evalPanel.add(moveEval);
        evalPanel.add(moveCoord);

        //panel for choosing what engine to play against
        JPanel opponentChoicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        opponentChoicePanel.setPreferredSize(new Dimension(300, 35));
        opponentChoicePanel.setBackground(new Color(51, 51, 51));

        //description for opponent box
        JLabel opponentDesc = new JLabel("Play vs.");
        opponentDesc.setBackground(new Color(51, 51, 51));
        opponentDesc.setForeground(Color.WHITE);
        opponentDesc.setFont(new Font("Ariel", Font.BOLD, 12));
        opponentDesc.setOpaque(true);

        //choice of engine opponent
        Engine[] opponentOptions = {null, new Random("b"), new Greedy("b")};
        opponentOptionsChoice = new JComboBox<>(opponentOptions);

        opponentChoicePanel.add(opponentDesc);
        opponentChoicePanel.add(opponentOptionsChoice);

        rightPanel.add(engineChoice);
        rightPanel.add(evalPanel);
        rightPanel.add(opponentChoicePanel);
    }

    private void printMessage(String message) {
        dialogPanel.removeAll();
        dialogPanel.revalidate();
        dialogPanel.repaint();

        JTextArea textArea = new JTextArea(message);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setFont(new Font("Ariel", Font.BOLD, 12));
        textArea.setBackground(new Color(51, 51, 51));
        textArea.setForeground(new Color(255, 255, 255));
        textArea.setPreferredSize(new Dimension(290, 60));
        textArea.setOpaque(true);

        dialogPanel.add(textArea);
    }

    private void compareCountResults(String counterResults, String stockFishResults) {
        String[] counterSplit = counterResults.split("~");
        String[] stockFishSplit = stockFishResults.split("~");

        boolean correct = true;
        int numNodes = 0;

        //remove the text already inside
        dialogPanel.removeAll();
        dialogPanel.revalidate();
        dialogPanel.repaint();

        //go through all the counter results and compare them with stockfish
        for (int counterIndex = 0; counterIndex < counterSplit.length; counterIndex++) {
            if (counterSplit[counterIndex].contains("Nodes")) {
                String[] lineSplit = counterSplit[counterIndex].split(" ");
                numNodes = Integer.parseInt(lineSplit[2]);

                counterSplit[counterIndex] = "";
                break;
            }

            String[] lineSplit = counterSplit[counterIndex].split(" ");
            String move = lineSplit[0];
            int count = Integer.parseInt(lineSplit[1]);

            boolean found = false;
            for (int stockFishIndex = 0; stockFishIndex < stockFishSplit.length; stockFishIndex++) {
                if (!stockFishSplit[stockFishIndex].contains(move)) {
                    continue;
                }

                String[] split = stockFishSplit[stockFishIndex].split(" ");
                int fishCount = Integer.parseInt(split[1]);

                JLabel label;
                if (count == fishCount) {
                    label = new JLabel(counterSplit[counterIndex] + ", ");
                    label.setFont(new Font("Ariel", Font.BOLD, 12));
                    label.setBackground(new Color(51, 51, 51));
                    label.setForeground(new Color(66, 131, 0));
                    label.setOpaque(true);
                } else {
                    label = new JLabel(counterSplit[counterIndex] + " (Stockfish: " + fishCount + "), ");
                    label.setFont(new Font("Ariel", Font.BOLD, 12));
                    label.setBackground(new Color(51, 51, 51));
                    label.setForeground(new Color(169, 0, 0));
                    label.setOpaque(true);

                    correct = false;
                }

                dialogPanel.add(label);

                found = true;
                stockFishSplit[stockFishIndex] = "";
                break;
            }

            if (found) {
                counterSplit[counterIndex] = "";
            }
        }

        //print appropriate messages if illegal moves were found or if a move was missed
        List<String> notFound = new ArrayList<>();
        for (String s : counterSplit) {
            if (s.equals("")) {
                continue;
            }

            String move = s.split(" ")[0];
            move = move.split(":")[0];
            notFound.add(move);

            correct = false;
        }

        if (notFound.size() > 0) {
            StringBuilder text = new StringBuilder("Illegal moves found: ");
            for (String move : notFound) {
                text.append(move).append(", ");
            }

            JLabel label = new JLabel(text.toString());
            label.setPreferredSize(new Dimension(290, 25));
            label.setFont(new Font("Ariel", Font.BOLD, 12));
            label.setForeground(new Color(255, 255, 255));
            dialogPanel.add(label);
        }

        notFound.clear();
        for (String s : stockFishSplit) {
            if (s.equals("")) {
                continue;
            }

            String move = s.split(" ")[0];
            move = move.split(":")[0];
            notFound.add(move);

            correct = false;
        }

        if (notFound.size() > 0) {
            StringBuilder text = new StringBuilder("Missed moves: ");
            for (String move : notFound) {
                text.append(move).append(", ");
            }

            JLabel label = new JLabel(text.toString());
            label.setPreferredSize(new Dimension(290, 25));
            label.setFont(new Font("Ariel", Font.BOLD, 12));
            label.setForeground(new Color(255, 255, 255));
            dialogPanel.add(label);
        }

        if (correct) {
            JTextArea textArea = new JTextArea("RESULTS WERE CORRECT! Nodes searched: " + numNodes);
            textArea.setPreferredSize(new Dimension(290, 35));
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setEditable(false);
            textArea.setFocusable(false);
            textArea.setFont(new Font("Ariel", Font.BOLD, 12));
            textArea.setForeground(new Color(66, 131, 0));
            textArea.setOpaque(false);
            dialogPanel.add(textArea);
        }

        dialogPanel.revalidate();
        dialogPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == flipBoard) {
            gamePanel.flipBoard();

            printMessage("You are now playing as " + Piece.getColorString(gamePanel.getPerspective()));
            return;
        }

        if (e.getSource() == resetButton) {
            gamePanel.resetBoard();

            printMessage("Board has been reset");
            return;
        }

        if (e.getSource() == confirmFen) {
            String fenString = fenInput.getText();
            if (fenString.equals("")) {
                printMessage("Invalid fen string");
                return;
            }

            try {
                gamePanel.boardFromFen(fenString);
            } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                printMessage("Invalid fen string");
                return;
            }

            fenInput.setText("");
            printMessage("Board has been constructed from " + fenString);
            return;
        }

        if (e.getSource() == perftButton) {
            try {
                int depth = Integer.parseInt(depthInput.getText());
                depthInput.setText("");

                String stockFishResults = StockFish.goPerft(depth, gamePanel.getFen());
                String counterResults = gamePanel.goPerft(depth);
                compareCountResults(counterResults, stockFishResults);
            } catch (IOException | InterruptedException ex) {
                printMessage("StockFish is fucked");
                return;
            } catch (NumberFormatException ex) {
                printMessage("You did something wrong");
                return;
            }
        }

        if (e.getSource() == opponentOptionsChoice) {

        }
    }
}
