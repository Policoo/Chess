#include "move.h"
#include "piece.h"
#include "utils.h"

Move::Move(int start, int end, Flag flag, int promotion) : 
	start_(start),
	end_(end), 
	flag_(flag), 
	promotion_(promotion) {
}

int Move::start() const {
	return start_;
}

int Move::end() const {
	return end_;
}

Flag Move::flag() const {
	return flag_;
}

int Move::promotion() const {
	return promotion_;
}

void Move::setPromotion(const int promotion) {
	promotion_ = promotion;
}

std::string Move::toString() const {
	std::string promotionString;
	if (flag_ == Flag::PROMOTION) {
		promotionString = static_cast<char>(tolower(Piece::toString(promotion_)));
	}
	return getChessCoords(start_) + getChessCoords(end_) + promotionString;
}
