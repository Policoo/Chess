#include "greedy.h"

#include <bit>
#include <chrono>
#include <functional>
#include <iostream>

#include "../../moveGenerator.h"

Greedy::Greedy() {

}

std::string Greedy::name() const {
    return "Greedy";
}

Move Greedy::bestMove(Board& board, int time /*timeMs*/) {
    // stopRequested_.store(false, std::memory_order_relaxed);
    const bool maximizing = (board.getTurn() == Piece::WHITE);
    int nodes = 0;
    const auto t0 = std::chrono::steady_clock::now();

    Move best{};
    int bestScore = maximizing ? std::numeric_limits<int>::min() : std::numeric_limits<int>::max();;

    auto moves = MoveGenerator::generateMoves(board);
    if (moves.empty()) {
        return best;
    }

    for (const Move& m : moves) {
        // if (stopRequested_.load(std::memory_order_relaxed)) break;

        board.makeMove(m);
        int s = minimax(board, this->depth - 1, !maximizing, std::numeric_limits<int>::min(), std::numeric_limits<int>::max(), nodes);
        board.undoMove(m);

        if ((maximizing && s > bestScore) || (!maximizing && s < bestScore)) {
            bestScore = s;
            best = m;
        }
    }

    const auto t1 = std::chrono::steady_clock::now();
    auto us = std::chrono::duration_cast<std::chrono::microseconds>(t1 - t0).count();
    double sec = us / 1e6;

    // std::cout << "[Minimax] depth=" << this->depth
    //           << " nodes=" << nodes
    //           << " time="  << std::fixed << std::setprecision(3) << (us / 1000.0) << " ms"
    //           << " nps="   << (sec > 0 ? static_cast<double>(nodes) / sec : 0.0)
    //           << " bestScore=" << bestScore
    //           << "\n";

    return best;
}

int Greedy::minimax(Board& board, int depth, bool maximizing, int alpha, int beta, int& nodes) {
    nodes++;

    // if (stopRequested_.load(std::memory_order_relaxed) || depth == 0 || board.isGameOver()) {
    if (depth == 0 || board.isGameOver()) {
        return evaluate(board);
    }

    auto moves = MoveGenerator::generateMoves(board);
    if (moves.empty()) {
        return evaluate(board);
    }

    if (maximizing) {
        int best = std::numeric_limits<int>::min();

        for (const Move& m : moves) {
            // if (stopRequested_.load(std::memory_order_relaxed)) break;

            board.makeMove(m);
            best = std::max(best, minimax(board, depth - 1, false, alpha, beta, nodes));
            board.undoMove(m);

            alpha = std::max(alpha, best);
            if (beta <= alpha) {
                break;
            }
        }

        return best;
    } else {
        int best = std::numeric_limits<int>::max();

        for (const Move& m : moves) {
            // if (stopRequested_.load(std::memory_order_relaxed)) break;

            board.makeMove(m);
            best = std::min(best, minimax(board, depth - 1, true, alpha, beta, nodes));
            board.undoMove(m);

            beta = std::min(beta, best);
            if (beta <= alpha) {
                break;
            }
        }

        return best;
    }
}

int Greedy::evaluate(Board& board) {
    if (board.isGameOver()) {
        if (board.isCheck() && board.getTurn() == Piece::WHITE) return std::numeric_limits<int>::max();
        if (board.isCheck() && board.getTurn() == Piece::BLACK) return std::numeric_limits<int>::min();

        //if it's not check, it's stalemate
        return 0;
    }

    int whiteScore = 0;
    int blackScore = 0;

    whiteScore += (std::popcount(board.getPiecePositions(Piece::create(Piece::PAWN, Piece::WHITE)) * 100));
    whiteScore += (std::popcount(board.getPiecePositions(Piece::create(Piece::KNIGHT, Piece::WHITE)) * 300));
    whiteScore += (std::popcount(board.getPiecePositions(Piece::create(Piece::BISHOP, Piece::WHITE)) * 300));
    whiteScore += (std::popcount(board.getPiecePositions(Piece::create(Piece::ROOK, Piece::WHITE)) * 500));
    whiteScore += (std::popcount(board.getPiecePositions(Piece::create(Piece::QUEEN, Piece::WHITE)) * 900));

    blackScore += (std::popcount(board.getPiecePositions(Piece::create(Piece::PAWN, Piece::BLACK)) * 100));
    blackScore += (std::popcount(board.getPiecePositions(Piece::create(Piece::KNIGHT, Piece::BLACK)) * 300));
    blackScore += (std::popcount(board.getPiecePositions(Piece::create(Piece::BISHOP, Piece::BLACK)) * 300));
    blackScore += (std::popcount(board.getPiecePositions(Piece::create(Piece::ROOK, Piece::BLACK)) * 500));
    blackScore += (std::popcount(board.getPiecePositions(Piece::create(Piece::QUEEN, Piece::BLACK)) * 900));

    return whiteScore - blackScore;
}

void Greedy::stop() {
    stopRequested_.store(true, std::memory_order_relaxed);
}
