#pragma once

#include <string>
#include "piece.h"

enum class Flag {
    NONE,
    CASTLE,
    EN_PASSANT,
    PROMOTION
};

class Move {
public:
    /**
     * @brief Creates a move object.
     * 
     * @param start - Start square index of the move.
     * @param end - End square index of the move.
     * @param flag - Flag of the move.
     * @param promotion - Promotion piece, only valid if flag is PROMOTION, otherwise
     * ignored.
    */
    Move(int start, int end, Flag flag, int promotion);

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
    int start_;
    int end_;
    Flag flag_;
    int promotion_;
};