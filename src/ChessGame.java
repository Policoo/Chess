import gui.ChessGUI;

import javax.swing.*;

public class ChessGame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}
