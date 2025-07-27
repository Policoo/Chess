#pragma once

#include "../engine.h"

class Random : public Engine {
public:
    Random();

    std::string name() const override;
    Move bestMove(Board& board, int time) override;
    int evaluate(Board& board) override;
    void stop() override;
};
