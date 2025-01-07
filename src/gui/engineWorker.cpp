#include "engineWorker.h"

#include <iostream>

#include "../util/utils.h"

EngineWorker::EngineWorker(QObject* parent) :
    QObject(parent) {
}

void EngineWorker::goPerft(Board& board, int depth) {
    std::cout << board.positionToFen() << "\n";
    std::vector<std::string> result{ "Depth: " + std::to_string(depth) };

    Counter counter;
    std::unordered_map<std::string, int> counterResults = counter.goPerft(board.positionToFen(), depth);
    std::unordered_map<std::string, int> fishResults = stockFishPerft(board.positionToFen(), depth);

    //remove all correct results from the hashmap
    std::vector<std::string> keysToRemove;
    for (const auto& [move, counterCount]: counterResults) {
        if (fishResults.find(move) != fishResults.end()) {
            int fishCount = fishResults[move];

            //if result is correct
            if (counterCount == fishCount) {
                result.push_back(move + ": " + std::to_string(counterCount));
            }
            else {
                std::string difference = (fishCount - counterCount < 0)
                                             ? std::to_string(fishCount - counterCount)
                                             : "+" +
                                               std::to_string(fishCount - counterCount);
                std::string resultString;
                resultString.append(move).append(": ").append(std::to_string(counterCount))
                        .append(", StockFish: ").append(std::to_string(fishCount)).append(" (").
                        append(difference).append(")");

                result.push_back(resultString);
            }

            keysToRemove.push_back(move);
        }
    }


    //erase elements from counterResults and fishResults
    for (const auto& key: keysToRemove) {
        counterResults.erase(key);
        fishResults.erase(key);
    }

    if (!counterResults.empty()) {
        std::string illegalMovesString = "Illegal moves found: ";
        for (const auto& counterPair: counterResults) {
            illegalMovesString += counterPair.first + ", ";
        }
        result.push_back(illegalMovesString);
    }

    if (!fishResults.empty()) {
        std::string movesNotFound = "Moves not found: ";
        for (const auto& fishPair: fishResults) {
            movesNotFound += fishPair.first + ", ";
        }
        result.push_back(movesNotFound);
    }

    emit perftDone(result);
}

void EngineWorker::bestMove(Board& board) {

}


void EngineWorker::setEngine(std::string engine) {

}
