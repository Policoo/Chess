#pragma once

#include <string>
#include <unordered_map>
#include <cstdint>

#include "precomputedGameData.h"

std::string bitString(int num);

std::string bitboardString(uint64_t bitboard);

/**
 * Returns the index of the least significant bit (LSB) and clears it
 * @param bitboard where to pop the LSB from
 * @return least significant bit of bitboard
 */
int popLSB(uint64_t& bitboard);

std::string getChessCoords(int index);

int getIndexFromChessCoordinates(char column, char row);

std::unordered_map<std::string, int> parsePerftResults(const std::string& results);

std::unordered_map<std::string, int> stockFishPerft(const std::string& fenString, int depth);

inline uint64_t betweenMask(int a, int b) {
    return PGD::squaresBetween[a][b];
}

inline uint64_t lineMask(int a, int b) {
    return PGD::squaresBetween[a][b] | (1ULL << a) | (1ULL << b);
}

inline uint64_t rankMask(int rank) {
    return 0xFFULL << ((7 - rank) * 8);
}