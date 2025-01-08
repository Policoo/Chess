#pragma once

#include <array>
#include <vector>
#include <unordered_map>
#include <memory>

/**
 * @brief PGD is short for precomputed game data. This singleton class holds important information
 * about the game that is used across multiple components of the project.
*/
class PGD {
public:
    /**
     * @param direction Direction the piece is/would be going in.
     * @param index Board index
     * @return How many squares you can go in a direction from the given index, until you reach the
     * edge of the board.
    */
    static const int& getEdgeOfBoard(int direction, int index);

    /**
     * @param piece The piece you want the directions for.
     * @return A vector containing all the directions the piece can go in.
    */
    const static std::vector<int>& getPieceDirections(int piece);

    /**
     * @param piece Piece to get the attack map for.
     * @param piecePosition Position index of the piece on the board.
     * @return A bitboard representing what squares the piece can attack from its position.
    */
    static const uint64_t& getAttackMap(int piece, int piecePosition);

private:
    /**
     * HashMap that holds information about how many squares you can go in a direction until you
     * hit the edge of the board. The key is the direction you are going in. The value is an int array, where
     * accessing the element at 0 would tell you how many squares a piece at index 0 on the board can go in a
     * direction, before it hits the edge of the board.
     */
    static std::unordered_map<int, std::array<int, 64> > edgeOfBoard;

    /**
     * HashMap that holds information about which directions each piece can go in. The key is the piece type,
     * or, in case of pawns, the piece type + color (this is because white and black pawns go in different directions).
     * The value is an int array containing all the directions that a piece can go in.
     */
    static std::unordered_map<int, std::vector<int> > pieceDirections;

    /**
     * HashMap that contains all the squares that a sliding piece can see from any given square on the board.
     * The key is the piece type, or, in case of pawns, the piece type + color (this is because white
     * and black pawns go in different directions). The value is a long array. To access a specific bitboard,
     * you ask for the element at the pieceIndex.
     *
     * For example, if I have a pawn on index 0 on the board, I will ask for the element at index 0 in the list.
     * The returned value will be a bitboard representing all the squares that that piece can see.
    */
    static std::unordered_map<int, std::array<uint64_t, 64> > attackMapBitboards;

    static std::unordered_map<int, std::array<int, 64> > initializeEdgeOfBoard();

    static std::unordered_map<int, std::vector<int> > initializePieceDirections();

    static std::unordered_map<int, std::array<uint64_t, 64> > initializeAttackMapBitboards();
};
