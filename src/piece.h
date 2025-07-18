#pragma once

/**
 * @brief Pieces are not their own type, instead they are integers. Empty squares are
 * represented by 0. This class encodes and decodes information about a piece integer.
*/
class Piece {
public:
    static constexpr int WHITE = 0;
    static constexpr int BLACK = 1;

    static constexpr int PAWN = 1;
    static constexpr int KING = 2;
    static constexpr int KNIGHT = 3;
    static constexpr int BISHOP = 4;
    static constexpr int ROOK = 5;
    static constexpr int QUEEN = 6;


    /**
     * @brief Creates a piece with the given color and type.
     *
     * @param type Type of the piece, for example Piece::PAWN.
     * @param color Color of the piece (Piece::WHITE or Piece::BLACK).
     * @return Integer representing a piece.
    */
    static int create(const int type, const int color);

    /**
     * @brief Does bit operations on the piece integer to determine its type.
     *
     * @param piece Piece to calculate the type of.
     * @return An integer equal to a piece type, for example Piece::ROOK
    */
    static int type(const int piece);

    /**
     * @brief Does bit operations on the piece integer to get determine its color.
     *
     * @param piece Piece to calculate the color of.
     * @return An integer equal to either Piece::WHITE or Piece::BLACK
    */
    static int color(const int piece);

    /**
     * @brief Determines a char to represent the piece.
     *
     * @param piece - Piece to represent into a char.
     * @return P/N/B/R/Q/K depending on the piece type. Char will be lower case if piece is black.
    */
    static char toString(const int piece);

private:
    static constexpr int TYPE_MASK = 0b1110;
    static constexpr int COLOR_MASK = 0x0001;
};
