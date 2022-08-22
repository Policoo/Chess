import board.Board;
import engines.Engine;

public class Game {
    private Board board;
    private Engine player1;
    private Engine player2;

    public Game(Engine player1, Engine player2) {
        board = new Board();
        this.player1 = player1;
        this.player2 = player2;
    }

    public void play() {
        board.reset();
    }
}
