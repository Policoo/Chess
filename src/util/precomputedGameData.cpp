#include "precomputedGameData.h"
#include "../piece.h"
#include "utils.h"

#include <iostream>

std::unordered_map<int, std::array<int, 64>> PGD::edgeOfBoard = initializeEdgeOfBoard();
std::unordered_map<int, std::vector<int>> PGD::pieceDirections = initializePieceDirections();
std::unordered_map<int, std::array<uint64_t, 64>> PGD::attackMapBitboards = initializeAttackMapBitboards();

const int& PGD::getEdgeOfBoard(const int direction, const int index) {
	return edgeOfBoard[direction][index];
}

const std::vector<int>& PGD::getPieceDirections(const int piece) {
	const int pieceType = (Piece::type(piece) == Piece::PAWN) ?
		Piece::ignoreIndex(piece) : Piece::type(piece);
	return pieceDirections[pieceType];
}

const uint64_t& PGD::getAttackMap(const int piece, const int piecePosition) {
	const int pieceType = (Piece::type(piece) == Piece::PAWN) ?
		Piece::ignoreIndex(piece) : Piece::type(piece);
	return attackMapBitboards[pieceType][piecePosition];
}

std::unordered_map<int, std::array<int, 64>> PGD::initializeEdgeOfBoard() {
	std::unordered_map<int, std::array<int, 64>> map;

	for (int index = 0; index < 64; index++) {
		//up and left
		int i = index;
		int count = 0;
		while (i - 9 >= 0 && (i / 8) - 1 == (i - 9) / 8) {
			i -= 9;
			count++;
		}
		map[-9][index] = count;

		//up
		i = index;
		count = 0;
		while (i - 8 >= 0) {
			i -= 8;
			count++;
		}
		map[-8][index] = count;

		//up and right
		i = index;
		count = 0;
		while (i - 7 >= 0 && i / 8 != (i - 7) / 8) {
			i -= 7;
			count++;
		}
		map[-7][index] = count;

		//left
		i = index;
		count = 0;
		while (i - 1 >= 0 && i / 8 == (i - 1) / 8) {
			i -= 1;
			count++;
		}
		map[-1][index] = count;

		//right
		i = index;
		count = 0;
		while (i + 1 < 64 && i / 8 == (i + 1) / 8) {
			i += 1;
			count++;
		}
		map[1][index] = count;

		//down and left
		i = index;
		count = 0;
		while (i + 7 < 64 && i / 8 != (i + 7) / 8) {
			i += 7;
			count++;
		}
		map[7][index] = count;

		//down
		i = index;
		count = 0;
		while (i + 8 < 64) {
			i += 8;
			count++;
		}
		map[8][index] = count;

		//down and right
		i = index;
		count = 0;
		while (i + 9 < 64 && (i / 8) + 1 == (i + 9) / 8) {
			i += 9;
			count++;
		}
		map[9][index] = count;

		//for knight
		if (index - 17 >= 0 && index % 8 != 0) {
			map[-17][index] = 1;
		}
		else {
			map[-17][index] = 0;
		}

		if (index - 15 > 0 && index % 8 != 7) {
			map[-15][index] = 1;
		}
		else {
			map[-15][index] = 0;
		}

		if (index - 10 >= 0 && index % 8 > 1) {
			map[-10][index] = 1;
		}
		else {
			map[-10][index] = 0;
		}

		if (index - 6 > 0 && index % 8 < 6) {
			map[-6][index] = 1;
		}
		else {
			map[-6][index] = 0;
		}

		if (index + 17 < 64 && index % 8 != 7) {
			map[17][index] = 1;
		}
		else {
			map[17][index] = 0;
		}

		if (index + 15 < 64 && index % 8 != 0) {
			map[15][index] = 1;
		}
		else {
			map[15][index] = 0;
		}

		if (index + 10 < 64 && index % 8 < 6) {
			map[10][index] = 1;
		}
		else {
			map[10][index] = 0;
		}

		if (index + 6 < 64 && index % 8 > 1) {
			map[6][index] = 1;
		}
		else {
			map[6][index] = 0;
		}
	}

	return map;
}

std::unordered_map<int, std::vector<int>> PGD::initializePieceDirections() {
	std::unordered_map<int, std::vector<int>> map;

	map[Piece::KING] = std::vector<int>({ -9, -8, -7, -1, 1, 7, 8, 9 });
	map[Piece::QUEEN] = std::vector<int>({ -9, -8, -7, -1, 1, 7, 8, 9 });
	map[Piece::BISHOP] = std::vector<int>({ -9, -7, 7, 9 });
	map[Piece::ROOK] = std::vector<int>({ -8, -1, 1, 8 });
	map[Piece::KNIGHT] = std::vector<int>({ -17, -15, -10, -6, 6, 10, 15, 17 });
	map[Piece::create(Piece::PAWN, Piece::WHITE)] = std::vector<int>({ -8, -9, -7 });
	map[Piece::create(Piece::PAWN, Piece::BLACK)] = std::vector<int>({ 8, 9, 7 });

	return map;
}

std::unordered_map<int, std::array<uint64_t, 64>> PGD::initializeAttackMapBitboards() {
	std::unordered_map<int, std::array<uint64_t, 64>> map;

	//attack maps for king
	std::vector<int> directions = pieceDirections[Piece::KING];
	for (int i = 0; i < 64; i++) {
		uint64_t bitboard = 0;

		for (int dir : directions) {
			//if the piece wouldn't go off the board, it can attack
			if (edgeOfBoard[dir][i] > 0) {
				bitboard |= (1LL << (i + dir));
			}
		}

		map[Piece::KING][i] = bitboard;
	}

	//attack maps for queen
	directions = pieceDirections[Piece::QUEEN];
	for (int i = 0; i < 64; i++) {
		uint64_t bitboard = 0;

		for (int dir : directions) {
			const int numSteps = edgeOfBoard[dir][i];
			int curIndex = i;

			for (int step = numSteps; step > 0; step--) {
				curIndex += dir;
				bitboard |= (1LL << curIndex);
			}
		}

		map[Piece::QUEEN][i] = bitboard;
	}

	//attack maps for rook
	directions = pieceDirections[Piece::ROOK];
	for (int i = 0; i < 64; i++) {
		uint64_t bitboard = 0;

		for (int dir : directions) {
			const int numSteps = edgeOfBoard[dir][i];
			int curIndex = i;

			for (int step = numSteps; step > 0; step--) {
				curIndex += dir;
				bitboard |= (1LL << curIndex);
			}
		}

		map[Piece::ROOK][i] = bitboard;
	}

	//attack maps for bishop
	directions = pieceDirections[Piece::BISHOP];
	for (int i = 0; i < 64; i++) {
		uint64_t bitboard = 0;

		for (int dir : directions) {
			const int numSteps = edgeOfBoard[dir][i];
			int curIndex = i;

			for (int step = numSteps; step > 0; step--) {
				curIndex += dir;
				bitboard |= (1LL << curIndex);
			}
		}
	}

	//attack maps for knight
	directions = pieceDirections[Piece::KNIGHT];
	for (int i = 0; i < 64; i++) {
		uint64_t bitboard = 0;

		for (int dir : directions) {
			//if the piece wouldn't go off the board, it can attack
			if (edgeOfBoard[dir][i] > 0) {
				bitboard |= (1LL << (i + dir));
			}
		}

		map[Piece::KNIGHT][i] = bitboard;
	}

	//attack maps for white pawn
	directions = pieceDirections[Piece::create(Piece::PAWN, Piece::WHITE)];
	for (int i = 0; i < 64; i++) {
		uint64_t bitboard = 0;

		for (int dir = 1; dir < 3; dir++) {
			//if the piece wouldn't go off the board, it can attack
			if (edgeOfBoard[directions[dir]][i] > 0) {
				bitboard |= (1LL << (i + directions[dir]));
			}
		}

		map[Piece::create(Piece::PAWN, Piece::WHITE)][i] = bitboard;
	}

	//attack maps for black pawn
	directions = pieceDirections[Piece::create(Piece::PAWN, Piece::BLACK)];
	for (int i = 0; i < 64; i++) {
		uint64_t bitboard = 0;

		for (int dir = 1; dir < 3; dir++) {
			//if the piece wouldn't go off the board, it can attack
			if (edgeOfBoard[directions[dir]][i] > 0) {
				bitboard |= (1LL << (i + directions[dir]));
			}
		}

		map[Piece::create(Piece::PAWN, Piece::BLACK)][i] = bitboard;
	}

	return map;
}
