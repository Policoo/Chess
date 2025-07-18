#include "move.h"
#include "piece.h"
#include "util/utils.h"

Move::Move(const int start, const int end, const Flag flag) :
    from(start),
    to(end),
    moveFlag(flag) {
}

int Move::start() const {
    return from;
}

int Move::end() const {
    return to;
}

Flag Move::flag() const {
    return moveFlag;
}

void Move::setPromotion(const int promotion) {
    switch (promotion) {
        case Piece::ROOK:
            moveFlag = Flag::PROMO_R;
            break;
        case Piece::KNIGHT:
            moveFlag = Flag::PROMO_N;
            break;
        case Piece::BISHOP:
            moveFlag = Flag::PROMO_B;
            break;
        case Piece::QUEEN:
            moveFlag = Flag::PROMO_Q;
            break;
    }
}

std::string Move::toString() const {
    std::string promotionString;

    switch (moveFlag) {
        case Flag::PROMO_B:
            promotionString = "b";
            break;
        case Flag::PROMO_N:
            promotionString = "n";
            break;
        case Flag::PROMO_R:
            promotionString = "r";
            break;
        case Flag::PROMO_Q:
            promotionString = "q";
            break;
        default:
            // not a promotion
            break;
    }

    return getChessCoords(from) + getChessCoords(to) + promotionString;
}
