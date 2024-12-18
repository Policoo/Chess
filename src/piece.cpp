#include "piece.h"

#include <iostream>

int Piece::create(const int type, const int color) {
    return type | color;
}

int Piece::type(const int piece) {
    return piece & TYPE_MASK;
}

int Piece::color(const int piece) {
    return piece & COLOR_MASK;
}

int Piece::index(const int piece) {
    return (piece & INDEX_MASK) >> 5;
}

int Piece::ignoreIndex(const int piece) {
    return piece & (TYPE_MASK | COLOR_MASK);
}

int Piece::setIndex(const int piece, const int index) {
    return piece | (index << 5);
}

char Piece::toString(const int piece) {
    const int color = Piece::color(piece);
    const int type = Piece::type(piece);

    char pieceStr = ' ';
    switch (type) {
        case PAWN:
            pieceStr = 'P';
            break;
        case KNIGHT:
            pieceStr = 'N';
            break;
        case BISHOP:
            pieceStr = 'B';
            break;
        case ROOK:
            pieceStr = 'R';
            break;
        case QUEEN:
            pieceStr = 'Q';
            break;
        case KING:
            pieceStr = 'K';
            break;
    }
    pieceStr = static_cast<char>((color == BLACK) ? tolower(pieceStr) : pieceStr);
    return pieceStr;
}
