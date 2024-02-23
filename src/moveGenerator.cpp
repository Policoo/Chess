#include "moveGenerator.h"

#include <unordered_map>

#include "move.h"
#include "piece.h"
#include "precomputedGameData.h"

// <--> MOVE GENERATION <--> //

std::vector<Move> MoveGenerator::moves;

const std::vector<Move>& MoveGenerator::generateMoves(Board& board) {
	//clear the moves vector to make sure it's empty
	moves.clear();

	if (board.isGameOver()) {
		return moves;
	}

	const int turn = board.getTurn();
	const std::vector<int>& piecePositions = board.getPiecePositions(turn);

	for (const int& piecePosition : piecePositions) {
		if (piecePosition == -1) {
			continue;
		}

		switch (board.getPieceType(piecePosition)) {
		case Piece::PAWN: {
			generatePawnMoves(board, piecePosition);
			break;
		}
		case Piece::BISHOP: {
			generateBishopMoves(board, piecePosition);
			break;
		}
		case Piece::ROOK: {
			generateRookMoves(board, piecePosition);
			break;
		}
		case Piece::QUEEN: {
			generateQueenMoves(board, piecePosition);
			break;
		}
		case Piece::KING: {
			generateKingMoves(board, piecePosition);
			generateCastleMoves(board, piecePosition);
			break;
		}
		case Piece::KNIGHT: {
			generateKnightMoves(board, piecePosition);
			break;
		}
		}
	}

	return moves;
}

void MoveGenerator::generatePawnMoves(Board& board, int index) {
	const int color = board.getPieceColor(index);
	const std::vector<int>& dir = PGD::getPieceDirections(Piece::create(Piece::PAWN, color));

	// see if pawn can move forward
	if (PGD::getEdgeOfBoard(dir[0], index) > 0 && board.isEmpty(index + dir[0]) &&
		board.isLegalMove(index, index + dir[0])) {
		// if a pawn can only move once before hitting the edge, he is going to promote
		if (PGD::getEdgeOfBoard(dir[0], index) == 1) {
			moves.emplace_back(index, index + dir[0], Flag::PROMOTION, Piece::QUEEN);
			moves.emplace_back(index, index + dir[0], Flag::PROMOTION, Piece::ROOK);
			moves.emplace_back(index, index + dir[0], Flag::PROMOTION, Piece::BISHOP);
			moves.emplace_back(index, index + dir[0], Flag::PROMOTION, Piece::KNIGHT);
		}
		else {
			moves.emplace_back(index, index + dir[0], Flag::NONE, 0);
		}
	}

	// if pawn can go forward 6 times it has not moved yet, check if moving 2 tiles is also possible
	if (PGD::getEdgeOfBoard(dir[0], index) == 6 && board.isEmpty(index + dir[0]) &&
		board.isEmpty(index + (dir[0] * 2)) &&
		board.isLegalMove(index, index + (dir[0] * 2))) {
		moves.emplace_back(index, index + (dir[0] * 2), Flag::NONE, 0);
	}

	// look sideways for captures
	for (int side = 1; side < dir.size(); side++) {
		if (PGD::getEdgeOfBoard(dir[side], index) == 0) {
			continue;
		}

		// check for regular capture
		if (!board.isEmpty(index + dir[side]) && !board.isColor(index + dir[side], color)
			&& board.isLegalMove(index, index + dir[side])) {
			// if a pawn can only move forward once before hitting the edge, he is going to promote
			if (PGD::getEdgeOfBoard(dir[0], index) == 1) {
				moves.emplace_back(index, index + dir[side], Flag::PROMOTION, Piece::QUEEN);
				moves.emplace_back(index, index + dir[side], Flag::PROMOTION, Piece::ROOK);
				moves.emplace_back(index, index + dir[side], Flag::PROMOTION, Piece::BISHOP);
				moves.emplace_back(index, index + dir[side], Flag::PROMOTION, Piece::KNIGHT);
			}
			else {
				moves.emplace_back(index, index + dir[side], Flag::NONE, 0);
			}
		}

		// from this point on we check for en passant, don't waste time if it's not
		// possible
		if (board.getEnPassant() == 0) {
			continue;
		}

		// if we get here, en passant is possible on the board, so check if this pawn can take it
		if (const int enPassantCapt = board.getEnPassant() + dir[0];
			enPassantCapt == index + dir[side] && board.isLegalMove(index, index + dir[side])) {
			moves.emplace_back(index, index + dir[side], Flag::EN_PASSANT, 0);
		}
	}
}

void MoveGenerator::generateKnightMoves(Board& board, const int index) {
	scanDirectionOnce(board, index, PGD::getPieceDirections(Piece::KNIGHT));
}

void MoveGenerator::generateBishopMoves(Board& board, const int index) {
	scanDirectionUntilCollision(board, index, PGD::getPieceDirections(Piece::BISHOP));
}

void MoveGenerator::generateRookMoves(Board& board, const int index) {
	scanDirectionUntilCollision(board, index, PGD::getPieceDirections(Piece::ROOK));
}

void MoveGenerator::generateQueenMoves(Board& board, const int index) {
	scanDirectionUntilCollision(board, index, PGD::getPieceDirections(Piece::QUEEN));
}

void MoveGenerator::generateKingMoves(Board& board, const int index) {
	scanDirectionOnce(board, index, PGD::getPieceDirections(Piece::KING));
}

void MoveGenerator::generateCastleMoves(Board& board, const int index) {
	const int color = board.getTurn();

	if (!board.canCastleKSide(color) && !board.canCastleQSide(color) || board.isCheck()) {
		return;
	}

	// if the tiles between king and rook are empty
	if (board.isEmpty(index + 1) && board.isEmpty(index + 2) &&
		board.canCastleKSide(color)) {
		// if there is a piece at the end, and it is your rook, which hasn't moved
		if (!board.isEmpty(index + 3) && board.isRook(index + 3) &&
			board.isColor(index + 3, color)) {
			// if move is legal
			if (board.isLegalMove(index, index + 1) &&
				board.isLegalMove(index, index + 2)) {
				moves.emplace_back(index, index + 2, Flag::CASTLE, 0);
			}
		}
	}

	// if the tiles between king and rook are empty
	if (board.isEmpty(index - 1) && board.isEmpty(index - 2) &&
		board.isEmpty(index - 3) && board.canCastleQSide(color)) {
		// if there is a piece at the end, and it is your rook, which hasn't moved
		if (!board.isEmpty(index - 4) && board.isRook(index - 4) &&
			board.isColor(index - 4, color)) {
			// if move is legal
			if (board.isLegalMove(index, index - 1) &&
				board.isLegalMove(index, index - 2)) {
				moves.emplace_back(index, index - 2, Flag::CASTLE, 0);
			}
		}
	}
}

void MoveGenerator::scanDirectionOnce(Board& board, const int index, const std::vector<int>& directions) {
	const int color = board.getTurn();

	for (const int& dir : directions) {
		const int count = PGD::getEdgeOfBoard(dir, index);

		if (count > 0 &&
			(board.isEmpty(index + dir) || !board.isColor(index + dir, color))) {
			if (board.isLegalMove(index, index + dir)) {
				moves.emplace_back(index, index + dir, Flag::NONE, 0);
			}
		}
	}
}

void MoveGenerator::scanDirectionUntilCollision(Board& board, const int index, const std::vector<int>& directions) {
	const int color = board.getTurn();

	for (const int& dir : directions) {
		int count = PGD::getEdgeOfBoard(dir, index);
		int curIndex = index;

		while (count > 0 && (board.isEmpty(curIndex + dir) ||
			!board.isColor(curIndex + dir, color))) {
			curIndex += dir;

			if (board.isLegalMove(index, curIndex)) {
				moves.emplace_back(index, curIndex, Flag::NONE, 0);
			}

			if (!board.isEmpty(curIndex) && !board.isColor(curIndex, color)) {
				break;
			}

			count--;
		}
	}
}

// <--> MOVE GENERATION <--> //

// <--> CHECKING IF LEGAL MOVES EXIST <--> //

bool MoveGenerator::legalMovesExist(Board& board) {
	if (board.isGameOver()) {
		return false;
	}

	const int turn = board.getTurn();
	const std::vector<int>& piecePositions = board.getPiecePositions(turn);

	for (const int& piecePosition : piecePositions) {
		if (piecePosition == -1) {
			continue;
		}

		switch (board.getPieceType(piecePosition)) {
			case Piece::PAWN: {
				if (checkPawnMoves(board, piecePosition)) {
					return true;
				}
				break;
			}
			case Piece::BISHOP: {
				if (checkBishopMoves(board, piecePosition)) {
					return true;
				}
				break;
			}
			case Piece::ROOK: {
				if (checkRookMoves(board, piecePosition)) {
					return true;
				}
				break;
			}
			case Piece::QUEEN: {
				if (checkQueenMoves(board, piecePosition)) {
					return true;
				}
				break;
			}
			case Piece::KING: {
				if (checkKingMoves(board, piecePosition) || checkCastleMoves(board, piecePosition)) {
					return true;
				}
				break;
			}
			case Piece::KNIGHT: {
				if (checkKnightMoves(board, piecePosition)) {
					return true;
				}
				break;
			}
		}
	}

	return false;
}

bool MoveGenerator::checkPawnMoves(Board& board, const int index) {
	const int color = board.getPieceColor(index);
	const std::vector<int>& dir = PGD::getPieceDirections(Piece::create(Piece::PAWN, color));

	// see if pawn can move forward
	if (PGD::getEdgeOfBoard(dir[0], index) > 0 && board.isEmpty(index + dir[0]) &&
		board.isLegalMove(index, index + dir[0])) {
		return true;
	}

	// if pawn can go forward 6 times it has not moved yet, check if moving 2 tiles is also possible
	if (PGD::getEdgeOfBoard(dir[0], index) == 6 && board.isEmpty(index + dir[0]) &&
		board.isEmpty(index + (dir[0] * 2)) &&
		board.isLegalMove(index, index + (dir[0] * 2))) {
		return true;
	}

	// look sideways for captures
	for (int side = 1; side < dir.size(); side++) {
		if (PGD::getEdgeOfBoard(dir[side], index) == 0) {
			continue;
		}

		// check for regular capture
		if (!board.isEmpty(index + dir[side]) && !board.isColor(index + dir[side], color)
			&& board.isLegalMove(index, index + dir[side])) {
			// if a pawn can only move forward once before hitting the edge, he is going to promote
			return true;
		}

		// from this point on we check for en passant, don't waste time if it's not
		// possible
		if (board.getEnPassant() == 0) {
			continue;
		}

		// if we get here, en passant is possible on the board, so check if this pawn can take it
		if (const int enPassantCapt = board.getEnPassant() + dir[0];
			enPassantCapt == index + dir[side] && board.isLegalMove(index, index + dir[side])) {
			return true;
		}
	}

	return false;
}

bool MoveGenerator::checkKnightMoves(Board& board, const int index) {
	return checkDirectionOnce(board, index, PGD::getPieceDirections(Piece::KNIGHT));
}

bool MoveGenerator::checkBishopMoves(Board& board, const int index) {
	return checkDirectionUntilCollision(board, index, PGD::getPieceDirections(Piece::BISHOP));
}

bool MoveGenerator::checkRookMoves(Board& board, const int index) {
	return checkDirectionUntilCollision(board, index,
		PGD::getPieceDirections(Piece::ROOK));
}

bool MoveGenerator::checkQueenMoves(Board& board, const int index) {
	return checkDirectionUntilCollision(board, index,
		PGD::getPieceDirections(Piece::QUEEN));
}

bool MoveGenerator::checkKingMoves(Board& board, const int index) {
	return checkDirectionOnce(board, index, PGD::getPieceDirections(Piece::KING));
}
bool MoveGenerator::checkCastleMoves(Board& board, const int index) {
	const int color = board.getTurn();

	if (!board.canCastleKSide(color) && !board.canCastleQSide(color)) {
		return false;
	}

	// if the tiles between king and rook are empty
	if (board.isEmpty(index + 1) && board.isEmpty(index + 2) &&
		board.canCastleKSide(color)) {
		// if there is a piece at the end, and it is your rook, which hasn't moved
		if (!board.isEmpty(index + 3) && board.isRook(index + 3) &&
			board.isColor(index + 3, color)) {
			// if move is legal
			if (board.isLegalMove(index, index + 1) &&
				board.isLegalMove(index, index + 2)) {
				return true;
			}
		}
	}

	// if the tiles between king and rook are empty
	if (board.isEmpty(index - 1) && board.isEmpty(index - 2) &&
		board.isEmpty(index - 3) && board.canCastleQSide(color)) {
		// if there is a piece at the end, and it is your rook, which hasn't moved
		if (!board.isEmpty(index - 4) && board.isRook(index - 4) &&
			board.isColor(index - 4, color)) {
			// if move is legal
			if (board.isLegalMove(index, index - 1) &&
				board.isLegalMove(index, index - 2)) {
				return true;
			}
		}
	}

	return false;
}

bool MoveGenerator::checkDirectionOnce(Board& board, const int index, const std::vector<int>& directions) {
	const int color = board.getTurn();

	for (const int& dir : directions) {
		const int count = PGD::getEdgeOfBoard(dir, index);

		if (count > 0 &&
			(board.isEmpty(index + dir) || !board.isColor(index + dir, color))) {
			if (board.isLegalMove(index, index + dir)) {
				return true;
			}
		}
	}

	return false;
}

bool MoveGenerator::checkDirectionUntilCollision(Board& board, const int index, const std::vector<int>& directions) {
	const int color = board.getTurn();

	for (const int& dir : directions) {
		int count = PGD::getEdgeOfBoard(dir, index);
		int curIndex = index;

		while (count > 0 && (board.isEmpty(curIndex + dir) ||
			!board.isColor(curIndex + dir, color))) {
			curIndex += dir;

			if (board.isLegalMove(index, curIndex)) {
				return true;
			}

			if (!board.isEmpty(curIndex) && !board.isColor(curIndex, color)) {
				break;
			}

			count--;
		}
	}

	return false;
}

// <--> CHECKING IF LEGAL MOVES EXIST <--> //
