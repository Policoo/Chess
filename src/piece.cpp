#include "piece.h"

#include <iostream>

int Piece::create(const int type, const int color) {
    return (type << 1) | color;
}

int Piece::type(const int piece) {
    return piece >> 1;
}

int Piece::color(const int piece) {
    return piece & COLOR_MASK;
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
