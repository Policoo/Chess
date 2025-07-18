#pragma once

#include <vector>

#include "board.h"
#include "move.h"

class MoveGenerator {
public:
    /**
     * Generates legal moves for the color to move for the given positions.
     *
     * @param board board the game is played on.
     * @return vector containing legal moves.
     */
    static std::vector<Move> generateMoves(Board& board);

    /**
     * Checks if there are any legal moves in the given position. Stops executing as soon
     * as it finds a piece with legal moves.
     *
     * @param board Board the game is played on.
     * @return True if it finds a piece with legal moves, false otherwise.
     */
    static bool legalMovesExist(Board& board);

private:
    static std::vector<Move> generateSlidingPieceMoves(Board& board, int index, int pieceType, bool returnEarly = false);

    static std::vector<Move> generateKingMoves(Board& board, const int index, bool returnEarly = false);

    static std::vector<Move> generateCastleMoves(Board& board, const int index, bool returnEarly = false);

    static std::vector<Move> generateKnightMoves(Board& board, const int index, bool returnEarly = false);

    static std::vector<Move> generatePawnMoves(Board& board, int index, bool returnEarly = false);
};
