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
	static const std::vector<Move>& generateMoves(Board& board);

	/**
	 * Checks if there are any legal moves in the given position. Stops executing as soon
	 * as it finds a piece with legal moves.
	 *
	 * @param board Board the game is played on.
	 * @return True if it finds a piece with legal moves, false otherwise.
	 */
	static bool legalMovesExist(Board& board);

private:
	static std::vector<Move> moves;

	// <--> The "generate" functions are used to generate moves <--> //

	static void generateKingMoves(Board& board, const int index);
	static void generateCastleMoves(Board& board, const int index);
	static void generateQueenMoves(Board& board, const int index);
	static void generateRookMoves(Board& board, const int index);
	static void generateBishopMoves(Board& board, const int index);
	static void generateKnightMoves(Board& board, const int index);
	static void generatePawnMoves(Board& board, int index);
	static void scanDirectionOnce(Board& board, const int index, const std::vector<int>& directions);
	static void scanDirectionUntilCollision(Board& board, const int index, const std::vector<int>& directions);

	// <--> The "check" functions are used to determine if there are legal moves, so they return early <--> //

	static bool checkKingMoves(Board& board, const int index);
	static bool checkCastleMoves(Board& board, const int index);
	static bool checkQueenMoves(Board& board, const int index);
	static bool checkRookMoves(Board& board, const int index);
	static bool checkBishopMoves(Board& board, const int index);
	static bool checkKnightMoves(Board& board, const int index);
	static bool checkPawnMoves(Board& board, const int index);
	static bool checkDirectionOnce(Board& board, const int index, const std::vector<int>& directions);
	static bool checkDirectionUntilCollision(Board& board, const int index, const std::vector<int>& directions);
};