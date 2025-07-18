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
     * @param flag
     * @return True if move is legal, false otherwise.
    */
    bool isLegalMove(int start, int end, Flag flag);

    /**
     * @brief Determines if the player, whose turn it is, is in check. Does so by scanning outwards from the
     * king. It is a little inefficient, but this should only get called in the GUI, so it's fine.
     *
     * @return true if the king is in check, false otherwise.
     */
    bool isCheck() const;

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

    uint64_t getCheck() const {
        return check;
    }

    uint64_t getCheckers() const {
        return checkers;
    }

    bool isGameOver() const;

    uint64_t &getPiecePositions(int piece);

    uint64_t& getPiecePositionsColor(int color);

    uint64_t& getPieceAttackMap(int position, int color);

    uint64_t& getColorAttackMap(int color);

    uint64_t& getPins(int index, int color);

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
        int lastCaptOrPawnAdv;
    };

    std::array<int, 64> tile;

    std::vector<BoardState> boardState;
    std::unordered_map<uint64_t, int> positionHistory;

    //TODO: First two slots are useless here, see if we can get rid of them (maybe we only access this through a func?)
    std::array<uint64_t, 14> piecePositions;
    std::array<uint64_t, 2> piecePositionsColor;
    std::array<uint64_t, 2> attackMap;

    //first 64 is white, second 64 is black. Because of the values of the color we can access with index + color
    std::array<uint64_t, 128> attackedTiles;
    std::array<uint64_t, 128> pins;

    //small helper function for accessing the indices for the arrays above
    static int indexWithColor(const int index, const int color) {
        return index + color * 64;
    }

    int enPassant;
    int castleRights;

    int currentMove;
    int lastCaptOrPawnAdv;
    int turn;

    uint64_t check;
    uint64_t checkers;

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
     * @brief updates the attacked tiles for each side
    */
    void updateAttackedTiles();

    /**
     * @brief Updates the pins map with any new pins and deletes old ones.
    */
    void updatePins();

    /**
     * Determines if the king is in check. If yes, the "check" and "doubleCheck" variables will become
     * a bitboard, representing the line of sight of a piece that can see the king.
    */
    void determineCheckLine();

    /**
     * Updates the piece positions for both colors
     */
    void updatePiecePositionsColor();

    /**
     * Makes the current position into a unique, or at least close to unique, hash.
     */
    void hashPosition();
};
