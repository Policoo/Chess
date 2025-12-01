#include "boardController.h"

#include <QString>
#include <QVariantMap>
#include <QVariantList>

#include "../../moveGenerator.h"
#include "../../piece.h"

BoardController::BoardController(QObject* parent) :
    QObject(parent),
    board(),
    legalMoves(),
    pieceMoves(),
    moveHistory(),
    selectedSquareIndex(-1),
    hasLastMove(false),
    lastPlayedMove(),
    whitePerspectiveFlag(true),
    promotionActive(false),
    promotionMove(),
    promotionChoicesList() {
    legalMoves = MoveGenerator::generateMoves(board);
}

QVariantList BoardController::piecesModel() const {
    return buildPiecesModel();
}

int BoardController::selectedSquare() const {
    return selectedSquareIndex;
}

QVariantList BoardController::legalTargets() const {
    QVariantList list;
    list.reserve(static_cast<int>(pieceMoves.size()));
    for (int sq : pieceMoves) {
        const int uiSq = whitePerspectiveFlag ? sq : 63 - sq;
        list.append(uiSq);
    }
    return list;
}

QVariantMap BoardController::lastMove() const {
    QVariantMap map;
    if (!hasLastMove) {
        return map;
    }
    const int startUi = whitePerspectiveFlag ? lastPlayedMove.start() : 63 - lastPlayedMove.start();
    const int endUi = whitePerspectiveFlag ? lastPlayedMove.end() : 63 - lastPlayedMove.end();
    map.insert(QStringLiteral("start"), startUi);
    map.insert(QStringLiteral("end"), endUi);
    return map;
}

bool BoardController::whitePerspective() const {
    return whitePerspectiveFlag;
}

Board& BoardController::boardRef() {
    return board;
}

QVariantList BoardController::promotionChoices() const {
    return promotionChoicesList;
}

void BoardController::resetBoard() {
    board = Board();
    legalMoves = MoveGenerator::generateMoves(board);
    pieceMoves.clear();
    selectedSquareIndex = -1;
    hasLastMove = false;
    moveHistory.clear();
    promotionActive = false;
    promotionChoicesList.clear();
    emit boardChanged();
    emit selectionChanged();
    emit lastMoveChanged();
    emit promotionChanged();
}

void BoardController::makeBoardFromFen(const QString& fenString) {
    board = Board(fenString.toStdString());
    legalMoves = MoveGenerator::generateMoves(board);
    pieceMoves.clear();
    selectedSquareIndex = -1;
    hasLastMove = false;
    moveHistory.clear();
    promotionActive = false;
    promotionChoicesList.clear();
    emit boardChanged();
    emit selectionChanged();
    emit lastMoveChanged();
    emit promotionChanged();
}

void BoardController::handleSquareClick(int square) {
    if (square < 0 || square >= 64) {
        return;
    }

    // If a promotion choice is active, interpret this click as a promotion
    // selection (or cancellation) before any normal move logic.
    if (promotionActive) {
        int chosenIndex = -1;
        for (int i = 0; i < promotionChoicesList.size(); ++i) {
            const QVariantMap choice = promotionChoicesList[i].toMap();
            if (choice.value(QStringLiteral("square")).toInt() == square) {
                chosenIndex = i;
                break;
            }
        }

        // Clicked outside the promotion options: cancel the overlay.
        if (chosenIndex == -1) {
            promotionActive = false;
            promotionChoicesList.clear();
            emit promotionChanged();
            return;
        }

        // Apply the promotion with the chosen piece type.
        const QVariantMap chosen = promotionChoicesList[chosenIndex].toMap();
        const int promoType = chosen.value(QStringLiteral("type")).toInt();

        Move moveToApply = promotionMove;
        moveToApply.setPromotion(promoType);

        promotionActive = false;
        promotionChoicesList.clear();
        emit promotionChanged();

        applyMove(moveToApply);
        return;
    }

    const int perIndex = whitePerspectiveFlag ? square : 63 - square;
    const int perLastClick = (selectedSquareIndex == -1)
        ? -1
        : (whitePerspectiveFlag ? selectedSquareIndex : 63 - selectedSquareIndex);

    // If this is a legal target from the selected piece
    if (!pieceMoves.empty() &&
        std::find(pieceMoves.begin(), pieceMoves.end(), perIndex) != pieceMoves.end()) {

        for (const Move& move : legalMoves) {
            if (move.start() == perLastClick && move.end() == perIndex) {
                if (move.flag() == Flag::PROMO_B || move.flag() == Flag::PROMO_N ||
                    move.flag() == Flag::PROMO_R || move.flag() == Flag::PROMO_Q) {
                    promotionActive = true;
                    promotionMove = move;
                    promotionChoicesList.clear();

                    const int uiIndex = whitePerspectiveFlag ? move.end() : 63 - move.end();
                    const int row = uiIndex / 8;
                    const int direction = (row == 0) ? 8 : -8;

                    const int color = board.getPieceColor(move.start());
                    const int promoTypes[4] = {
                        Piece::QUEEN,
                        Piece::ROOK,
                        Piece::BISHOP,
                        Piece::KNIGHT
                    };

                    for (int i = 0; i < 4; ++i) {
                        const int curIndex = uiIndex + direction * i;
                        if (curIndex < 0 || curIndex >= 64) {
                            continue;
                        }
                        QVariantMap choice;
                        choice.insert(QStringLiteral("square"), curIndex);
                        choice.insert(QStringLiteral("type"), promoTypes[i]);
                        choice.insert(QStringLiteral("color"), color);
                        promotionChoicesList.append(choice);
                    }

                    selectedSquareIndex = -1;
                    pieceMoves.clear();
                    emit selectionChanged();
                    emit promotionChanged();
                    return;
                }

                applyMove(move);
                return;
            }
        }

        // If we didn't find a matching move, clear selection.
        selectedSquareIndex = -1;
        pieceMoves.clear();
        emit selectionChanged();
        return;
    }

    // If we clicked an empty tile that is not a legal move
    if (board.isEmpty(perIndex)) {
        selectedSquareIndex = -1;
        pieceMoves.clear();
        emit selectionChanged();
        return;
    }

    // If we clicked on a piece whose turn it is
    if (board.isColor(perIndex, board.getTurn())) {
        selectedSquareIndex = square;

        pieceMoves.clear();
        if (!legalMoves.empty()) {
            for (const Move& move : legalMoves) {
                if (move.start() == perIndex) {
                    pieceMoves.push_back(move.end());
                }
            }
        }

        emit selectionChanged();
        return;
    }

    // Otherwise we clicked on an opponent's piece that is not a legal target
    selectedSquareIndex = square;
    pieceMoves.clear();
    emit selectionChanged();
}

QVariantList BoardController::buildPiecesModel() const {
    QVariantList model;
    model.reserve(32);

    for (int square = 0; square < 64; ++square) {
        if (board.isEmpty(square)) {
            continue;
        }

        QVariantMap entry;
        const int uiSquare = whitePerspectiveFlag ? square : 63 - square;
        entry.insert(QStringLiteral("square"), uiSquare);
        entry.insert(QStringLiteral("type"), board.getPieceType(square));
        entry.insert(QStringLiteral("color"), board.getPieceColor(square));

        model.append(entry);
    }

    return model;
}

void BoardController::undoMove() {
    if (moveHistory.empty()) {
        return;
    }

    const Move move = moveHistory.back();
    moveHistory.pop_back();
    board.undoMove(move);

    legalMoves = MoveGenerator::generateMoves(board);
    pieceMoves.clear();
    selectedSquareIndex = -1;

    hasLastMove = !moveHistory.empty();
    if (hasLastMove) {
        lastPlayedMove = moveHistory.back();
    }

    emit boardChanged();
    emit selectionChanged();
    emit lastMoveChanged();
}

void BoardController::togglePerspective() {
    whitePerspectiveFlag = !whitePerspectiveFlag;
    emit boardChanged();
    emit selectionChanged();
    emit lastMoveChanged();
    emit perspectiveChanged();
}

QString BoardController::debugString() {
    return QString::fromStdString(board.debugString());
}

void BoardController::applyMove(const Move& move) {
    board.makeMove(move);
    lastPlayedMove = move;
    moveHistory.push_back(move);

    legalMoves = MoveGenerator::generateMoves(board);
    hasLastMove = true;
    selectedSquareIndex = -1;
    pieceMoves.clear();

    emit boardChanged();
    emit selectionChanged();
    emit lastMoveChanged();
}
