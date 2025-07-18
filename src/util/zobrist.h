#pragma once

#include <array>

class Zobrist {
public:
    static const uint64_t& getKey(int piece, const int index);

    static const uint64_t& getColorKey(const int color);

    static const uint64_t& getCastleKey(const int castleRights);

    static const uint64_t& getEnPassantKey(const int enPassant);

private:
    static std::array<std::array<uint64_t, 64>, 14> keys;
    static std::array<uint64_t, 2> colorKeys;
    static std::array<uint64_t, 16> castleKeys;
    static std::array<uint64_t, 64> enPassantKeys;

    static std::array<std::array<uint64_t, 64>, 14> initializeKeys();

    static std::array<uint64_t, 2> initializeColorKeys();

    static std::array<uint64_t, 16> initializeCastleKeys();

    static std::array<uint64_t, 64> initializeEnPassantKeys();

    static uint64_t generateKey();
};
