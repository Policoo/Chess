#pragma once

#include <string>

#include "../board.h"
#include "../move.h"

class Engine {
public:
    virtual ~Engine() = default;

    /**
     * Get name of engine
     * @return name of engine
     */
    virtual std::string name() const = 0;

    /**
     * Looks ahead to try and find the best move
     * @param board board to find the best move on
     * @param time time in milliseconds to make the move
     * @return best move found
     */
    virtual Move bestMove(Board &board, int time) = 0;

    virtual int evaluate(Board &board) = 0;

    virtual void stop() {}
};
