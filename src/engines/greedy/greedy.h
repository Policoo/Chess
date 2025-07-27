#pragma once

#include <atomic>

#include "../engine.h"

class Greedy : public Engine {
public:
    Greedy();

    std::string name() const override;
    Move bestMove(Board& board, int time) override;
    int evaluate(Board& board) override;
    void stop() override;

private:
    int depth = 3;
    std::atomic<bool> stopRequested_{false};

    int minimax(Board& board, int depth, bool maximizing, int alpha, int beta, int& nodes);
};
