import game.Board;
import engines.Engine;


public class MatchMaker {
    private Board board;
    private Engine player1;
    private Engine player2;
    private final int numGames = 1000;

    public MatchMaker(Engine player1, Engine player2) {
        board = new Board();
        this.player1 = player1;
        this.player2 = player2;
    }

    public void play() {
        board.reset();
    }
}
