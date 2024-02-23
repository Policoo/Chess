#pragma once

#include <string>

#include "board.h"
#include "moveGenerator.h"

class Counter {
public:
    std::unordered_map<std::string, int> goPerft(const std::string& fenString, int depth);

private:
    std::unordered_map<std::string, int> results;

    static int countPositions(Board& board, int depth);
};