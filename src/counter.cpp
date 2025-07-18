#include <iostream>

#include "counter.h"
#include "move.h"

std::unordered_map<std::string, int> Counter::goPerft(const std::string& fenString, int depth) {
    const auto board = new Board(fenString);

    const std::vector<Move> moves = MoveGenerator::generateMoves(*board);

    int numPositions = 0;
    for (const Move& move: moves) {
        if (depth == 1) {
            results[move.toString()] = 1;
            numPositions += 1;
            continue;
        }

        if (move.toString() == "f3f2") {
            std::cout << "here" << std::endl;
        }

        board->makeMove(move, true);
        const int movePositions = countPositions(*board, depth - 1);
        board->undoMove(move);

        results[move.toString()] = movePositions;
        numPositions += movePositions;
    }

    results["Nodes searched"] = numPositions;

    delete board;
    return results;
}

int Counter::countPositions(Board& board, const int depth) {
    const std::vector<Move> moves = MoveGenerator::generateMoves(board);

    //this is for bulk counting, no reason to actually make all these moves
    if (depth == 1) {
        return static_cast<int>(moves.size());
    }

    int numPositions = 0;
    for (const Move& move: moves) {
        board.makeMove(move, true);
        const int movePositions = countPositions(board, depth - 1);
        board.undoMove(move);

        numPositions += movePositions;
    }

    return numPositions;
}
