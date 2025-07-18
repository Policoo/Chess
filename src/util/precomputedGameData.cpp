#include "precomputedGameData.h"
#include "../piece.h"
#include "utils.h"

#include <iostream>

// <--> HELPER FUNCTIONS <--> //

uint64_t computeBetween(int from, int to) {
    int df = (to & 7) - (from & 7);
    int dr = (to >> 3) - (from >> 3);
    int sf = (df == 0 ? 0 : df/abs(df));
    int sr = (dr == 0 ? 0 : dr/abs(dr));
    if ((sf && sr && abs(df) != abs(dr)) || (sf==0 && sr==0))
        return 0ULL;
    int dir = sf + sr*8;
    uint64_t mask = 0ULL;
    for (int sq = from + dir; sq != to; sq += dir)
        mask |= 1ULL << sq;
    return mask;
}

// <--> HELPER FUNCTIONS <--> //

std::unordered_map<int, std::array<int, 64>> PGD::edgeOfBoard = initializeEdgeOfBoard();
std::unordered_map<int, std::vector<int>> PGD::pieceDirections = initializePieceDirections();
std::unordered_map<int, std::array<uint64_t, 64>> PGD::attackMapBitboards = initializeAttackMapBitboards();
std::unordered_map<int, std::array<uint64_t, 64>> PGD::relevantMasks = initializeRelevantMasks();
std::unordered_map<int, std::unordered_map<int, std::unordered_map<uint64_t, uint64_t>>> PGD::pieceLookupTable = initializePieceLookupTable();
const std::array<std::array<uint64_t, 64>, 64> PGD::squaresBetween = initializeSquaresBetween();
const std::array<uint8_t, 64> PGD::castleMask = initializeCastleMask();

const int& PGD::getEdgeOfBoard(const int direction, const int index) {
	return edgeOfBoard[direction][index];
}

const std::vector<int>& PGD::getPieceDirections(int piece) {
    piece = (Piece::type(piece) == Piece::PAWN) ? piece : Piece::create(Piece::type(piece), Piece::WHITE);
	return pieceDirections[piece];
}

const uint64_t& PGD::getAttackMap(int piece, const int piecePosition) {
    piece = (Piece::type(piece) == Piece::PAWN) ? piece : Piece::create(Piece::type(piece), Piece::WHITE);
	return attackMapBitboards[piece][piecePosition];
}

const uint64_t& PGD::getPseudoMoves(const int pieceType,
                                    const int piecePosition,
                                    const uint64_t blockerBitboard) {
    if (pieceType == Piece::QUEEN) {
        // get the rook-style sliding moves
        const uint64_t rookMoves =
          getPseudoMoves(Piece::ROOK, piecePosition, blockerBitboard);
        // get the bishop-style sliding moves
        const uint64_t bishopMoves =
          getPseudoMoves(Piece::BISHOP, piecePosition, blockerBitboard);

        // combine them
        static uint64_t queenMoves;
        queenMoves = rookMoves | bishopMoves;
        return queenMoves;
    }

    uint64_t relevant = getAttackMap(Piece::create(pieceType, Piece::WHITE),
                                     piecePosition);

    if (pieceType == Piece::ROOK) {
        uint64_t sqBB = 1ULL << piecePosition;
        if ((sqBB & FILE_A) == 0) relevant &= ~FILE_A;
        if ((sqBB & FILE_H) == 0) relevant &= ~FILE_H;
        if ((sqBB & RANK_1) == 0) relevant &= ~RANK_1;
        if ((sqBB & RANK_8) == 0) relevant &= ~RANK_8;
    } else {
        // bishop falls into the “else” here
        relevant &= ~EDGE_RING;
    }

    const uint64_t key = blockerBitboard & relevant;
    return pieceLookupTable[pieceType][piecePosition][key];
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

	map[Piece::create(Piece::KING, Piece::WHITE)] = std::vector<int>({ -9, -8, -7, -1, 1, 7, 8, 9 });
	map[Piece::create(Piece::QUEEN, Piece::WHITE)] = std::vector<int>({ -9, -8, -7, -1, 1, 7, 8, 9 });
	map[Piece::create(Piece::BISHOP, Piece::WHITE)] = std::vector<int>({ -9, -7, 7, 9 });
	map[Piece::create(Piece::ROOK, Piece::WHITE)] = std::vector<int>({ -8, -1, 1, 8 });
	map[Piece::create(Piece::KNIGHT, Piece::WHITE)] = std::vector<int>({ -17, -15, -10, -6, 6, 10, 15, 17 });
	map[Piece::create(Piece::PAWN, Piece::WHITE)] = std::vector<int>({ -8, -9, -7 });
	map[Piece::create(Piece::PAWN, Piece::BLACK)] = std::vector<int>({ 8, 9, 7 });

	return map;
}

std::unordered_map<int, std::array<uint64_t, 64>> PGD::initializeAttackMapBitboards() {
	std::unordered_map<int, std::array<uint64_t, 64>> map;

	//attack maps for king
	std::vector<int> directions = pieceDirections[Piece::create(Piece::KING, Piece::WHITE)];
	for (int i = 0; i < 64; i++) {
		uint64_t bitboard = 0;

		for (int dir : directions) {
			//if the piece wouldn't go off the board, it can attack
			if (edgeOfBoard[dir][i] > 0) {
				bitboard |= (1LL << (i + dir));
			}
		}

		map[Piece::create(Piece::KING, Piece::WHITE)][i] = bitboard;
	}

	//attack maps for queen
	directions = pieceDirections[Piece::create(Piece::QUEEN, Piece::WHITE)];
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

		map[Piece::create(Piece::QUEEN, Piece::WHITE)][i] = bitboard;
	}

	//attack maps for rook
	directions = pieceDirections[Piece::create(Piece::ROOK, Piece::WHITE)];
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

		map[Piece::create(Piece::ROOK, Piece::WHITE)][i] = bitboard;
	}

	//attack maps for bishop
	directions = pieceDirections[Piece::create(Piece::BISHOP, Piece::WHITE)];
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

	    map[Piece::create(Piece::BISHOP, Piece::WHITE)][i] = bitboard;
	}

	//attack maps for knight
	directions = pieceDirections[Piece::create(Piece::KNIGHT, Piece::WHITE)];
	for (int i = 0; i < 64; i++) {
		uint64_t bitboard = 0;

		for (int dir : directions) {
			//if the piece wouldn't go off the board, it can attack
			if (edgeOfBoard[dir][i] > 0) {
				bitboard |= (1LL << (i + dir));
			}
		}

		map[Piece::create(Piece::KNIGHT, Piece::WHITE)][i] = bitboard;
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

std::unordered_map<int, std::unordered_map<int, std::unordered_map<uint64_t, uint64_t>>> PGD::initializePieceLookupTable() {
    //TODO: This shit is cancerous, change to magic bitboards
    static std::unordered_map<int, std::unordered_map<int, std::unordered_map<uint64_t, uint64_t>>> map;

    for (auto const& [piece, attackMapBitboards] : attackMapBitboards) {
        //queens take a long time to calculate, and we can just & rook and bishop at runtime instead
        if (Piece::type(piece) == Piece::QUEEN) {
            continue;
        }

        //knight/king lookup table is the same as their respective attack maps
        if (Piece::type(piece) == Piece::KNIGHT || Piece::type(piece) == Piece::KING) {
            continue;
        }

        //we handle pawns differently
        if (Piece::type(piece) == Piece::PAWN) {
            continue;
        }

        //generate pseudo-legal moves for sliding pieces
        for (int piecePos = 0; piecePos < 64; piecePos++) {
            const uint64_t& attackMap = attackMapBitboards[piecePos];

            //go through each possible blocker configuration for an attack map
            for (uint64_t blockerBitboard : generateBlockerBitboards(attackMap, Piece::type(piece) == Piece::ROOK, piecePos)) {
                uint64_t pseudoLegalMovesBitboard = 0LL;

                //go in each direction and mark the pseudo-legal moves as 1 on the bitboard
                for (int dir : getPieceDirections(piece)) {
                    const int numSteps = edgeOfBoard[dir][piecePos];
                    int curIndex = piecePos;

                    for (int step = numSteps; step > 0; step--) {
                        curIndex += dir;
                        pseudoLegalMovesBitboard |= (1LL << curIndex);

                        //if this square that we just added has a blocker, stop
                        if ((blockerBitboard & (1LL << curIndex)) > 0) {
                           break;
                        }
                    }
                }

                map[Piece::type(piece)][piecePos][blockerBitboard] = pseudoLegalMovesBitboard;
            }
        }
    }

    return map;
}

std::array<std::array<uint64_t, 64>, 64> PGD::initializeSquaresBetween() {
    std::array<std::array<uint64_t, 64>, 64> array{};

    for (int a = 0; a < 64; ++a) {
        for (int b = 0; b < 64; ++b) {
            array[a][b] = computeBetween(a, b);
        }
    }

    return array;
}

std::vector<uint64_t> PGD::generateBlockerBitboards(const uint64_t attackMap, const bool isRook, const int piecePos) {
    std::vector<int> moveSquareIndices;

    //take out the edges to reduce the size of the lookup table
    uint64_t relevant = attackMap;

    if (isRook) {
        uint64_t sqBB = 1LL << piecePos;
        if ((sqBB & FILE_A) == 0) relevant &= ~FILE_A; // drop far south edge
        if ((sqBB & FILE_H) == 0) relevant &= ~FILE_H; // drop far north edge
        if ((sqBB & RANK_1) == 0) relevant &= ~RANK_1; // drop far west edge
        if ((sqBB & RANK_8) == 0) relevant &= ~RANK_8; // drop far east edge
    } else {
        relevant = attackMap & ~EDGE_RING;
    }


    //create a list of the indices of the bits that are set in the movement mask
    for (int i = 0; i < 64; i++) {
        if (((relevant >> i) & 1) == 1) {
           moveSquareIndices.push_back(i);
        }
    }

    //calculate total number of blocker bitboards (2^n)
    const int numPatterns = 1 << moveSquareIndices.size();
    std::vector<uint64_t> blockerBitboards(numPatterns);

    //create all bitboards
    for (int patternIndex = 0; patternIndex < numPatterns; patternIndex++) {
        for (int bitIndex = 0; bitIndex < moveSquareIndices.size(); bitIndex++) {
            const int bit = (patternIndex >> bitIndex) & 1;
            blockerBitboards[patternIndex] |= static_cast<uint64_t>(bit) << moveSquareIndices[bitIndex];
        }
    }

    return blockerBitboards;
}

std::unordered_map<int, std::array<uint64_t, 64>> PGD::initializeRelevantMasks() {
    std::unordered_map<int, std::array<uint64_t, 64>> map;

    // Build a per-square “relevant occupancy” mask for every sliding piece
    for (const auto& [pieceKey, attackArrays] : attackMapBitboards) {

        int baseType = Piece::type(pieceKey);
        if (baseType == Piece::KNIGHT || baseType == Piece::KING || baseType == Piece::PAWN || baseType == Piece::QUEEN)
            continue;                           // only rook, bishop (and optionally queen)

        const auto& dirs = pieceDirections[pieceKey];

        for (int sq = 0; sq < 64; ++sq) {
            uint64_t mask = attackArrays[sq];   // full attack ray bitboard

            // Remove the edge square on every ray
            for (int dir : dirs) {
                int toEdge = edgeOfBoard[dir][sq];
                int edgeSq = sq + dir * toEdge; // last square in this direction
                mask &= ~(1ULL << edgeSq);
            }

            map[pieceKey][sq] = mask;
        }
    }

    return map;
}

std::array<uint8_t, 64> PGD::initializeCastleMask() {
    std::array<uint8_t, 64> mask;
    mask.fill(0b1111);  // by default don’t clear any rights

    //black
    mask[0]  = 0b0111;
    mask[7]  = 0b1011;
    mask[4]  = 0b0011;

    //white
    mask[56] = 0b1101;
    mask[63] = 0b1110;
    mask[60] = 0b1100;

    return mask;
}