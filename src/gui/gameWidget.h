#pragma once

#include <QMouseEvent>
#include <QLabel>

#include <string>
#include <vector>

#include "../board.h"
#include "../move.h"

class GameWidget : public QWidget {
    Q_OBJECT

public:
    explicit GameWidget(QWidget* parent);

    void resetBoard();

    std::string getDebugString();

    void undoMove();

    void flipBoard();

    void makeBoardFromFen(const std::string& fenString);

    void goPerft(int depth);

    void setOpponent(std::string opponentChoice);

    void startEngineMatch(std::string engine1, std::string engine2);

    std::string getPerspective();

void resizeEvent(QResizeEvent* ev) override {
    QWidget::resizeEvent(ev);

    // figure out how much space gutters eat up:
    int gutterW = westWidget->width() + eastWidget->width();
    int gutterH = northWidget->height() + southWidget->height();

    // remaining space
    int availW = width()  - gutterW;
    int availH = height() - gutterH;

    // square side is the smaller of the two
    int side = qMin(availW, availH);

    // position the board in the middle:
    int x = (width()  - side) / 2;
    int y = (height() - side) / 2;

    boardWidget->setGeometry(x, y, side, side);
}

public slots:
    void perftDone(std::vector<std::string> results);

signals:
    void moveMadeSignal();

    void perftResultsReady(std::vector<std::string> results);

protected:
    void mousePressEvent(QMouseEvent* event) override;

private:
    QWidget* boardWidget;

    QWidget* northWidget;
    QWidget* eastWidget;
    QWidget* westWidget;
    QWidget* southWidget;

    std::vector<QLabel*> tile;
    std::vector<QPixmap> pieceImages;

    Board* board;
    std::vector<Move> legalMoves;
    std::vector<Move> moveHistory;
    std::vector<int> pieceMoves;
    std::vector<int> promotionTiles;
    Move promotionMove;
    int lastClick;
    int perspective;

    /**
     * Initializes the tile array and sets up the board.
     */
    void constructBoard();

    /**
     * Adds coordinates to board.
     */
    void addCoordinates();

    /**
     * Cuts up the image with the pieces and stores the images in a vector.
     */
    void initializePieceImages();

    /**
     * Sets the square color to base state
     */
    void resetColors();

    /**
     * Highlights the clicked tile if there is a piece on it.
     *
     * @param index tile that was clicked.
     */
    void highlightClick(int index);

    /**
     * Highlights the move on the board.
     *
     * @param move move that was made.
     */
    void highlightMove(Move move);

    /**
     * Colors the tiles where legal moves are available.
     *
     * @param index index of piece.
     */
    void showLegalMoves(int index);

    /**
     * Colors the squares to show the options for promoting a pawn.
     *
     * @param index tile where promotion is happening.
     */
    void showPromotionOptions(int index);

    /**
     * Updates the pieces on the GUI to reflect board state.
     */
    void updateBoard();

    /**
     * Processes a click on the board.
     *
     * @param index tile that was clicked.
     */
    void handleClick(int index);

    void makeMove(Move move);

    void clearWidgets(QWidget* widget);
};
