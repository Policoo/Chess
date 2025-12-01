#pragma once

#include <QObject>
#include <QVariantList>
#include <QVariantMap>

#include "../../board.h"
#include "../../move.h"

class BoardController : public QObject {
    Q_OBJECT

    Q_PROPERTY(QVariantList piecesModel READ piecesModel NOTIFY boardChanged)
    Q_PROPERTY(int selectedSquare READ selectedSquare NOTIFY selectionChanged)
    Q_PROPERTY(QVariantList legalTargets READ legalTargets NOTIFY selectionChanged)
    Q_PROPERTY(QVariantMap lastMove READ lastMove NOTIFY lastMoveChanged)
    Q_PROPERTY(bool whitePerspective READ whitePerspective NOTIFY perspectiveChanged)
    Q_PROPERTY(QVariantList promotionChoices READ promotionChoices NOTIFY promotionChanged)

public:
    explicit BoardController(QObject* parent = nullptr);

    QVariantList piecesModel() const;

    int selectedSquare() const;

    QVariantList legalTargets() const;

    QVariantMap lastMove() const;

    bool whitePerspective() const;

    Board& boardRef();

    void applyMove(const Move& move);

    QVariantList promotionChoices() const;

public slots:
    Q_INVOKABLE void resetBoard();

    Q_INVOKABLE void makeBoardFromFen(const QString& fenString);

    Q_INVOKABLE void handleSquareClick(int square);

    Q_INVOKABLE void undoMove();

    Q_INVOKABLE void togglePerspective();

    Q_INVOKABLE QString debugString();

signals:
    void boardChanged();

    void selectionChanged();

    void lastMoveChanged();

    void perspectiveChanged();

    void promotionChanged();

private:
    Board board;

    std::vector<Move> legalMoves;
    std::vector<int> pieceMoves;
    std::vector<Move> moveHistory;
    int selectedSquareIndex;
    bool hasLastMove;
    Move lastPlayedMove;
    bool whitePerspectiveFlag;

    bool promotionActive;
    Move promotionMove;
    QVariantList promotionChoicesList;

    QVariantList buildPiecesModel() const;
};
