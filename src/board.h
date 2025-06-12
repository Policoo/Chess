#pragma once

#include <string>
#include <unordered_map>
#include <vector>
#include <array>
#include <cstdint>

#include "move.h"

class Board {
public:
    /**
     * Creates a new board. If fenString is given, board will be made from that.
     *
     * @param fenString optional parameter.
     */
    explicit Board(const std::string &fenString = "");

    /**
     * Makes a move on the board.
     *
     * @param move - Move you want to make
     * @param skipGameOverCheck - Optional parameter, default = false
     */
    void makeMove(const Move &move, bool skipGameOverCheck = false);

    /**
     * Undoes a move on the board by moving the pieces back and restoring past game state variables.
     *
     * @param move - move to be unmade.
     */
    void undoMove(const Move &move);

    /**
     * @brief Determines if the move is legal by seeing if the king is in check or if the piece is pinned.
     * @param start  Start index of piece.
     * @param end  End index of piece.
     * @return True if move is legal, false otherwise.
    */
    bool isLegalMove(int start, int end, bool isEnPassant = false);

    /**
     * @brief Determines if the player, whose turn it is, is in check. Does so by scanning outwards from the
     * king. It is a little inefficient, but this should only get called in the GUI, so it's fine.
     *
     * @return true if the king is in check, false otherwise.
     */
    bool isCheck() const;

    /**
     * Goes through the board to get the coordinates of the king of the
     * given color.
     *
     * @param color color of the king you want the coordinates of.
     * @return an array containing the x and y-axis coordinates of the king.
     */
    int getKingIndex(int color);


    bool isEmpty(int index);

    int getPieceType(int index);

    int getPieceColor(int index);


    bool isKing(int index);

    bool isQueen(int index);

    bool isRook(int index);

    bool isBishop(int index);

    bool isKnight(int index);

    bool isPawn(int index);

    bool isColor(int index, int color);


    bool canCastleKSide(int color) const;

    bool canCastleQSide(int color) const;


    int getTurn() const;

    int getEnPassant() const;

    bool isGameOver() const;

    std::vector<int> &getPiecePositions(int color);


    std::string positionToFen();

    std::string toString();

    std::string debugString();

private:
    /**
     * @brief Holds information from current board state that can be used to undo a move.
    */
    struct BoardState {
        int targetTile;
        int castleRights;
        int enPassant;
        int enPassantIndex;
        int lastCaptOrPawnAdv;
    };

    std::array<int, 64> tile;

    std::vector<BoardState> boardState;
    std::unordered_map<int, int> remainingPieces;
    std::unordered_map<int, int> kingPositions;
    std::unordered_map<uint64_t, int> positionHistory;

    std::unordered_map<int, uint64_t> attackMap;
    std::unordered_map<int, std::vector<uint64_t> > attackedTiles;
    std::unordered_map<int, std::vector<uint64_t> > pins;
    std::unordered_map<int, std::vector<int> > piecePositions;


    int enPassant;
    int castleRights;

    int currentMove;
    int lastCaptOrPawnAdv;
    int turn;

    uint64_t check;
    uint64_t doubleCheck;

    bool gameOver;
    uint64_t hash;

    /**
     * Sets up board for the start position.
     */
    void startPos();

    /**
     * Sets up the board from the given fen string.
     *
     * @param fenString fen string for the board.
     */
    void makeBoardFromFen(const std::string &fenString);

    void initializePiecePositions();

    void initializeAttackTiles();

    void initializePinLines();

    /**
     * Updates the gameOver variable according to the current game state
     */
    void checkGameOver();

    /**
     * Determines if the king is in check and has no legal moves.
     *
     * @return true if its checkmate, false otherwise.
     */
    bool isCheckMate();

    /**
     * Determines if there is a stalemate.
     *
     * @return true if it's stalemate, false otherwise.
     */
    bool isStaleMate();

    /**
     * Determines if the same position has been reached 3 times.
     *
     * @return true if it's the 3 repetition rule, false otherwise.
     */
    bool isRepetition();

    /**
     * Checks if there has been no capture or pawn advancement in the last 50 moves.
     *
     * @return true if there are no captures or pawn advancements in the last 50 moves,
     * false otherwise.
     */
    bool fiftyMoveRule() const;

    /**
     * Checks if there is sufficient material on the board for a checkmate.
     *
     * @return true if there is insufficient material, false otherwise.
     */
    bool insufficientMaterial();

    /**
     * Updates variables that keeps track of game state, namely colorToMove, positionHistory, currentMove,
     * enPassant. Also keeps the hash up to date.
     */
    void updateGameState(const int start, const int end, bool skipGameOverCheck);

    /**
     * Reverts variables that keep track of the game state to the last move state.
     */
    void revertGameStats(const int start, const int end, const Board::BoardState &state);

    /**
     * @brief Updates the attack map for all pieces that saw the move and for the moved piece.
     *
     * @param oldIndex - Old board index of the moved piece.
     * @param newIndex - New board index of the moved piece.
    */
    void updateAttackedTiles(const int oldIndex, const int newIndex);

    /**
     * @brief Updates the pins map with any new pins and deletes old ones.
    */
    void updatePins();

    /**
     * Calculates a bitboard of what the piece at index can see.
     * @param index - Board index where the piece is.
     * @return bitboard where a square seen by the piece is set to 1.
     */
    uint64_t calculateAttackedTiles(int index);

    /**
     * Calculates a bitboard of a pin line starting from this piece.
     * @param index - Board index where the piece is.
     * @return bitboard containing the pin line, or 0L if there is no pin line.
     */
    uint64_t calculatePinLine(const int index);

    /**
     * Determines if the king is in check. If yes, the "check" and "doubleCheck" variables will become
     * a bitboard, representing the line of sight of a piece that can see the king.
    */
    void determineCheckLine();

    /**
     * Makes the current position into a unique, or at least close to unique, hash.
     */
    void hashPosition();
};
