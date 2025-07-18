#pragma once

#include <string>
#include "piece.h"

enum Flag : uint8_t {
    QUIET = 0,
    CAPTURE = 1,
    EN_PASSANT = 2,
    DBL_PUSH = 3,
    PROMO_N = 4, PROMO_B = 5, PROMO_R = 6, PROMO_Q = 7,
    CASTLE_K = 8, CASTLE_Q = 9
};


class Move {
public:
    /**
     * @brief Creates a move object.
     *
     * @param start - Start square index of the move.
     * @param end - End square index of the move.
     * @param flag - Flag of the move.
     * ignored.
    */
    Move(int start, int end, Flag flag);

    Move() = default;

    /**
     * @return The start index of the move.
    */
    int start() const;

    /**
     * @return The end index of the move.
    */
    int end() const;

    /**
     * @return The flag of the move.
    */
    Flag flag() const;

    /**
     * @return The promotion type of the move.
    */
    int promotion() const;

    /**
     * @brief Sets the promotion type of the move.
     *
     * @param promotion - Piece type of the promotion.
    */
    void setPromotion(const int promotion);

    /**
     * @return String representation of the move. If move is a promotion, the promotion
     * type is also printed. Example return value: "a2a4".
    */
    std::string toString() const;

private:
    int from;
    int to;
    Flag moveFlag;
};
