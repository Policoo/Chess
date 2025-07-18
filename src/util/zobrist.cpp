#include <random>
#include <iostream>

#include "zobrist.h"
#include "../piece.h"

std::array<std::array<uint64_t, 64>, 14> Zobrist::keys = initializeKeys();
std::array<uint64_t, 2> Zobrist::colorKeys = initializeColorKeys();
std::array<uint64_t, 16> Zobrist::castleKeys = initializeCastleKeys();
std::array<uint64_t, 64> Zobrist::enPassantKeys = initializeEnPassantKeys();

const uint64_t& Zobrist::getKey(int piece, const int index) {
    return keys[piece][index];
}

const uint64_t& Zobrist::getColorKey(const int color) {
    return colorKeys[color];
}

const uint64_t& Zobrist::getCastleKey(const int castleRights) {
    return castleKeys[castleRights];
}

const uint64_t& Zobrist::getEnPassantKey(const int enPassant) {
    return enPassantKeys[enPassant];
}

uint64_t Zobrist::generateKey() {
    std::random_device rd;
    std::mt19937_64 gen(rd());
    std::uniform_int_distribution<uint64_t> dis;

    return dis(gen);
}

std::array<std::array<uint64_t, 64>, 14> Zobrist::initializeKeys() {
    std::array<std::array<uint64_t, 64>, 14> arr{};

    for (auto& pieceKey: arr) {
        for (uint64_t& piecePositionKey: pieceKey) {
            piecePositionKey = generateKey();
        }
    }

    return arr;
}

std::array<uint64_t, 2> Zobrist::initializeColorKeys() {
    std::array<uint64_t, 2> arr{};

    for (uint64_t& index: arr) {
        index = generateKey();
    }

    return arr;
}

std::array<uint64_t, 16> Zobrist::initializeCastleKeys() {
    std::array<uint64_t, 16> arr{};

    for (uint64_t& index: arr) {
        index = generateKey();
    }

    return arr;
}

std::array<uint64_t, 64> Zobrist::initializeEnPassantKeys() {
    std::array<uint64_t, 64> arr{};

    for (uint64_t& right: arr) {
        right = generateKey();
    }

    //index 0 is no en passant, so make it zero so its harmless to ^ it
    arr[0] = 0ULL;

    return arr;
}
