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
    static constexpr uint64_t EDGE_RING = 0xFF818181818181FF;

    static inline constexpr uint64_t FILE_A = 0x0101010101010101ULL;
    static inline constexpr uint64_t FILE_H = 0x8080808080808080ULL;
    static inline constexpr uint64_t RANK_1 = 0x00000000000000FFULL;
    static inline constexpr uint64_t RANK_8 = 0xFF00000000000000ULL;

    static const std::array<std::array<uint64_t, 64>, 64> squaresBetween;
    static const std::array<uint8_t, 64> castleMask;

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

    //TODO: WRITE DOCS
    /**
     *
     * @param pieceType
     * @param piecePosition
     * @param blockerBitboard
     * @return
     */
    static const uint64_t& getPseudoMoves(int pieceType, int piecePosition, uint64_t blockerBitboard);

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
    static std::unordered_map<int, std::vector<int>> pieceDirections;

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

    static std::unordered_map<
        int,                                              // piece type
        std::unordered_map<
            int,                                          // square 0–63
            std::unordered_map<
                uint64_t,                                 // blocker bitboard
                uint64_t                                  // attack bitboard
            >
        >
    > pieceLookupTable;

    static std::unordered_map<int, std::array<uint64_t, 64>> relevantMasks;

    static std::unordered_map<int, std::array<int, 64> > initializeEdgeOfBoard();

    static std::unordered_map<int, std::vector<int> > initializePieceDirections();

    static std::unordered_map<int, std::array<uint64_t, 64> > initializeAttackMapBitboards();

    static std::unordered_map<int, std::unordered_map<int, std::unordered_map<uint64_t, uint64_t>>> initializePieceLookupTable();

    static std::array<std::array<uint64_t, 64>, 64> initializeSquaresBetween();

    static std::vector<uint64_t> generateBlockerBitboards(uint64_t attackMap, bool isRook, int piecePos);

    static std::unordered_map<int, std::array<uint64_t, 64>> initializeRelevantMasks();

    static std::array<uint8_t, 64> initializeCastleMask();
};
