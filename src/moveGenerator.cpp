#include "moveGenerator.h"

#include <bit>
#include <unordered_map>

#include "move.h"
#include "piece.h"
#include "util/precomputedGameData.h"
#include "util/utils.h"

// <--> MOVE GENERATION <--> //

std::vector<Move> MoveGenerator::generateMoves(Board& board) {
    std::vector<Move> moves;
    moves.reserve(218);

    if (board.isGameOver()) {
        return moves;
    }

    const int turn = board.getTurn();
    uint64_t piecePositions = board.getPiecePositionsColor(turn);

    while (piecePositions) {
        const int piecePosition = popLSB(piecePositions);

        switch (board.getPieceType(piecePosition)) {
            case Piece::PAWN: {
                generatePawnMoves(board, piecePosition, moves);
                break;
            }
            case Piece::BISHOP: {
                generateSlidingPieceMoves(board, piecePosition, Piece::BISHOP, moves);
                break;
            }
            case Piece::ROOK: {
                generateSlidingPieceMoves(board, piecePosition, Piece::ROOK, moves);
                break;
            }
            case Piece::QUEEN: {
                generateSlidingPieceMoves(board, piecePosition, Piece::BISHOP, moves);
                generateSlidingPieceMoves(board, piecePosition, Piece::ROOK, moves);
                break;
            }
            case Piece::KING: {
                generateKingMoves(board, piecePosition, moves);
                generateCastleMoves(board, piecePosition, moves);
                break;
            }
            case Piece::KNIGHT: {
                generateKnightMoves(board, piecePosition, moves);
                break;
            }
        }
    }

    return moves;
}

void MoveGenerator::generatePawnMoves(Board& board, const int index, std::vector<Move> &moves, bool returnEarly) {
    //double check, only the king can move
    if (std::popcount(board.getCheckers()) > 1) {
        return;
    }

    const int color = board.getPieceColor(index);
    const std::vector<int>& dir = PGD::getPieceDirections(Piece::create(Piece::PAWN, color));

    //NOTE: It is impossible for a pawn to not be able to move forward, given that there isn't a piece blocking it.
    //      This is because if a pawn gets to the edge of the board, it promotes

    const uint64_t friendly = board.getPiecePositionsColor(board.getTurn());
    const uint64_t enemy = board.getPiecePositionsColor(abs(board.getTurn() - 1));
    const uint64_t allPieces = friendly | enemy;

    //check if push is allowed
    uint64_t movesBB = (1LL << index + dir[0]) & ~allPieces;

    const int startRank = (board.getTurn() == Piece::WHITE) ? 1 : 6;
    const uint64_t startRankMask = rankMask(startRank);
    const uint64_t isOnStartRank = (1LL << index) & startRankMask;

    //check if double push is allowed
    if (movesBB && isOnStartRank > 0) {
        uint64_t doublePush = (1LL << index + (dir[0] * 2)) & ~allPieces;

        if (board.isCheck())
            doublePush &= board.getCheck();
        if (board.getPins(index, board.getTurn()))
            doublePush &= board.getPins(index, board.getTurn());

        if (doublePush)
            moves.emplace_back(Move(index, index + (dir[0] * 2), Flag::DBL_PUSH));
    }

    //check captures
    const uint64_t attackMap = PGD::getAttackMap(Piece::create(Piece::PAWN, color), index);
    const uint64_t capturesBB = attackMap & enemy;
    movesBB |= capturesBB;

    //this is true if the en passant pawn is right next to us
    int enPassant = board.getEnPassant();
    if (enPassant == index - 1 || enPassant == index + 1) {

        //we get the en passant direction like this because the condition is the other way around for black
        //and I didn't want to write another if statement
        int epDir = enPassant > index ? dir[2 - board.getTurn()] : dir[1 + board.getTurn()];
        int end = index + epDir;

        //check that the pawn doesn't wrap around the board to take en passant
        if ((attackMap & (1LL << end)) != 0 && board.isLegalMove(index, end, Flag::EN_PASSANT))
            moves.emplace_back(Move(index, end, Flag::EN_PASSANT));
    }

    //pawn can only move in the check line or pin line
    if (board.isCheck())
        movesBB &= board.getCheck();
    if (board.getPins(index, board.getTurn()))
        movesBB &= board.getPins(index, board.getTurn());

    int promotionRank = board.getTurn() == Piece::WHITE ? 7 : 0;
    uint64_t promotionRankMask = rankMask(promotionRank);

    while (movesBB) {
        const int to = popLSB(movesBB);

        if ((promotionRankMask & (1LL << to)) > 0) {
            moves.emplace_back(index, to, Flag::PROMO_B);
            moves.emplace_back(index, to, Flag::PROMO_R);
            moves.emplace_back(index, to, Flag::PROMO_N);
            moves.emplace_back(index, to, Flag::PROMO_Q);
            continue;
        }

        Flag flag = (((1LL << to) & enemy) > 0) ? Flag::CAPTURE : Flag::QUIET;
        moves.emplace_back(index, to, flag);

        if (returnEarly)
            return;
    }
}

void MoveGenerator::generateKnightMoves(Board& board, const int index, std::vector<Move> &moves, bool returnEarly) {
    const uint64_t pseudoMoves = PGD::getAttackMap(Piece::create(Piece::KNIGHT, Piece::WHITE), index);
    uint64_t pseudoLegalMoves = pseudoMoves & ~board.getPiecePositionsColor(board.getPieceColor(index));

    //if it's a double check, only king moves allowed
    if (std::popcount(board.getCheckers()) > 1)
        return;

    //if it's check, you are only allowed to move on the checkline
    if (std::popcount(board.getCheckers()) == 1)
        pseudoLegalMoves &= board.getCheck();

    //if there is a pin, only moves along that pin line are legal
    //pinned pieces will not be able to move in checks
    const uint64_t pin = board.getPins(index, board.getTurn());
    if (pin)
        pseudoLegalMoves &= pin;

    while (pseudoLegalMoves) {
        const int moveTo = popLSB(pseudoLegalMoves);

        //check if we need to set the capture flag
        const Flag flag = (board.isEmpty(moveTo)) ? Flag::QUIET : Flag::CAPTURE;
        moves.emplace_back(index, moveTo, flag);

        if (returnEarly)
            return;
    }
}

void MoveGenerator::generateSlidingPieceMoves(Board& board, const int index, const int pieceType,
                                              std::vector<Move> &moves, bool returnEarly) {
    const uint64_t blockerBitboard = board.getPiecePositionsColor(Piece::WHITE) | board.
                                     getPiecePositionsColor(Piece::BLACK);

    //where we can move according to current blockers
    const uint64_t pseudoMoves = PGD::getPseudoMoves(pieceType, index, blockerBitboard);

    //take out friendly piece captures
    uint64_t pseudoLegalMoves = pseudoMoves & ~board.getPiecePositionsColor(board.getPieceColor(index));

    //if it's a double check, only king moves allowed
    if (std::popcount(board.getCheckers()) > 1)
        return;

    //if it's check, you are only allowed to move on the checkline
    if (std::popcount(board.getCheckers()) == 1)
        pseudoLegalMoves &= board.getCheck();

    //if there is a pin, only moves along that pin line are legal
    //pinned pieces will not be able to move in checks
    const uint64_t pin = board.getPins(index, board.getTurn());
    if (pin)
        pseudoLegalMoves &= pin;

    while (pseudoLegalMoves) {
        const int moveTo = popLSB(pseudoLegalMoves);

        //check if we need to set the capture flag
        const Flag flag = (board.isEmpty(moveTo)) ? Flag::QUIET : Flag::CAPTURE;
        moves.emplace_back(index, moveTo, flag);

        if (returnEarly)
            return;
    }
}

void MoveGenerator::generateKingMoves(Board& board, const int index, std::vector<Move> &moves, bool returnEarly) {
    const uint64_t pseudoMoves = PGD::getAttackMap(Piece::create(Piece::KING, Piece::WHITE), index);
    uint64_t pseudoLegalMoves = pseudoMoves & ~board.getPiecePositionsColor(board.getPieceColor(index));

    if (board.isCheck()) {
        //this is all for extending the check line
        uint64_t checkers = board.getCheckers();
        while (checkers) {
            int checkerIndex = popLSB(checkers);

            switch (board.getPieceType(checkerIndex)) {
                case Piece::KING:
                case Piece::KNIGHT:
                    pseudoLegalMoves &= ~PGD::getAttackMap(Piece::create(board.getPieceType(checkerIndex), Piece::WHITE), checkerIndex);
                break;
               case Piece::PAWN:
                    pseudoLegalMoves &= ~PGD::getAttackMap(Piece::create(Piece::PAWN, abs(board.getTurn() - 1)), checkerIndex);
                break;
                default:
                    //sliding pieces
                        uint64_t blockerBB = board.getPiecePositionsColor(Piece::WHITE) | board.getPiecePositionsColor(Piece::BLACK);
                //remove king from blockers
                blockerBB &= ~(1LL << index);
                uint64_t pieceView = PGD::getPseudoMoves(board.getPieceType(checkerIndex), checkerIndex, blockerBB);
                pseudoLegalMoves &= ~(pieceView);
                break;
            }
        }
    }

    uint64_t legalMoves = pseudoLegalMoves & ~board.getColorAttackMap(abs(board.getTurn() - 1));

    while (legalMoves) {
        const int moveTo = popLSB(legalMoves);

        //check if we need to set the capture flag
        const Flag flag = (board.isEmpty(moveTo)) ? Flag::QUIET : Flag::CAPTURE;
        moves.emplace_back(index, moveTo, flag);

        if (returnEarly)
            return;
    }
}

void MoveGenerator::generateCastleMoves(Board& board, const int index, std::vector<Move> &moves, bool returnEarly) {
    const int color = board.getTurn();

    if (board.isCheck()) {
        return;
    }

    const uint64_t friendly = board.getPiecePositionsColor(board.getTurn());
    const uint64_t enemy = board.getPiecePositionsColor(abs(board.getTurn() - 1));

    const uint64_t allPieces = friendly | enemy;
    const uint64_t enemyAttackMap = board.getColorAttackMap(abs(board.getTurn() - 1));

    if (board.canCastleKSide(color)) {
        uint64_t between = betweenMask(index, index + 3);

        //if the king would not be attacked while castling and there is no piece between king and rook
        if ((between & enemyAttackMap) == 0 && (between & allPieces) == 0)
            moves.emplace_back(index, index + 2, Flag::CASTLE_K);
    }

    if (board.canCastleQSide(color)) {
        uint64_t between = betweenMask(index, index - 4);

        //no pieces allowed between king and rook
        if ((between & allPieces) != 0) {
            return;
        }

        //index - 3 can be a check, it's fine
        between &= ~(1LL << index - 3);

        //if the king would not be attacked while castling, it's ok
        if ((between & enemyAttackMap) == 0)
            moves.emplace_back(index, index - 2, Flag::CASTLE_Q);
    }
}

// <--> MOVE GENERATION <--> //

// <--> CHECKING IF LEGAL MOVES EXIST <--> //

bool MoveGenerator::legalMovesExist(Board& board) {
    std::vector<Move> moves;

    if (board.isGameOver()) {
        return false;
    }

    const int turn = board.getTurn();
    uint64_t piecePositions = board.getPiecePositionsColor(turn);

    while (piecePositions) {
        const int piecePosition = popLSB(piecePositions);

        switch (board.getPieceType(piecePosition)) {
            case Piece::PAWN: {
                generatePawnMoves(board, piecePosition, moves, true);
                if (!moves.empty())
                    return true;
                break;
            }
            case Piece::BISHOP: {
                generateSlidingPieceMoves(
                        board, piecePosition, Piece::BISHOP, moves, true);
                if (!moves.empty())
                    return true;
                break;
            }
            case Piece::ROOK: {
                generateSlidingPieceMoves(board, piecePosition, Piece::ROOK, moves, true);
                if (!moves.empty())
                    return true;
                break;
            }
            case Piece::QUEEN: {
                generateSlidingPieceMoves(
                        board, piecePosition, Piece::BISHOP, moves, true);
                generateSlidingPieceMoves(
                        board, piecePosition, Piece::ROOK, moves, true);
                if (!moves.empty())
                    return true;
                break;
            }
            case Piece::KING: {
                generateKingMoves(board, piecePosition, moves, true);
                generateCastleMoves(board, piecePosition, moves, true);
                if (!moves.empty())
                    return true;
                break;
            }
            case Piece::KNIGHT: {
                generateKnightMoves(board, piecePosition, moves, true);
                if (!moves.empty())
                    return true;
                break;
            }
        }
    }

    return false;
}
