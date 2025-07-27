//
// Created by Paul on 19/07/2025.
//

#include "random.h"
#include "../../moveGenerator.h"

#include <random>

Random::Random() {

}

std::string Random::name() const {
    return "Random";
}

Move Random::bestMove(Board& board, int time) {
    std::vector<Move> moves = MoveGenerator::generateMoves(board);

    static std::mt19937 rng{std::random_device{}()};
    std::uniform_int_distribution<int> dist(0, moves.size() - 1);
    int random_index = dist(rng);

    return moves[random_index];
}

int Random::evaluate(const Board& board) {
    return 0;
}

void Random::stop() {

}
