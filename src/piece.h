#pragma once

/**
 * @brief Pieces are not their own type, instead they are integers. Empty squares are
 * represented by 0. This class encodes and decodes information about a piece integer.
*/
class Piece {
public:
    static constexpr int PAWN = 1;
    static constexpr int KNIGHT = 2;
    static constexpr int KING = 3;
    static constexpr int BISHOP = 4;
    static constexpr int ROOK = 5;
    static constexpr int QUEEN = 6;

    static constexpr int WHITE = 8;
    static constexpr int BLACK = 16;

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
     * @brief Does bit operations on the piece integer to determine the index associated
     * with it. Accessing arrays in the board class with this index will lead to information
     * about this piece, such as it's position and attack map.
     *
     * @param piece Piece to calculate the index of.
     * @return An integer equal to a piece type, for example Piece::ROOK
    */
    static int index(const int piece);

    /**
     * @brief Does bit operations on the piece integer to determine its color and type.
     *
     * @param piece Piece to get the color and type of.
     * @return An piece integer with all the same properties as the given piece, except for
     * the index.
    */
    static int ignoreIndex(const int piece);

    /**
     * @brief Does bit operations on the piece integer to encode an index in it.
     *
     * @param piece - Piece integer to encode the index into
     * @param index - Index to encode into the piece integer.
     * @return A piece integer with the same color and type, but with the index also
     * encoded inside.
    */
    static int setIndex(const int piece, const int index);

    /**
     * @brief Determines a char to represent the piece.
     * 
     * @param piece - Piece to represent into a char.
     * @return P/N/B/R/Q/K depending on the piece type. Char will be lower case if piece is black.
    */
    static char toString(const int piece);

private:
    static constexpr int TYPE_MASK = 0x7;
    static constexpr int COLOR_MASK = 0x18;
    static constexpr int INDEX_MASK = 0x3E0;
};